package br.com.yago.emerald.model;

import br.com.yago.emerald.config.GameConfig; // ## IMPORT NECESSÁRIO ##
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Entidade base do jogo, agora com controle de colisão e posição no grid.
 */
public abstract class Entity {
    protected final Vector2 worldPosition;
    protected final float width, height;
    protected final Rectangle bounds;

    public Entity(float x, float y, float width, float height) {
        this.worldPosition = new Vector2(x, y);
        this.width = width;
        this.height = height;
        this.bounds = new Rectangle(x, y, width, height);
    }

    public abstract void update(float delta);
    public abstract TextureRegion getCurrentFrame();

    public Vector2 getWorldPosition() { return worldPosition; }
    public float getWidth()  { return width; }
    public float getHeight() { return height; }

    public Rectangle getBounds() {
        bounds.setPosition(worldPosition);
        return bounds;
    }
    
    // ## MÉTODOS MOVIDOS PARA CÁ ##
    // Agora todas as entidades sabem sua posição no grid.
    public int getGridX() {
        return (int)(worldPosition.x / GameConfig.TILE_SIZE);
    }

    public int getGridY() {
        return (int)(worldPosition.y / GameConfig.TILE_SIZE);
    }
}