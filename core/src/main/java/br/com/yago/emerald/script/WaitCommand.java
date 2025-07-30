package br.com.yago.emerald.script;

import br.com.yago.emerald.model.Character;
import br.com.yago.emerald.view.GameScreen;
import com.badlogic.gdx.utils.JsonValue;

public class WaitCommand implements ScriptCommand {
    private float duration;
    private float timer;

    @Override
    public void initialize(JsonValue data) {
        this.duration = data.getFloat("duration", 0f);
    }

    @Override
    public void start(GameScreen screen, Character initiator) {
        this.timer = 0f;
    }

    @Override
    public void update(float delta) {
        timer += delta;
    }

    @Override
    public boolean isFinished() {
        return timer >= duration;
    }
}
