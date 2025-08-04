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
    private boolean ignoreCollision; // ## NOVA PROPRIEDADE ##
    private boolean started = false;

    @Override
    public void initialize(JsonValue data) {
        this.target = data.getString("target", "initiator").toLowerCase();
        this.distance = data.getInt("distance", 1);
        this.ignoreCollision = data.getBoolean("ignoreCollision", false); // Lê do JSON

        String dirStr = data.getString("direction", "DOWN").toUpperCase();
        try {
            this.direction = Direction.valueOf(dirStr);
        } catch (IllegalArgumentException e) {
            this.direction = Direction.DOWN;
        }
    }

    // Setters para criação dinâmica
    public void setTarget(String target) { this.target = target; }
    public void setDirection(Direction direction) { this.direction = direction; }
    public void setDistance(int distance) { this.distance = distance; }
    public void setIgnoreCollision(boolean ignoreCollision) { this.ignoreCollision = ignoreCollision; }

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
        // ## CHAMADA ATUALIZADA ##
        // Passa o booleano 'ignoreCollision' para o método do Character.
        characterToMove.startContinuousMove(direction, distance, ignoreCollision);
    }

    @Override
    public void update(float delta) {
        // A lógica de movimento já é cuidada pela classe Character.
    }

    @Override
    public boolean isFinished() {
        if (!started) return false;
        if (characterToMove == null) return true;
        
        boolean done = !characterToMove.isContinuousMoving()
                && characterToMove.getCurrentState() == Character.State.IDLE;

        if (done) {
            characterToMove.setControlEnabled(true);
        }
        
        return done;
    }
}