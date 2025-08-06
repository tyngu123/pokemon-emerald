package br.com.yago.emerald.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MapNameBox {

    private enum State { HIDDEN, SHOWING, SLIDING_OUT }

    private final NinePatch boxNinePatch; // ## ALTERADO: de Texture para NinePatch ##
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    private State currentState = State.HIDDEN;
    private String mapName = "";
    private float timer = 0f;

    private float x, y, width, height;
    private float initialY;

    private static final float SHOW_DURATION = 1.5f;
    private static final float SLIDE_DURATION = 0.3f;

    // ## CONSTRUTOR ALTERADO ##
    public MapNameBox(NinePatch boxNinePatch, BitmapFont font) {
        this.boxNinePatch = boxNinePatch;
        this.font = font;
    }

    public void show(String mapName) {
        this.mapName = mapName;
        this.timer = 0f;
        this.currentState = State.SHOWING;

        // Calcula o tamanho da caixa dinamicamente com base no texto
        layout.setText(font, mapName);
        this.width = layout.width + 80f; // Adiciona margem horizontal
        this.height = layout.height + 20f; // Adiciona margem vertical
    }

    public void update(float delta, Viewport uiViewport) {
        // Posiciona a caixa no canto superior esquerdo na primeira vez que for ativada
        if (initialY == 0 && currentState != State.HIDDEN) {
            this.x = 20f;
            this.initialY = uiViewport.getWorldHeight() - this.height - 20f;
            this.y = initialY;
        }

        if (currentState == State.HIDDEN) {
            return;
        }

        timer += delta;

        if (currentState == State.SHOWING) {
            if (timer >= SHOW_DURATION) {
                currentState = State.SLIDING_OUT;
                timer = 0;
            }
        } else if (currentState == State.SLIDING_OUT) {
            float progress = Math.min(1f, timer / SLIDE_DURATION);
            y = Interpolation.pow2Out.apply(initialY, uiViewport.getWorldHeight(), progress);

            if (progress >= 1f) {
                currentState = State.HIDDEN;
                y = initialY;
            }
        }
    }

    public void render(SpriteBatch batch) {
        if (currentState == State.HIDDEN) {
            return;
        }

        // ## RENDERIZAÇÃO ALTERADA para usar o NinePatch ##
        boxNinePatch.draw(batch, x, y, width, height);

        // Centraliza o texto
        float textX = x + (width - layout.width) / 2;
        float textY = y + (height + layout.height) / 2;

        font.draw(batch, layout, textX, textY);
    }
}