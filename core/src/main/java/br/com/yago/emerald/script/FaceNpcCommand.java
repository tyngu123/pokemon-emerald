package br.com.yago.emerald.script;

import br.com.yago.emerald.model.Character;
import br.com.yago.emerald.model.Direction;
import br.com.yago.emerald.model.NPC;
import br.com.yago.emerald.model.Player;
import br.com.yago.emerald.view.GameScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Comando de script que faz o jogador virar para encarar um NPC específico,
 * identificado pelo seu ID.
 */
public class FaceNpcCommand implements ScriptCommand {

    private String npcId;

    @Override
    public void initialize(JsonValue data) {
        // Pega o ID do NPC alvo do JSON.
        this.npcId = data.getString("npcId", null);
    }

    @Override
    public void start(GameScreen screen, Character initiator) {
        if (npcId == null) {
            Gdx.app.error("FaceNpcCommand", "Comando 'FACE_NPC' chamado sem um 'npcId'.");
            return;
        }

        // Pega as instâncias do Jogador e do NPC alvo.
        Player player = screen.getPlayer();
        NPC targetNpc = screen.getNpcById(npcId);

        if (player == null || targetNpc == null) {
            Gdx.app.error("FaceNpcCommand", "Jogador ou NPC com ID '" + npcId + "' não encontrado.");
            return;
        }

        // Calcula a direção do jogador para o NPC e vira o jogador.
        Direction directionToNpc = player.getDirectionTo(targetNpc);
        player.setFacingDirection(directionToNpc);
    }

    @Override
    public void update(float delta) {
        // Ação instantânea, não precisa de update.
    }

    @Override
    public boolean isFinished() {
        // Termina imediatamente.
        return true;
    }
}