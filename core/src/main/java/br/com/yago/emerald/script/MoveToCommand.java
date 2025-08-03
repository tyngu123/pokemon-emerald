package br.com.yago.emerald.script;

import br.com.yago.emerald.model.Character;
import br.com.yago.emerald.model.Direction;
import br.com.yago.emerald.view.GameScreen;
import com.badlogic.gdx.utils.JsonValue;

public class MoveToCommand implements ScriptCommand {

    private Character characterToMove;
    private String target;

    private Direction direction;
    private int distance;
    private boolean started = false;
    private int actualDistance = 0;

    @Override
    public void initialize(JsonValue data) {
        this.target = data.getString("target", "initiator").toLowerCase();

        String dirStr = data.getString("direction", "DOWN").toUpperCase();
        try {
            this.direction = Direction.valueOf(dirStr);
        } catch (IllegalArgumentException e) {
            this.direction = Direction.DOWN;
        }
        this.distance = data.getInt("distance", 1);
    }

    // ## MÉTODOS SETTER ADICIONADOS ##
    public void setTarget(String target) { this.target = target; }
    public void setDirection(Direction direction) { this.direction = direction; }
    public void setDistance(int distance) { this.distance = distance; }

    @Override
    public void start(GameScreen screen, Character initiator) {
        this.started = true;

        if ("player".equals(target)) {
            this.characterToMove = screen.getPlayer();
        } else {
            this.characterToMove = initiator;
        }

        if (this.characterToMove == null) {
            return;
        }

        characterToMove.setControlEnabled(false);

        actualDistance = characterToMove.maxWalkableDistance(direction, distance);
        if (actualDistance <= 0) {
            return;
        }

        characterToMove.startContinuousMove(direction, actualDistance);
    }

    @Override
    public void update(float delta) {
        // A lógica de movimento já é cuidada pela classe Character.
    }

    @Override
    public boolean isFinished() {
        if (started && characterToMove == null) {
            return true;
        }
        
        if (started && actualDistance == 0) {
            if (characterToMove != null) {
                characterToMove.setControlEnabled(true);
            }
            return true;
        }
        
        boolean done = started
                && !characterToMove.isContinuousMoving()
                && characterToMove.getCurrentState() == Character.State.IDLE;

        if (done) {
            characterToMove.setControlEnabled(true);
            return true;
        }
        
        return false;
    }
}