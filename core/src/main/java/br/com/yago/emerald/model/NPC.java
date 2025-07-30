package br.com.yago.emerald.model;

import br.com.yago.emerald.config.GameConfig;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/**
 * Classe NPC com patrulha PACE, WANDER limitado por range, SPIN e STATIONARY.
 */
public class NPC extends Character {

    private final String id;
    private final String dialogueKey;
    private String movementType;
    private int movementRange;

    // Para WANDER limitado
    private final int startGridX;
    private final int startGridY;
    private float aiTimer;
    private Direction aiDirection = null;
    private final boolean isVisible;
    // IA SPIN
    private int spinIndex = 0;
    private final Direction[] spinDirections = {
        Direction.DOWN, Direction.LEFT, Direction.UP, Direction.RIGHT
    };

    // PACE
    private int paceSteps = 0;
    private boolean isPacingForward = true;

    private final String scriptFile;

    public NPC(String id,
               String npcType,
               String dialogueKey,
               String movementType,
               Direction initialDirection,
               int movementRange,
               TextureAtlas atlas,
               TiledMapTileLayer collisionLayer,
               Array<Entity> allEntities,
               float startX,
               float startY,
               String scriptFile, boolean isVisible) {
        super(atlas, collisionLayer, allEntities, startX, startY);
        this.id = id;
        this.dialogueKey = dialogueKey;
        this.movementType = movementType;
        this.movementRange = movementRange;
        this.facingDirection = initialDirection;
        this.scriptFile = scriptFile;
        // grava posição original em tiles
        this.isVisible = isVisible; // Armazena a nova propriedade
        this.startGridX = (int) (startX / GameConfig.TILE_SIZE);
        this.startGridY = (int) (startY / GameConfig.TILE_SIZE);
        resetAiTimer();
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if ("WANDER".equals(movementType)) {
            aiTimer -= delta;
            if (aiTimer <= 0f) {
                // escolhe direção aleatória
                int r = MathUtils.random(0, Direction.values().length - 1);
                aiDirection = Direction.values()[r];
                // passo apenas se dentro do range
                int currX = (int) (worldPosition.x / GameConfig.TILE_SIZE);
                int currY = (int) (worldPosition.y / GameConfig.TILE_SIZE);
                int nx = currX + aiDirection.getDx();
                int ny = currY + aiDirection.getDy();
                int dx = Math.abs(nx - startGridX);
                int dy = Math.abs(ny - startGridY);
                // usa distância de Manhattan
                if (dx + dy <= movementRange && isWalkable(nx, ny) && !isOccupied(nx, ny)) {
                    this.facingDirection = aiDirection;
                    startPosition.set(worldPosition);
                    targetPosition.set(nx * GameConfig.TILE_SIZE, ny * GameConfig.TILE_SIZE);
                    movementProgress = 0f;
                    currentState = State.WALKING;
                }
                resetAiTimer();
            }
        } else if ("SPIN_STATIONARY".equals(movementType)) {
            aiTimer -= delta;
            if (aiTimer <= 0f) {
                spinIndex = (spinIndex + 1) % spinDirections.length;
                this.facingDirection = spinDirections[spinIndex];
                resetAiTimer();
            }
        }
        // PACE e STATIONARY normais são tratados em super.update/updateMovement
    }

    @Override
    protected Direction getDesiredDirection() {
        if ("PACE_HORIZONTAL".equals(movementType)) {
            return isPacingForward ? Direction.RIGHT : Direction.LEFT;
        } else if ("PACE_VERTICAL".equals(movementType)) {
            return isPacingForward ? Direction.UP : Direction.DOWN;
        }
        // WANDER e SPIN usam lógica própria em update()
        return null;
    }

    @Override
    protected void updateMovement(float delta) {
        movementProgress += delta / GameConfig.PLAYER_MOVE_DURATION;
        worldPosition.x = Interpolation.linear.apply(
            startPosition.x, targetPosition.x, Math.min(movementProgress, 1f)
        );
        worldPosition.y = Interpolation.linear.apply(
            startPosition.y, targetPosition.y, Math.min(movementProgress, 1f)
        );
        if (movementProgress >= 1f) {
            worldPosition.set(targetPosition);
            if (movementType.startsWith("PACE")) {
                paceSteps++;
                if (paceSteps >= movementRange) {
                    isPacingForward = !isPacingForward;
                    paceSteps = 0;
                }
                Direction next = getDesiredDirection();
                if (next != null) {
                    this.facingDirection = next;
                    int cx = (int) (worldPosition.x / GameConfig.TILE_SIZE);
                    int cy = (int) (worldPosition.y / GameConfig.TILE_SIZE);
                    int nx = cx + next.getDx();
                    int ny = cy + next.getDy();
                    if (isWalkable(nx, ny) && !isOccupied(nx, ny)) {
                        startPosition.set(worldPosition);
                        targetPosition.set(nx * GameConfig.TILE_SIZE, ny * GameConfig.TILE_SIZE);
                        movementProgress = 0f;
                        currentState = State.WALKING;
                        return;
                    }
                }
            }
            currentState = State.IDLE;
        }
    }

    @Override
    public TextureRegion getCurrentFrame() {
        if (!isVisible) {
            return null; // Se for invisível, não retorna nenhuma textura.
        }
        if (movementType.startsWith("PACE")) {
            return super.getCurrentFrame();
        }
        return super.getCurrentFrame();
    }

    private void resetAiTimer() {
        if ("SPIN_STATIONARY".equals(movementType)) {
            aiTimer = 1f;
        } else {
            aiTimer = MathUtils.random(2f, 5f);
        }
    }

    // === Setters para script ===

    public void setMovementType(String movementType) {
        this.movementType = movementType;
        this.paceSteps = 0;
        this.isPacingForward = true;
        resetAiTimer();
    }

    public void setMovementRange(int movementRange) {
        this.movementRange = movementRange;
    }

    public String getScriptFile() {
        return scriptFile;
    }

    public String getId() {
        return id;
    }

    public String getDialogueKey() {
        return dialogueKey;
    }
}
