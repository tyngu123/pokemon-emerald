package br.com.yago.emerald.script;

import br.com.yago.emerald.model.Character;
import br.com.yago.emerald.model.NPC;
import br.com.yago.emerald.view.GameScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Comando de script que altera o padrão de movimentação de um NPC.
 * Pode alvejar um NPC específico por ID ou o iniciador do script.
 */
public class SetMovementCommand implements ScriptCommand {
    private String npcId; // O ID do NPC alvo (opcional)
    private String newType;
    private int newRange;
    private boolean finished = false;

    @Override
    public void initialize(JsonValue data) {
        // Lê os parâmetros do JSON. npcId é opcional.
        this.npcId = data.getString("npcId", null);
        this.newType  = data.getString("movementType", "STATIONARY");
        this.newRange = data.getInt("movementRange", 0);
    }

    @Override
    public void start(GameScreen screen, Character initiator) {
        NPC targetNpc = null;

        // Lógica para determinar o alvo do comando.
        if (npcId != null) {
            // Se um ID foi fornecido, busca o NPC no mapa.
            targetNpc = screen.getNpcById(npcId);
            if (targetNpc == null) {
                Gdx.app.error("SetMovementCommand", "NPC com ID '" + npcId + "' não foi encontrado.");
            }
        } else if (initiator instanceof NPC) {
            // Se nenhum ID foi fornecido, usa o NPC que iniciou o script.
            targetNpc = (NPC) initiator;
        }

        // Se um alvo válido foi encontrado, aplica as mudanças.
        if (targetNpc != null) {
            targetNpc.setMovementType(newType);
            targetNpc.setMovementRange(newRange);
        } else {
            Gdx.app.error("SetMovementCommand", "Comando não pôde encontrar um alvo NPC para modificar.");
        }

        finished = true; // Comando é instantâneo.
    }

    @Override
    public void update(float delta) {
        // Nada a fazer aqui.
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}