package br.com.yago.emerald.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;

/**
 * Gerencia o efeito de fade in/out da tela.
 */
public class FadeManager {

    private enum State { FADING_IN, FADING_OUT, IDLE }

    private State currentState = State.IDLE;
    private float timer = 0f;
    private float duration = 0f;
    private float alpha = 0f;
    private final Texture blackPixel; // Uma textura simples de 1x1 pixel preto
    private Runnable onFadeOutComplete; // Callback para executar ao final do fade out

    public FadeManager() {
        // Cria uma textura preta programaticamente para o fade
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        blackPixel = new Texture(pixmap);
        pixmap.dispose();
    }

    public void fadeIn(float duration) {
        this.currentState = State.FADING_IN;
        this.duration = duration;
        this.timer = 0f;
        this.alpha = 1f; // Começa totalmente preto
    }

    public void fadeOut(float duration, Runnable onComplete) {
        this.currentState = State.FADING_OUT;
        this.duration = duration;
        this.timer = 0f;
        this.alpha = 0f; // Começa totalmente visível
        this.onFadeOutComplete = onComplete;
    }

    public void update(float delta) {
        if (currentState == State.IDLE) return;

        timer += delta;
        float progress = Math.min(1f, timer / duration);

        if (currentState == State.FADING_IN) {
            alpha = Interpolation.fade.apply(1f, 0f, progress); // De preto para transparente
            if (progress >= 1f) {
                currentState = State.IDLE;
            }
        } else if (currentState == State.FADING_OUT) {
            alpha = Interpolation.fade.apply(0f, 1f, progress); // De transparente para preto
            if (progress >= 1f) {
                currentState = State.IDLE;
                if (onFadeOutComplete != null) {
                    onFadeOutComplete.run(); // Executa o callback
                }
            }
        }
    }

    public void render(SpriteBatch batch, float width, float height) {
        if (alpha > 0) {
            batch.setColor(1, 1, 1, alpha); // Define a transparência
            batch.draw(blackPixel, 0, 0, width, height);
            batch.setColor(1, 1, 1, 1); // Reseta a cor para o normal
        }
    }

    public void dispose() {
        blackPixel.dispose();
    }
}