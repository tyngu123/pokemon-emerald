package br.com.yago.emerald.script;

import br.com.yago.emerald.model.Character;
import br.com.yago.emerald.view.GameScreen;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;

import java.util.function.Consumer; // Import para o callback

/**
 * Executa uma fila de comandos de script em sequência.
 */
public class ScriptRunner {
    private final GameScreen screen;
    private final Queue<ScriptCommand> commandQueue = new Queue<>();
    private ScriptCommand currentCommand;
    private Character initiator;

    public ScriptRunner(GameScreen screen) {
        this.screen = screen;
    }

    public void runScript(Array<ScriptCommand> commands, Character initiator, Runnable onFinish) {
        if (isActive() || commands == null || commands.size == 0) {
            if(onFinish != null) onFinish.run();
            return;
        }
        this.initiator = initiator;
        for (ScriptCommand cmd : commands) {
            commandQueue.addLast(cmd);
        }
        // Trava os personagens ANTES de iniciar o primeiro comando
        initiator.setLocked(true);
        screen.getPlayer().setLocked(true);

        nextCommand();

        // Se o script terminar instantaneamente, chama o onFinish
        if (!isActive() && onFinish != null) {
            onFinish.run();
        }
    }

    public void update(float delta) {
        if (isActive()) {
            currentCommand.update(delta);

            // ## LÓGICA SIMPLIFICADA ##
            // Não precisa mais de casos especiais. Funciona para TODOS os comandos.
            if (currentCommand.isFinished()) {
                nextCommand();
            }
        }
    }

    private void nextCommand() {
        if (!commandQueue.isEmpty()) {
            currentCommand = commandQueue.removeFirst();
            currentCommand.start(screen, initiator);
        } else {
            // Fim do script
            currentCommand = null;
            // Destrava os personagens
            if(initiator != null) initiator.setLocked(false);
            screen.getPlayer().setLocked(false);
            initiator = null;
        }
    }

    public boolean isActive() {
        return currentCommand != null;
    }
}
