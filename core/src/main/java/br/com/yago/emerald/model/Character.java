package br.com.yago.emerald.model;

import br.com.yago.emerald.config.GameConfig;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;
import java.util.Map;

public abstract class Character extends Entity {

    public enum State { IDLE, WALKING }

    protected State currentState;
    protected Direction facingDirection;
    protected float stateTimer;
    protected final Map<String, Animation<TextureRegion>> animations;

    protected final Vector2 startPosition;
    protected final Vector2 targetPosition;
    protected float movementProgress;

    protected TiledMapTileLayer collisionLayer;
    protected Array<Entity> allEntities;

    private boolean controlEnabled = true;
    protected boolean isLocked = false;
    private boolean justFinishedMoving = false;

    // === Continuous multi-tile movement for scripts ===
    private boolean continuousMoving = false;
    private final Vector2 continuousStartPosition = new Vector2();
    private final Vector2 continuousTargetPosition = new Vector2();
    private float continuousProgress = 0f;
    private float continuousDuration = 0f;

    public Character(TextureAtlas atlas, TiledMapTileLayer collisionLayer, Array<Entity> allEntities, float startX, float startY) {
        super(startX, startY, GameConfig.PLAYER_WIDTH, GameConfig.PLAYER_HEIGHT);
        this.collisionLayer = collisionLayer;
        this.allEntities = allEntities;
        this.currentState = State.IDLE;
        this.facingDirection = Direction.DOWN;
        this.stateTimer = 0;
        this.startPosition = new Vector2(startX, startY);
        this.targetPosition = new Vector2(startX, startY);
        this.movementProgress = 1f;

        animations = new HashMap<>();
        if (atlas != null) {
            float frameDuration = 0.15f;
            animations.put("walk_down", new Animation<>(frameDuration, atlas.findRegions("walkdown"), Animation.PlayMode.LOOP));
            animations.put("walk_up", new Animation<>(frameDuration, atlas.findRegions("walkup"), Animation.PlayMode.LOOP));
            animations.put("walk_left", new Animation<>(frameDuration, atlas.findRegions("walkleft"), Animation.PlayMode.LOOP));
            animations.put("walk_right", new Animation<>(frameDuration, atlas.findRegions("walkright"), Animation.PlayMode.LOOP));
            animations.put("idle_down", new Animation<>(frameDuration, atlas.findRegion("idledown")));
            animations.put("idle_up", new Animation<>(frameDuration, atlas.findRegion("idleup")));
            animations.put("idle_left", new Animation<>(frameDuration, atlas.findRegion("idleleft")));
            animations.put("idle_right", new Animation<>(frameDuration, atlas.findRegion("idleright")));
        }
    }

    /**
     * ## MÉTODO CORRIGIDO ##
     * A lógica foi reestruturada para permitir o movimento contínuo e também respeitar travas.
     */
    @Override
    public void update(float delta) {
        stateTimer += delta;

        // tratamento de movimento contínuo (multi-tile) para scripts
        if (continuousMoving) {
            continuousProgress += delta;
            float t = Math.min(continuousProgress / continuousDuration, 1f);
            worldPosition.x = Interpolation.linear.apply(continuousStartPosition.x, continuousTargetPosition.x, t);
            worldPosition.y = Interpolation.linear.apply(continuousStartPosition.y, continuousTargetPosition.y, t);
            if (t >= 1f) {
                continuousMoving = false;
                currentState = State.IDLE;
                this.justFinishedMoving = true;
                // sincroniza as posições base/tile para refletir o fim do movimento contínuo
                startPosition.set(worldPosition);
                targetPosition.set(worldPosition);
            }
            // enquanto estiver em movimento contínuo, não processa input normal
            return;
        }

        // A interpolação do movimento tile a tile
        if (currentState == State.WALKING) {
            updateMovement(delta);
        }

        // Se o personagem está travado, não processa novos inputs/IA.
        if (isLocked) {
            return;
        }

        // Limpa a flag no início do frame se estiver parado.
        if (currentState == State.IDLE) {
            justFinishedMoving = false;
        }

        // Processa a IA/Input do jogador somente se estiver parado e não travado.
        if (currentState == State.IDLE) {
            handleIdleInput();
        }
    }

    private void handleIdleInput() {
        if (!controlEnabled) return; // Checa o controle da IA/mov encadeado
        Direction desiredDirection = getDesiredDirection();
        if (desiredDirection == null) return;

        if (desiredDirection == facingDirection) {
            this.stateTimer = 0;
            tryToMove(desiredDirection);
        } else {
            this.facingDirection = desiredDirection;
            this.stateTimer = 0;
        }
    }

    protected void updateMovement(float delta) {
        movementProgress += delta / GameConfig.PLAYER_MOVE_DURATION;
        worldPosition.x = Interpolation.linear.apply(startPosition.x, targetPosition.x, Math.min(movementProgress, 1f));
        worldPosition.y = Interpolation.linear.apply(startPosition.y, targetPosition.y, Math.min(movementProgress, 1f));

        if (movementProgress >= 1f) {
            worldPosition.set(targetPosition);
            currentState = State.IDLE;
            this.justFinishedMoving = true; // Sinaliza que um passo foi concluído

            // Sincroniza os pontos base para o próximo movimento
            startPosition.set(worldPosition);
            targetPosition.set(worldPosition);

            // Não encadeia mais movimento aqui, a lógica de update cuida disso.
            // Isso evita movimentos indesejados após um script.
            if (!isLocked) {
                handleIdleInputChained();
            }
        }
    }

    private void handleIdleInputChained() {
        if (!controlEnabled) return;
        Direction desiredDirection = getDesiredDirection();
        if (desiredDirection == null) return;

        // Tenta se mover imediatamente. Se não, apenas vira.
        if (desiredDirection == facingDirection) {
            tryToMove(desiredDirection);
        } else {
            this.facingDirection = desiredDirection;
            this.stateTimer = 0;
        }
    }

    protected void tryToMove(Direction direction) {
        if (currentState != State.IDLE) return;

        int targetGridX = getGridX() + direction.getDx();
        int targetGridY = getGridY() + direction.getDy();

        if (isWalkable(targetGridX, targetGridY) && !isOccupied(targetGridX, targetGridY)) {
            startPosition.set(worldPosition);
            targetPosition.set(targetGridX * GameConfig.TILE_SIZE, targetGridY * GameConfig.TILE_SIZE);
            movementProgress = 0f;
            currentState = State.WALKING;
        }
    }

    public boolean isOccupied(int gridX, int gridY) {
        for (int i = 0; i < allEntities.size; i++) {
            Entity other = allEntities.get(i);
            if (other == this) continue;
            if (other instanceof Character) {
                Character c = (Character) other;
                int otherTargetGridX = (int) (c.targetPosition.x / GameConfig.TILE_SIZE);
                int otherTargetGridY = (int) (c.targetPosition.y / GameConfig.TILE_SIZE);
                if (gridX == otherTargetGridX && gridY == otherTargetGridY) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isWalkable(int gridX, int gridY) {
        if (gridX < 0 || gridX >= collisionLayer.getWidth() || gridY < 0 || gridY >= collisionLayer.getHeight()) {
            return false;
        }
        return collisionLayer.getCell(gridX, gridY) == null;
    }

    @Override
    public TextureRegion getCurrentFrame() {
        if (animations.isEmpty()) {
            return null;
        }
        String animationKey = (currentState == State.WALKING ? "walk_" : "idle_") + facingDirection.name().toLowerCase();
        return animations.get(animationKey).getKeyFrame(stateTimer, true);
    }

    public void setControlEnabled(boolean enabled) {
        this.controlEnabled = enabled;
        if (!enabled) {
            if (this instanceof Player) {
                ((Player) this).setNextDirection(null);
            }
        }
    }

    public void setFacingDirection(Direction direction) {
        if (currentState == State.IDLE) {
            this.facingDirection = direction;
            this.stateTimer = 0;
        }
    }

    public Direction getDirectionTo(Character target) {
        float dx = target.getWorldPosition().x - this.worldPosition.x;
        float dy = target.getWorldPosition().y - this.worldPosition.y;
        if (Math.abs(dx) > Math.abs(dy)) {
            return dx > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            return dy > 0 ? Direction.UP : Direction.DOWN;
        }
    }

    public boolean isControlEnabled() { return controlEnabled; }

    public void setLocked(boolean locked) { this.isLocked = locked; }
    public State getCurrentState() { return currentState; }
    public int getGridX() { return (int)(worldPosition.x / GameConfig.TILE_SIZE); }
    public int getGridY() { return (int)(worldPosition.y / GameConfig.TILE_SIZE); }
    protected abstract Direction getDesiredDirection();

    // --- MÉTODOS PARA SCRIPTING ---
    public void clearJustFinishedMovingFlag() { this.justFinishedMoving = false; }
    public boolean isJustFinishedMoving() { return justFinishedMoving; }
    public boolean isContinuousMoving() { return continuousMoving; }

    public void forceMove(Direction direction) {
        if (currentState == State.IDLE) {
            this.facingDirection = direction;
            this.stateTimer = 0;
            tryToMove(direction);
        }
    }

    /**
     * Retorna a maior distância (em tiles) que pode andar nessa direção sem colidir.
     */
    public int maxWalkableDistance(Direction dir, int desiredDistance) {
        for (int i = 1; i <= desiredDistance; i++) {
            int checkX = getGridX() + dir.getDx() * i;
            int checkY = getGridY() + dir.getDy() * i;
            if (!isWalkable(checkX, checkY) || isOccupied(checkX, checkY)) {
                return i - 1;
            }
        }
        return desiredDistance;
    }

    /**
     * Inicia um movimento contínuo de vários tiles em uma direção.
     */
    public void startContinuousMove(Direction dir, int distance) {
        int actualDistance = maxWalkableDistance(dir, distance);
        if (actualDistance <= 0) return;
        float dx = dir.getDx() * actualDistance * GameConfig.TILE_SIZE;
        float dy = dir.getDy() * actualDistance * GameConfig.TILE_SIZE;
        continuousStartPosition.set(worldPosition);
        continuousTargetPosition.set(worldPosition.x + dx, worldPosition.y + dy);
        continuousProgress = 0f;
        continuousDuration = actualDistance * GameConfig.PLAYER_MOVE_DURATION;
        this.facingDirection = dir;
        this.currentState = State.WALKING;
        this.continuousMoving = true;
    }
}
