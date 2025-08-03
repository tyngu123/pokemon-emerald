package br.com.yago.emerald.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MapNameBox {

    private enum State { HIDDEN, SHOWING, SLIDING_OUT }

    private final Texture boxTexture;
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    private State currentState = State.HIDDEN;
    private String mapName = "";
    private float timer = 0f;

    private float x, y, width, height;
    private float initialY;

    private static final float SHOW_DURATION = 1.5f;  // Duração que a caixa fica visível
    private static final float SLIDE_DURATION = 0.3f; // Duração da animação de saída

    public MapNameBox(Texture boxTexture, BitmapFont font) {
        this.boxTexture = boxTexture;
        this.font = font;
        this.width = boxTexture.getWidth() * 4;
        this.height = boxTexture.getHeight() * 4;
    }

    /**
     * Ativa a caixa para ser exibida com um nome de mapa.
     */
    public void show(String mapName) {
        this.mapName = mapName;
        this.timer = 0f;
        this.currentState = State.SHOWING;
    }

    public void update(float delta, Viewport uiViewport) {
        // Posiciona a caixa no canto superior esquerdo na primeira vez que for ativada
        if (initialY == 0) {
            this.x = 20f; // Margem da esquerda
            this.initialY = uiViewport.getWorldHeight() - this.height - 20f; // Margem do topo
            this.y = initialY;
        }

        if (currentState == State.HIDDEN) {
            return;
        }

        timer += delta;

        if (currentState == State.SHOWING) {
            if (timer >= SHOW_DURATION) {
                currentState = State.SLIDING_OUT;
                timer = 0; // Reseta o timer para a próxima animação
            }
        } else if (currentState == State.SLIDING_OUT) {
            // Calcula o progresso do slide (de 0.0 a 1.0)
            float progress = Math.min(1f, timer / SLIDE_DURATION);
            // Usa uma interpolação para um movimento suave
            y = Interpolation.pow2Out.apply(initialY, uiViewport.getWorldHeight(), progress);

            if (progress >= 1f) {
                currentState = State.HIDDEN;
                y = initialY; // Reseta a posição para a próxima vez
            }
        }
    }

    public void render(SpriteBatch batch) {
        if (currentState == State.HIDDEN) {
            return;
        }

        // Desenha a caixa
        batch.draw(boxTexture, x, y, width, height);

        // Usa o GlyphLayout para centralizar o texto dentro da caixa
        layout.setText(font, mapName);
        float textX = x + (width - layout.width) / 2;
        float textY = y + (height + layout.height) / 2;

        // Desenha o texto
        font.draw(batch, layout, textX, textY);
    }
}