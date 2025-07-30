// src/main/java/br/com/yago/emerald/model/Entity.java
package br.com.yago.emerald.model;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/**
 * Sem dependência de Box2D; controla posição e tamanho.
 */
public abstract class Entity {
    protected final Vector2 worldPosition;
    protected final float width, height;

    public Entity(float x, float y, float width, float height) {
        this.worldPosition = new Vector2(x, y);
        this.width = width;
        this.height = height;
    }

    public abstract void update(float delta);
    public abstract TextureRegion getCurrentFrame();

    public Vector2 getWorldPosition() { return worldPosition; }
    public float getWidth()  { return width; }
    public float getHeight() { return height; }
}
