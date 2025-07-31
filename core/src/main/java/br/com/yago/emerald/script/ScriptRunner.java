package br.com.yago.emerald.script;

import br.com.yago.emerald.model.Character;
import br.com.yago.emerald.view.GameScreen;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;

/**
 * Executa uma sequência de comandos de script em ordem. Trava o initiator e o
 * jogador enquanto o script está ativo e libera quando termina.
 */
public class ScriptRunner {
    private final GameScreen screen;
    private final Queue<ScriptCommand> commandQueue = new Queue<>();
    private ScriptCommand currentCommand;
    private Character initiator;
    private Runnable onFinish; // callback opcional

    public ScriptRunner(GameScreen screen) {
        this.screen = screen;
    }

    /**
     * Conveniência: inicia o script sem callback.
     */
    public void run(Character initiator, Array<ScriptCommand> commands) {
        runScript(commands, initiator, null);
    }

    /**
     * API principal (compatível com chamadas antigas): executa um script.
     */
    public void runScript(Array<ScriptCommand> commands, Character initiator, Runnable onFinish) {
        if (commands == null || commands.size == 0) return;
        this.initiator = initiator;
        this.onFinish = onFinish;
        commandQueue.clear();
        for (ScriptCommand cmd : commands) {
            commandQueue.addLast(cmd);
        }
        // trava os personagens
        if (initiator != null) initiator.setLocked(true);
        screen.getPlayer().setLocked(true);
        nextCommand();
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
            // fim do script
            currentCommand = null;
            if (initiator != null) initiator.setLocked(false);
            screen.getPlayer().setLocked(false);
            initiator = null;
            if (onFinish != null) {
                onFinish.run();
                onFinish = null;
            }
        }
    }

    public boolean isActive() {
        return currentCommand != null;
    }
}
