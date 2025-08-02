package br.com.yago.emerald.script;

import br.com.yago.emerald.model.Character;
import br.com.yago.emerald.model.NPC;
import br.com.yago.emerald.view.GameScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;

/**
 * Executa uma sequência de comandos de script em ordem. Trava o initiator e o
 * jogador enquanto o script está ativo e libera quando termina. Aplica lógica de
 * encadeamento de script e substituição (nextScript) para scripts run-once.
 */
public class ScriptRunner {
    private final GameScreen screen;
    private final Queue<ScriptCommand> commandQueue = new Queue<>();
    private ScriptCommand currentCommand;
    private Character initiator;
    private Runnable onFinish; // callback opcional
    private ScriptDefinition currentDefinition;
    private final ScriptLoader loader;

    public ScriptRunner(GameScreen screen) {
        this.screen = screen;
        this.loader = new ScriptLoader();
    }

    /**
     * Conveniência: inicia o script simples (sem metadado).
     */
    public void run(Character initiator, Array<ScriptCommand> commands) {
        runScript(commands, initiator, null);
    }

    /**
     * Inicia execução de comandos avulsos. Compatível legado.
     */
    public void runScript(Array<ScriptCommand> commands, Character initiator, Runnable onFinish) {
        if (commands == null || commands.size == 0) {
            Gdx.app.log("ScriptRunner", "runScript chamado com lista vazia de comandos, ignorando.");
            return;
        }
        this.currentDefinition = null; // não há definição completa aqui
        this.initiator = initiator;
        this.onFinish = onFinish;
        commandQueue.clear();
        for (ScriptCommand cmd : commands) {
            commandQueue.addLast(cmd);
        }
        if (initiator != null) initiator.setLocked(true);
        screen.getPlayer().setLocked(true);
        nextCommand();
    }

    /**
     * Roda uma definição completa (com runMode/nextScript).
     */
    public void runDefinition(Character initiator, ScriptDefinition definition, Runnable onFinish) {
        if (definition == null) {
            Gdx.app.log("ScriptRunner", "runDefinition recebeu definition null, abortando.");
            return;
        }
        Gdx.app.log("ScriptRunner", String.format("definição carregada: name=%s runMode=%s nextScript=%s commands=%d",
                definition.getName(),
                definition.getRunMode(),
                definition.getNextScript(),
                definition.getCommands() != null ? definition.getCommands().size : 0));

        if (definition.getCommands() == null || definition.getCommands().size == 0) {
            Gdx.app.log("ScriptRunner", "runDefinition recebeu definition sem comandos para: " + definition.getName() + ", abortando.");
            return;
        }

        // se é ONCE e já foi executado, ignora
        if (initiator instanceof NPC
                && definition.getRunMode() == ScriptDefinition.RunMode.ONCE
                && ((NPC) initiator).hasConsumedScript(definition.getName())) {
            Gdx.app.log("ScriptRunner", "pulando script ONCE já consumido: " + definition.getName() + " para NPC " + ((NPC) initiator).getId());
            if (onFinish != null) {
                onFinish.run();
            }
            return;
        }

        Gdx.app.log("ScriptRunner", "iniciando script " + definition.getName() +
                " (runMode=" + definition.getRunMode() + ") para " +
                (initiator instanceof NPC ? "NPC " + ((NPC) initiator).getId() : initiator.getClass().getSimpleName()));

        this.currentDefinition = definition;
        this.initiator = initiator;
        this.onFinish = onFinish;
        commandQueue.clear();
        for (ScriptCommand cmd : definition.getCommands()) {
            commandQueue.addLast(cmd);
        }
        if (initiator != null) initiator.setLocked(true);
        screen.getPlayer().setLocked(true);
        nextCommand();
    }

    /**
     * Conveniência para NPC: dispara o script dele corretamente usando definição,
     * aplicando runMode=ONCE e swap de nextScript.
     */
    public void triggerNpcScript(NPC npc) {
        triggerNpcScript(npc, null);
    }

    public void triggerNpcScript(NPC npc, Runnable onFinish) {
        if (npc == null) {
            Gdx.app.log("ScriptRunner", "triggerNpcScript chamado com npc null.");
            return;
        }
        String scriptFile = npc.getScriptFile();
        if (scriptFile == null || scriptFile.trim().isEmpty()) {
            Gdx.app.log("ScriptRunner", "triggerNpcScript: npc " + npc.getId() + " não tem scriptFile definido.");
            return;
        }
        scriptFile = scriptFile.trim();
        Gdx.app.log("ScriptRunner", "triggerNpcScript para NPC " + npc.getId() + " com scriptFile=" + scriptFile);
        if (!loader.scriptFileExists(scriptFile)) {
            Gdx.app.error("ScriptRunner", "scriptFile do NPC não existe: " + scriptFile + " para NPC " + npc.getId());
            return;
        }
        ScriptDefinition def = loader.loadScriptDefinition(scriptFile);
        runDefinition(npc, def, onFinish);
    }

    public void update(float delta) {
        if (currentCommand == null) return;

        currentCommand.update(delta);
        if (currentCommand.isFinished()) {
            nextCommand();
        }
    }

    private void nextCommand() {
        if (!commandQueue.isEmpty()) {
            currentCommand = commandQueue.removeFirst();
            currentCommand.start(screen, initiator);
        } else {
            Gdx.app.log("ScriptRunner", "script finalizado para " +
                    (initiator instanceof NPC ? "NPC " + ((NPC) initiator).getId() : initiator));
            currentCommand = null;
            if (initiator != null) initiator.setLocked(false);
            screen.getPlayer().setLocked(false);

            if (currentDefinition != null && initiator instanceof NPC) {
                NPC npc = (NPC) initiator;

                if (currentDefinition.getRunMode() == ScriptDefinition.RunMode.ONCE) {
                    npc.markScriptConsumed(currentDefinition.getName());
                    Gdx.app.log("ScriptRunner", "marcando script consumido: " + currentDefinition.getName() + " para NPC " + npc.getId());
                }

                if (currentDefinition.getRunMode() == ScriptDefinition.RunMode.ONCE
                        && currentDefinition.getNextScript() != null) {
                    String next = currentDefinition.getNextScript().trim();
                    if (loader.scriptFileExists(next)) {
                        npc.setScriptFile(next);
                        Gdx.app.log("ScriptRunner", "trocando script do NPC " + npc.getId() + " para nextScript: " + next);
                    } else {
                        Gdx.app.error("ScriptRunner", "nextScript definido mas não existe: " + next + " para NPC " + npc.getId());
                    }
                }
            }

            initiator = null;
            if (onFinish != null) {
                onFinish.run();
                onFinish = null;
            }
            currentDefinition = null;
        }
    }
    
 // Em ScriptRunner.java
    public ScriptLoader getLoader() {
        return loader;
    }

    public boolean isActive() {
        return currentCommand != null;
    }
}
