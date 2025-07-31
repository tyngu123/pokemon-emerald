package br.com.yago.emerald.script;

import br.com.yago.emerald.model.Character;
import br.com.yago.emerald.model.Direction;
import br.com.yago.emerald.view.GameScreen;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Comando que move um personagem uma certa quantidade de tiles em uma direção,
 * de forma contínua (sem parar tile a tile), mantendo o estado WALKING durante
 * todo o deslocamento e voltando para IDLE ao final.
 */
public class MoveToCommand implements ScriptCommand {

    private Character initiator;
    private Direction direction;
    private int distance;
    private GameScreen screen;
    private boolean started = false;
    private int actualDistance = 0;

    @Override
    public void initialize(JsonValue data) {
        String dirStr = data.getString("direction", "DOWN").toUpperCase();
        try {
            this.direction = Direction.valueOf(dirStr);
        } catch (IllegalArgumentException e) {
            this.direction = Direction.DOWN;
        }
        this.distance = data.getInt("distance", 1);
    }

    @Override
    public void start(GameScreen screen, Character initiator) {
        this.screen = screen;
        this.initiator = initiator;
        this.started = true;
        initiator.setControlEnabled(false);

        actualDistance = initiator.maxWalkableDistance(direction, distance);
        if (actualDistance <= 0) {
            return;
        }

        initiator.startContinuousMove(direction, actualDistance);
    }

    @Override
    public void update(float delta) {
        // Nada extra aqui: o próprio Character gerencia o progresso contínuo.
    }

    @Override
    public boolean isFinished() {
        boolean done = started
                && actualDistance > 0
                && !initiator.isContinuousMoving()
                && initiator.getCurrentState() == Character.State.IDLE;
        if (done) {
            initiator.setControlEnabled(true);
            return true;
        }
        if (started && actualDistance == 0) {
            initiator.setControlEnabled(true);
            return true;
        }
        return false;
    }
}
