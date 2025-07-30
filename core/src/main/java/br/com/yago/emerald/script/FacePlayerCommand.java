package br.com.yago.emerald.script;

import br.com.yago.emerald.model.Character;
import br.com.yago.emerald.model.Direction;
import br.com.yago.emerald.view.GameScreen;
import com.badlogic.gdx.utils.JsonValue;

public class FacePlayerCommand implements ScriptCommand {
    @Override public void initialize(JsonValue data) { /* Sem parâmetros */ }

    @Override
    public void start(GameScreen screen, Character initiator) {
        Direction dirToPlayer = initiator.getDirectionTo(screen.getPlayer());
        initiator.setFacingDirection(dirToPlayer);
    }

    @Override public void update(float delta) { /* Ação instantânea */ }
    @Override public boolean isFinished() { return true; }
}
