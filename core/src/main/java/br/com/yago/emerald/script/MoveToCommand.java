package br.com.yago.emerald.script;

import br.com.yago.emerald.model.Character;
import br.com.yago.emerald.model.Direction;
import br.com.yago.emerald.view.GameScreen;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Comando que move um personagem (seja o 'initiator' ou o 'player')
 * uma certa quantidade de tiles em uma direção de forma contínua.
 */
public class MoveToCommand implements ScriptCommand {

    // NOVOS CAMPOS
    private Character characterToMove; // Armazena quem será movido
    private String target;             // "player" ou "initiator"

    private Direction direction;
    private int distance;
    private boolean started = false;
    private int actualDistance = 0;

    @Override
    public void initialize(JsonValue data) {
        // LÊ O NOVO PARÂMETRO "target". O padrão é "initiator".
        this.target = data.getString("target", "initiator").toLowerCase();

        String dirStr = data.getString("direction", "DOWN").toUpperCase();
        try {
            this.direction = Direction.valueOf(dirStr);
        } catch (IllegalArgumentException e) {
            this.direction = Direction.DOWN;
        }
        this.distance = data.getInt("distance", 1);
    }

    @Override
    public void start(GameScreen screen, Character initiator) {
        this.started = true;

        // LÓGICA PRINCIPAL: Decide quem mover com base no parâmetro "target"
        if ("player".equals(target)) {
            this.characterToMove = screen.getPlayer();
        } else {
            this.characterToMove = initiator;
        }

        // Se por algum motivo o personagem não for encontrado, não faz nada.
        if (this.characterToMove == null) {
            return;
        }

        characterToMove.setControlEnabled(false);

        actualDistance = characterToMove.maxWalkableDistance(direction, distance);
        if (actualDistance <= 0) {
            return;
        }

        characterToMove.startContinuousMove(direction, actualDistance);
    }

    @Override
    public void update(float delta) {
        // A lógica de movimento já é cuidada pela classe Character.
    }

    @Override
    public boolean isFinished() {
        // Se o personagem alvo nem existia, termina imediatamente.
        if (started && characterToMove == null) {
            return true;
        }
        
        // Se o personagem não podia se mover, termina e reabilita o controle.
        if (started && actualDistance == 0) {
            if (characterToMove != null) {
                characterToMove.setControlEnabled(true);
            }
            return true;
        }
        
        // A condição normal de término: o movimento contínuo acabou.
        boolean done = started
                && !characterToMove.isContinuousMoving()
                && characterToMove.getCurrentState() == Character.State.IDLE;

        if (done) {
            characterToMove.setControlEnabled(true);
            return true;
        }
        
        return false;
    }
}