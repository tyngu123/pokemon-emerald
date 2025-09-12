package br.com.yago.emerald.script;

import br.com.yago.emerald.model.Character;
import br.com.yago.emerald.view.GameScreen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Um comando de script que executa uma animação pré-batalha,
 * controlando o zoom da câmera e o fade da tela simultaneamente.
 */
public class PreBattleAnimCommand implements ScriptCommand {

    private float duration;
    private float targetZoom;
    private float timer;

    private GameScreen screen;
    private OrthographicCamera camera;
    private float initialZoom;

    @Override
    public void initialize(JsonValue data) {
        this.duration = data.getFloat("duration", 0.04f);
        this.targetZoom = data.getFloat("targetZoom", 0.2f);
        this.timer = 0f;
    }

    @Override
    public void start(GameScreen screen, Character initiator) {
        this.screen = screen;
        this.camera = screen.getGameCamera();
        this.initialZoom = this.camera.zoom;

        // Inicia o fade out para durar o mesmo tempo que a animação de zoom
        this.screen.startFadeOut(this.duration, null);
    }

    @Override
    public void update(float delta) {
        timer += delta;
        float progress = Math.min(1f, timer / duration);

        // Aplica uma interpolação suave para o zoom
        camera.zoom = Interpolation.pow2Out.apply(initialZoom, targetZoom, progress);
        camera.update();
    }

    @Override
    public boolean isFinished() {
        // O comando termina quando o tempo se esgota e o fade da tela termina
        return timer >= duration && !screen.isFading();
    }
}