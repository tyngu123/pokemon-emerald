package br.com.yago.emerald.model;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;

public class Follower extends Character {

    private final Player player;
    private final Queue<PathNode> playerTrail = new Queue<>();

    public static class PathNode {
        public int x, y;
        public Direction direction;

        public PathNode(int x, int y, Direction direction) {
            this.x = x;
            this.y = y;
            this.direction = direction;
        }
    }

    public Follower(TextureAtlas atlas, TiledMapTileLayer collisionLayer, Array<Entity> allEntities, Player player) {
        super(atlas, collisionLayer, allEntities, player.getWorldPosition().x, player.getWorldPosition().y);
        this.player = player;
        this.facingDirection = player.facingDirection;
    }

    @Override
    public void update(float delta) {
        // A orientação é sempre em relação ao jogador quando está parado
        if (currentState == State.IDLE) {
            this.facingDirection = getDirectionTo(player);
        }

        super.update(delta);
        
        // A lógica de movimento só é acionada quando o seguidor não está se movendo
        if (currentState == State.IDLE) {
            followPlayer();
        }
    }

    @Override
    public TextureRegion getCurrentFrame() {
        if (!visible || animations.isEmpty()) return null;

        String animationKey = "walk-" + facingDirection.name().toLowerCase();
        Animation<TextureRegion> anim = animations.get(animationKey);

        if (anim != null) {
            return anim.getKeyFrame(stateTimer, true);
        }
        
        return super.getCurrentFrame();
    }

	/**
     * Verifica a colisão com outras entidades, ignorando o jogador.
     */
    @Override
    public boolean isOccupied(int gridX, int gridY) {
        if (allEntities == null) return false;
        for (int i = 0; i < allEntities.size; i++) {
            Entity other = allEntities.get(i);
            if (other == this || other instanceof Player) { // Ignora a si mesmo e o jogador
                continue;
            }
            if (other instanceof Character) {
                Character c = (Character) other;
                if (c.getGridX() == gridX && c.getGridY() == gridY) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addPathNode(int x, int y, Direction direction) {
        playerTrail.addLast(new PathNode(x, y, direction));
    }

    /**
     * ## LÓGICA DE MOVIMENTO REFEITA E ROBUSTA ##
     */
    private void followPlayer() {
        // Prioridade 1: Seguir a trilha deixada pelo jogador
        if (playerTrail.size > 0) {
            PathNode nextNode = playerTrail.first();
            
            // Impede que o seguidor entre no tile do jogador
            if (nextNode.x == player.getGridX() && nextNode.y == player.getGridY()) {
                return; // Espera o jogador se mover
            }

            Direction moveDirection = getDirectionTo(nextNode.x, nextNode.y);
            if (moveDirection != null) {
                this.facingDirection = moveDirection; // Vira-se para a direção do movimento
                tryToMove(moveDirection);
            }
        } else {
            // Prioridade 2: Se não há trilha, reposicionar-se atrás do jogador
            int idealX = player.getGridX() - player.facingDirection.getDx();
            int idealY = player.getGridY() - player.facingDirection.getDy();

            // Se não está na posição ideal, move-se para lá
            if (getGridX() != idealX || getGridY() != idealY) {
                Direction moveDirection = getDirectionTo(idealX, idealY);
                if (moveDirection != null) {
                    tryToMove(moveDirection);
                }
            }
        }
    }
    
    @Override
    protected void onMovementComplete() {
        super.onMovementComplete();
        // Só remove o nó da trilha se o Pokémon chegou ao destino daquele nó
        if (playerTrail.size > 0) {
            PathNode completedNode = playerTrail.first();
            if (getGridX() == completedNode.x && getGridY() == completedNode.y) {
                 playerTrail.removeFirst();
            }
        }
    }

    private Direction getDirectionTo(int targetX, int targetY) {
        int dx = targetX - getGridX();
        int dy = targetY - getGridY();

        if (dx == 0 && dy == 0) return null; // Já está no destino

        // Prioriza o movimento no eixo com a maior distância a ser percorrida
        if (Math.abs(dx) > Math.abs(dy)) {
            return dx > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            return dy > 0 ? Direction.UP : Direction.DOWN;
        }
    }
}