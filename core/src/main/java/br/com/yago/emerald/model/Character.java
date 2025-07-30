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

    // ## NOVO: Flag para controlar se o personagem obedece a input/IA ##
    private boolean controlEnabled = true;

    protected boolean isLocked = false;

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

    // Modificar o início do método update()
    @Override
    public void update(float delta) {
        // Se o personagem está travado por um script, sua lógica de movimento/IA é ignorada.
        // A animação de idle continua rodando.
        if (isLocked) {
            stateTimer += delta;
            return;
        }

        stateTimer += delta;
        if (currentState == State.WALKING) {
            updateMovement(delta);
        } else if (currentState == State.IDLE) {
            handleIdleInput();
        }
    }

    private void handleIdleInput() {
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
            handleIdleInputChained();
        }
    }

    private void handleIdleInputChained() {
        if (!controlEnabled) return; // Não encadeia movimentos se o controle estiver desativado
        Direction desiredDirection = getDesiredDirection();
        if (desiredDirection == null) return;

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
        // Se não há animações (NPC invisível), retorna nulo.
        if (animations.isEmpty()) {
            return null;
        }
        String animationKey = (currentState == State.WALKING ? "walk_" : "idle_") + facingDirection.name().toLowerCase();
        return animations.get(animationKey).getKeyFrame(stateTimer, true);
    }

    // --- MÉTODOS PARA CONTROLE E INTERAÇÃO ---
    public void setControlEnabled(boolean enabled) {
        this.controlEnabled = enabled;
        if (!enabled) {
            // Se o controle for desativado, garante que o personagem pare de tentar se mover
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

    public void setLocked(boolean locked) {
        this.isLocked = locked;
    }

    public State getCurrentState() {
        return currentState;
    }

    public int getGridX() { return (int)(worldPosition.x / GameConfig.TILE_SIZE); }
    public int getGridY() { return (int)(worldPosition.y / GameConfig.TILE_SIZE); }

    protected abstract Direction getDesiredDirection();
}
