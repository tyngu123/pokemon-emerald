package br.com.yago.emerald.script;

import br.com.yago.emerald.model.Character;
import br.com.yago.emerald.model.Direction;
import br.com.yago.emerald.view.GameScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Comando de script que força um personagem a virar para uma
 * direção cardeal específica (UP, DOWN, LEFT, RIGHT).
 */
public class TurnToCommand implements ScriptCommand {

    private Direction targetDirection;

    @Override
    public void initialize(JsonValue data) {
        // Lê a direção do JSON. Usa DOWN como padrão se não for especificada ou for inválida.
        String dirStr = data.getString("direction", "DOWN").toUpperCase();
        try {
            this.targetDirection = Direction.valueOf(dirStr);
        } catch (IllegalArgumentException e) {
            Gdx.app.error("TurnToCommand", "Direção inválida fornecida no script: " + dirStr);
            this.targetDirection = Direction.DOWN; // Valor seguro como padrão
        }
    }

    @Override
    public void start(GameScreen screen, Character initiator) {
        // Usa o método que já existe na classe Character para virar o personagem.
        initiator.setFacingDirection(targetDirection);
    }

    @Override
    public void update(float delta) {
        // É uma ação instantânea, não precisa de lógica no update.
    }

    @Override
    public boolean isFinished() {
        // O comando termina no mesmo frame em que começa.
        return true;
    }
}