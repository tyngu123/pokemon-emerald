package br.com.yago.emerald.script;

import br.com.yago.emerald.model.Character;
import br.com.yago.emerald.model.Direction;
import br.com.yago.emerald.model.NPC;
import br.com.yago.emerald.model.Player;
import br.com.yago.emerald.view.GameScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Queue;

public class PathfindToPlayerCommand implements ScriptCommand {
    private String npcId;
    private NPC targetNpc;
    private boolean commandStarted = false;

    @Override
    public void initialize(JsonValue data) {
        // Pega o ID do NPC do script. Se não for fornecido, usará o 'initiator'.
        this.npcId = data.getString("npcId", null);
    }

    @Override
    public void start(GameScreen screen, Character initiator) {
        if (npcId == null) {
            // Se nenhum ID for especificado, o alvo é quem iniciou o script.
            if (initiator instanceof NPC) {
                targetNpc = (NPC) initiator;
            }
        } else {
            // Se um ID for especificado, busca o NPC no mapa.
            targetNpc = screen.getNpcById(npcId);
        }
        
        if (targetNpc == null) {
            Gdx.app.error("PathfindToPlayer", "NPC com ID '" + npcId + "' não encontrado.");
            commandStarted = true; // Finaliza imediatamente se o NPC não for encontrado.
            return;
        }

        Player player = screen.getPlayer();
        Queue<Direction> path = screen.findPathFor(targetNpc, player);
        
        if (path.size > 0) {
            targetNpc.followPath(path);
        } else {
             Gdx.app.log("PathfindToPlayer", "Nenhum caminho encontrado para o NPC " + targetNpc.getId());
        }
        commandStarted = true;
    }

    @Override
    public void update(float delta) {
        // A lógica de seguir o caminho é tratada pela classe Character.
    }

    @Override
    public boolean isFinished() {
        // O comando termina quando o NPC não estiver mais seguindo um caminho.
        return commandStarted && (targetNpc == null || !targetNpc.isFollowingPath());
    }
}