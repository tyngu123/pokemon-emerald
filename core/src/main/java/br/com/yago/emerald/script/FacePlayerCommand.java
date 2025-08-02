package br.com.yago.emerald.script;

import br.com.yago.emerald.model.Character;
import br.com.yago.emerald.model.Direction;
import br.com.yago.emerald.view.GameScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonValue;

public class FacePlayerCommand implements ScriptCommand {
    
    private String targetId;

    @Override 
    public void initialize(JsonValue data) {
        // Lê o ID do alvo. Se não houver, será nulo.
        this.targetId = data.getString("target", null);
    }

    @Override
    public void start(GameScreen screen, Character initiator) {
        Character characterToTurn = null;

        if (targetId != null) {
            // Se um ID foi especificado, busca o personagem.
            if ("player".equalsIgnoreCase(targetId)) {
                characterToTurn = screen.getPlayer();
            } else {
                characterToTurn = screen.getNpcById(targetId);
            }
        } else {
            // Se nenhum ID foi especificado, o alvo é quem iniciou o script.
            characterToTurn = initiator;
        }

        if (characterToTurn != null) {
            // Se encontrou o personagem, calcula a direção e o vira.
            Direction dirToPlayer = characterToTurn.getDirectionTo(screen.getPlayer());
            characterToTurn.setFacingDirection(dirToPlayer);
        } else {
            Gdx.app.error("FacePlayerCommand", "Alvo '" + targetId + "' não encontrado.");
        }
    }

    @Override 
    public void update(float delta) { /* Ação instantânea */ }
    
    @Override 
    public boolean isFinished() { return true; }
}