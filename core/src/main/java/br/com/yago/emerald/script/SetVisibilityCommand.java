package br.com.yago.emerald.script;

import br.com.yago.emerald.model.Character;
import br.com.yago.emerald.view.GameScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Comando de script que torna um personagem visível ou invisível.
 */
public class SetVisibilityCommand implements ScriptCommand {

    private String target;
    private boolean visible;

    @Override
    public void initialize(JsonValue data) {
        this.target = data.getString("target", "initiator").toLowerCase();
        this.visible = data.getBoolean("visible", true);
    }
    
    // Setters para criação dinâmica
    public void setTarget(String target) { this.target = target; }
    public void setVisible(boolean visible) { this.visible = visible; }

    @Override
    public void start(GameScreen screen, Character initiator) {
        Character characterToChange = null;
        if ("player".equals(target)) {
            characterToChange = screen.getPlayer();
        } else if (initiator instanceof Character) {
            characterToChange = initiator;
        } else {
             characterToChange = screen.getNpcById(target);
        }

        if (characterToChange != null) {
            characterToChange.setVisible(visible);
        } else {
            Gdx.app.error("SetVisibilityCommand", "Alvo '" + target + "' não encontrado.");
        }
    }

    @Override
    public void update(float delta) { /* Ação instantânea */ }

    @Override
    public boolean isFinished() {
        return true;
    }
}