package br.com.yago.emerald.controller;

import br.com.yago.emerald.model.Direction;
import br.com.yago.emerald.model.Player;
import br.com.yago.emerald.view.GameScreen; // Importar
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import java.util.ArrayList;
import java.util.List;

public class PlayerInputHandler extends InputAdapter {

    private final Player player;
    private final GameScreen screen; // ## NOVA REFERÊNCIA ##
    private final List<Direction> pressedDirections = new ArrayList<>();

    // O construtor agora recebe a GameScreen
    public PlayerInputHandler(Player player, GameScreen screen) {
        this.player = player;
        this.screen = screen;
    }

    @Override
    public boolean keyDown(int keycode) {
        // Checa a interação com 'Z'
        if (keycode == Input.Keys.Z) {
            // ## MUDANÇA PRINCIPAL AQUI ##
            // Só permite pedir uma nova interação se um script NÃO estiver rodando.
            // A DialogueBox ainda vai capturar este input para se fechar, mas uma
            // nova interação não será solicitada.
            if (!screen.getScriptRunner().isActive()) {
                player.requestInteraction();
            }
            return true;
        }

        // Lógica de movimento (sem alterações)
        Direction dir = getDirectionFromKeycode(keycode);
        if (dir != null) {
            if (!pressedDirections.contains(dir)) {
                pressedDirections.add(dir);
            }
            updatePlayerDirection();
            return true;
        }
        return false;
    }

    // ... (resto da classe sem alterações) ...
    @Override
    public boolean keyUp(int keycode) {
        Direction dir = getDirectionFromKeycode(keycode);
        if (dir != null) {
            pressedDirections.remove(dir);
            updatePlayerDirection();
            return true;
        }
        return false;
    }
    private void updatePlayerDirection() {
        if (pressedDirections.isEmpty()) {
            player.setNextDirection(null);
        } else {
            Direction latestDirection = pressedDirections.get(pressedDirections.size() - 1);
            player.setNextDirection(latestDirection);
        }
    }
    private Direction getDirectionFromKeycode(int keycode) {
        switch (keycode) {
            case Input.Keys.W: case Input.Keys.UP: return Direction.UP;
            case Input.Keys.S: case Input.Keys.DOWN: return Direction.DOWN;
            case Input.Keys.A: case Input.Keys.LEFT: return Direction.LEFT;
            case Input.Keys.D: case Input.Keys.RIGHT: return Direction.RIGHT;
            default: return null;
        }
    }
}
