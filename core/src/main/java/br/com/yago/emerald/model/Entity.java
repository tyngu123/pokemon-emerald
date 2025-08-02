package br.com.yago.emerald.model;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Entidade base do jogo, agora com controle de colisão.
 */
public abstract class Entity {
    protected final Vector2 worldPosition;
    protected final float width, height;
    protected final Rectangle bounds; // Caixa de colisão

    public Entity(float x, float y, float width, float height) {
        this.worldPosition = new Vector2(x, y);
        this.width = width;
        this.height = height;
        // Inicializa a caixa de colisão com a posição e tamanho da entidade
        this.bounds = new Rectangle(x, y, width, height);
    }

    public abstract void update(float delta);
    public abstract TextureRegion getCurrentFrame();

    public Vector2 getWorldPosition() { return worldPosition; }
    public float getWidth()  { return width; }
    public float getHeight() { return height; }

    /**
     * Retorna a caixa de colisão da entidade, sincronizada com sua posição atual.
     * Este método resolve o erro de compilação.
     */
    public Rectangle getBounds() {
        bounds.setPosition(worldPosition);
        return bounds;
    }
}