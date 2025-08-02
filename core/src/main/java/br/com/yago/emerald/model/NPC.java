package br.com.yago.emerald.model;

import br.com.yago.emerald.config.GameConfig;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import java.util.HashSet;
import java.util.Set;

public class NPC extends Character {

    private final String id;
    private final String dialogueKey;
    private String movementType;
    private String originalMovementType;
    private int movementRange;

    private final int startGridX;
    private final int startGridY;
    private float aiTimer;
    private Direction aiDirection = null;
    private final boolean isVisible;
    private int spinIndex = 0;
    private final Direction[] spinDirections = { Direction.DOWN, Direction.LEFT, Direction.UP, Direction.RIGHT };

    // ## LÓGICA DO PACE REFEITA ##
    private enum PaceState { FORWARD, RETURNING_TO_CENTER_FROM_FORWARD, BACKWARD, RETURNING_TO_CENTER_FROM_BACKWARD }
    private PaceState paceState = PaceState.FORWARD;
    private int paceSteps = 0;

    private String scriptFile;
    private final Set<String> consumedOnceScripts = new HashSet<>();

    public NPC(String id, String npcType, String dialogueKey, String movementType, Direction initialDirection,
               int movementRange, TextureAtlas atlas, TiledMapTileLayer collisionLayer, Array<Entity> allEntities,
               float startX, float startY, String scriptFile, boolean isVisible) {
        super(atlas, collisionLayer, allEntities, startX, startY);
        this.id = id;
        this.dialogueKey = dialogueKey;
        this.movementType = movementType;
        this.originalMovementType = movementType;
        this.movementRange = movementRange;
        this.facingDirection = initialDirection;
        this.scriptFile = scriptFile;
        this.isVisible = isVisible;
        this.startGridX = (int) (startX / GameConfig.TILE_SIZE);
        this.startGridY = (int) (startY / GameConfig.TILE_SIZE);
        resetAiTimer();
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (isLocked || isFollowingPath() || currentState == State.WALKING) {
            return;
        }

        if (!isFollowingPath() && "SCRIPTED".equals(movementType)) {
            restoreAiMovement();
        }

        if (currentState == State.IDLE) {
            if ("WANDER".equals(movementType)) {
                updateWander(delta);
            } else if ("SPIN_STATIONARY".equals(movementType)) {
                updateSpin(delta);
            }
        }
    }
    
    @Override
    protected Direction getDesiredDirection() {
        Direction pathDirection = super.getDesiredDirection();
        if (pathDirection != null) {
            return pathDirection;
        }
        
        // ## LÓGICA DO PACE CORRIGIDA E DE VOLTA ##
        if (movementType.startsWith("PACE")) {
            return getPaceDirection();
        }
        
        if ("WANDER".equals(movementType)) {
            return aiDirection;
        }
        
        return null;
    }

    private Direction getPaceDirection() {
        Direction forwardDir = ("PACE_HORIZONTAL".equals(movementType)) ? Direction.RIGHT : Direction.UP;
        Direction backwardDir = forwardDir.getOpposite();

        switch(paceState) {
            case FORWARD:
                return forwardDir;
            case RETURNING_TO_CENTER_FROM_FORWARD:
            case RETURNING_TO_CENTER_FROM_BACKWARD:
                 // A direção para voltar ao centro depende da posição atual
                int dx = startGridX - getGridX();
                int dy = startGridY - getGridY();
                if (Math.abs(dx) > Math.abs(dy)) return dx > 0 ? Direction.RIGHT : Direction.LEFT;
                return dy > 0 ? Direction.UP : Direction.DOWN;
            case BACKWARD:
                return backwardDir;
        }
        return null; // Nunca deve acontecer
    }

    private void updatePaceLogic() {
        if (!movementType.startsWith("PACE")) return;
        
        paceSteps++;

        switch (paceState) {
            case FORWARD:
                if (paceSteps >= movementRange) {
                    paceState = PaceState.RETURNING_TO_CENTER_FROM_FORWARD;
                    paceSteps = 0;
                }
                break;
            case RETURNING_TO_CENTER_FROM_FORWARD:
                if (getGridX() == startGridX && getGridY() == startGridY) {
                    paceState = PaceState.BACKWARD;
                    paceSteps = 0;
                }
                break;
            case BACKWARD:
                if (paceSteps >= movementRange) {
                    paceState = PaceState.RETURNING_TO_CENTER_FROM_BACKWARD;
                    paceSteps = 0;
                }
                break;
            case RETURNING_TO_CENTER_FROM_BACKWARD:
                if (getGridX() == startGridX && getGridY() == startGridY) {
                    paceState = PaceState.FORWARD;
                    paceSteps = 0;
                }
                break;
        }
    }
    
    @Override
    protected void onMovementComplete() {
        super.onMovementComplete();
        // Sinaliza que um passo terminou, para que a lógica seja atualizada.
        if (movementType.startsWith("PACE")) {
            updatePaceLogic();
        }
    }

    private void updateWander(float delta) {
        aiTimer -= delta;
        if (aiTimer <= 0f) {
            resetAiTimer();
            if (aiDirection != null) return;

            Array<Direction> possibleDirections = new Array<>();
            for (Direction dir : Direction.values()) {
                int targetX = getGridX() + dir.getDx();
                int targetY = getGridY() + dir.getDy();
                int distX = Math.abs(targetX - startGridX);
                int distY = Math.abs(targetY - startGridY);

                if (distX <= movementRange && distY <= movementRange && isWalkable(targetX, targetY) && !isOccupied(targetX, targetY)) {
                    possibleDirections.add(dir);
                }
            }
            if (possibleDirections.size > 0) {
                aiDirection = possibleDirections.random();
            }
        }
    }

    private void updateSpin(float delta) {
        aiTimer -= delta;
        if (aiTimer <= 0f) {
            spinIndex = (spinIndex + 1) % spinDirections.length;
            this.facingDirection = spinDirections[spinIndex];
            resetAiTimer();
        }
    }
    
    @Override
    protected void tryToMove(Direction direction) {
        super.tryToMove(direction);
        if (currentState == State.WALKING && "WANDER".equals(movementType)) {
            this.aiDirection = null;
        }
    }

    public void interruptAiMovement() {
        this.aiDirection = null;
        this.movementType = "SCRIPTED";
    }

    public void restoreAiMovement() {
        this.movementType = this.originalMovementType;
        this.paceSteps = 0;
        this.paceState = PaceState.FORWARD;
        resetAiTimer();
    }

    @Override
    public TextureRegion getCurrentFrame() {
        if (!isVisible) return null;
        return super.getCurrentFrame();
    }

    private void resetAiTimer() {
        if ("SPIN_STATIONARY".equals(movementType)) {
            aiTimer = 1f;
        } else {
            aiTimer = MathUtils.random(2f, 5f);
        }
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
        this.originalMovementType = movementType;
        this.aiDirection = null;
        this.paceSteps = 0;
        this.paceState = PaceState.FORWARD;
        resetAiTimer();
    }
    
    // Getters e Setters
    public String getId() { return id; }
    public String getDialogueKey() { return dialogueKey; }
    public String getScriptFile() { return scriptFile; }
    public void setScriptFile(String scriptFile) { this.scriptFile = (scriptFile != null) ? scriptFile.trim() : null; }
    public void setMovementRange(int movementRange) { this.movementRange = movementRange; }
    public boolean hasConsumedScript(String scriptName) { return scriptName != null && consumedOnceScripts.contains(scriptName.toLowerCase()); }
    public void markScriptConsumed(String scriptName) { if (scriptName != null) consumedOnceScripts.add(scriptName.toLowerCase()); }
    public Set<String> getConsumedOnceScripts() { return new HashSet<>(consumedOnceScripts); }
}