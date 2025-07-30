package br.com.yago.emerald.script;

import br.com.yago.emerald.model.Character;
import br.com.yago.emerald.model.NPC;
import br.com.yago.emerald.view.GameScreen;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Comando de script que altera o padrão de movimentação do NPC.
 */
public class SetMovementCommand implements ScriptCommand {
    private String newType;
    private int newRange;
    private boolean finished = false;

    @Override
    public void initialize(JsonValue data) {
        // lê movementType e movementRange do JSON
        this.newType  = data.getString("movementType", "STATIONARY");
        this.newRange = data.getInt("movementRange", 0);
    }

    @Override
    public void start(GameScreen screen, Character initiator) {
        if (initiator instanceof NPC) {
            NPC npc = (NPC) initiator;
            npc.setMovementType(newType);
            npc.setMovementRange(newRange);
        }
        finished = true; // instantâneo
    }

    @Override
    public void update(float delta) {
        // nada
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}
