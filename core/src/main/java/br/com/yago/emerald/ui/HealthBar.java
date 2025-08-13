package br.com.yago.emerald.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import br.com.yago.emerald.model.Pokemon;

/**
 * A class to draw Pokémon HP bars with a background.
 * The bar changes color and depletes from right to left.
 */
public class HealthBar {

    private final Texture backgroundBar; // The static gray background
    private final Texture greenBar;
    private final Texture yellowBar;
    private final Texture redBar;

    private final float maxWidth;
    private final float height;

    public HealthBar(float maxWidth, float height) {
        this.maxWidth = maxWidth;
        this.height = height;

        // Create a 1x1 light gray texture for the background
        Pixmap bgPixmap = new Pixmap(1, 1, Pixmap.Format.RGB888);
        bgPixmap.setColor(new Color(0.8f, 0.8f, 0.8f, 1f)); // Light Gray
        bgPixmap.fill();
        backgroundBar = new Texture(bgPixmap);

     // Create textures for the health colors
        Pixmap greenPixmap = new Pixmap(1, 1, Pixmap.Format.RGB888);
        greenPixmap.setColor(Color.valueOf("70F8A8")); // #70F8A8
        greenPixmap.fill();
        greenBar = new Texture(greenPixmap);

        Pixmap yellowPixmap = new Pixmap(1, 1, Pixmap.Format.RGB888);
        yellowPixmap.setColor(Color.valueOf("F8E038")); // #F8E038
        yellowPixmap.fill();
        yellowBar = new Texture(yellowPixmap);

        Pixmap redPixmap = new Pixmap(1, 1, Pixmap.Format.RGB888);
        redPixmap.setColor(new Color(0xFF4500FF)); // Orange-Red
        redPixmap.fill();
        redBar = new Texture(redPixmap);

        // Dispose of the pixmaps as they are no longer needed
        bgPixmap.dispose();
        greenPixmap.dispose();
        yellowPixmap.dispose();
        redPixmap.dispose();
    }

    public void draw(SpriteBatch batch, Pokemon pokemon, float x, float y) {
        if (pokemon == null) return;

        // 1. Draw the static gray background bar first
        batch.draw(backgroundBar, x, y, maxWidth, height);

        // 2. Calculate the health percentage
        float hpPercentage = (float) pokemon.getCurrentHP() / pokemon.getMaxHP();
        hpPercentage = Math.max(0, hpPercentage); // Ensure it doesn't go below zero

        // 3. Choose the appropriate color for the current health
        Texture barTexture;
        if (hpPercentage > 0.5f) {
            barTexture = greenBar;
        } else if (hpPercentage > 0.2f) {
            barTexture = yellowBar;
        } else {
            barTexture = redBar;
        }

        // 4. Calculate the current width of the colored health bar
        float currentWidth = maxWidth * hpPercentage;

        // 5. Draw the colored health bar on top of the background
        // The bar is aligned to the right, so it depletes from right to left
        batch.draw(barTexture, x, y, currentWidth, height);
    }

    public void dispose() {
        backgroundBar.dispose();
        greenBar.dispose();
        yellowBar.dispose();
        redBar.dispose();
    }
}