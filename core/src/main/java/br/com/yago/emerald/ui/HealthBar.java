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

    /**
     * NOVO MÉTODO: Desenha a barra de vida com base em uma porcentagem específica.
     * Isso é usado para a animação de perda de HP.
     * @param batch O SpriteBatch para desenhar.
     * @param hpPercentage A porcentagem de vida a ser exibida (0.0 a 1.0).
     * @param x A posição X.
     * @param y A posição Y.
     */
    public void draw(SpriteBatch batch, float hpPercentage, float x, float y) {
        // 1. Desenha o fundo cinza estático primeiro
        batch.draw(backgroundBar, x, y, maxWidth, height);

        // 2. Garante que a porcentagem não seja negativa
        hpPercentage = Math.max(0, hpPercentage);

        // 3. Escolhe a cor apropriada para a vida atual
        Texture barTexture;
        if (hpPercentage > 0.5f) {
            barTexture = greenBar;
        } else if (hpPercentage > 0.2f) {
            barTexture = yellowBar;
        } else {
            barTexture = redBar;
        }

        // 4. Calcula a largura atual da barra de vida colorida
        float currentWidth = maxWidth * hpPercentage;

        // 5. Desenha a barra de vida colorida sobre o fundo
        batch.draw(barTexture, x, y, currentWidth, height);
    }
    
    /**
     * Método original, agora chama o novo método sobrecarregado.
     * @param batch O SpriteBatch para desenhar.
     * @param pokemon O Pokémon cuja vida será exibida.
     * @param x A posição X.
     * @param y A posição Y.
     */
    public void draw(SpriteBatch batch, Pokemon pokemon, float x, float y) {
        if (pokemon == null) return;
        float hpPercentage = (float) pokemon.getCurrentHP() / pokemon.getMaxHP();
        draw(batch, hpPercentage, x, y);
    }

    public void dispose() {
        backgroundBar.dispose();
        greenBar.dispose();
        yellowBar.dispose();
        redBar.dispose();
    }
}
