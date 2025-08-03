package br.com.yago.emerald.script;

import br.com.yago.emerald.model.Character;
import br.com.yago.emerald.view.GameScreen;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Comando de script que escurece a tela (fade out) e espera a animação terminar.
 */
public class FadeOutCommand implements ScriptCommand {

    private GameScreen screen;
    private float duration;
    private boolean fadeStarted = false;

    @Override
    public void initialize(JsonValue data) {
        // Pega a duração do fade do JSON, com um padrão de 0.5 segundos.
        this.duration = data.getFloat("duration", 0.5f);
    }

    @Override
    public void start(GameScreen screen, Character initiator) {
        this.screen = screen;
        // Inicia o fade out na GameScreen. O callback é nulo porque o próprio comando
        // vai verificar quando o fade terminar.
        this.screen.startFadeOut(duration, null);
        this.fadeStarted = true;
    }

    @Override
    public void update(float delta) {
        // A lógica do fade é atualizada pela GameScreen.
    }

    @Override
    public boolean isFinished() {
        // O comando só termina quando o FadeManager não estiver mais fazendo fade.
        return fadeStarted && !screen.isFading();
    }
}