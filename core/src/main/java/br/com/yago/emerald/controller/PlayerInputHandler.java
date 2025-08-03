package br.com.yago.emerald.controller;

import br.com.yago.emerald.model.Direction;
import br.com.yago.emerald.model.Player;
import br.com.yago.emerald.model.Warp;
import br.com.yago.emerald.script.MoveToCommand;
import br.com.yago.emerald.script.FadeOutCommand;
import br.com.yago.emerald.script.ScriptCommand;
import br.com.yago.emerald.script.WarpCommand;
import br.com.yago.emerald.view.GameScreen;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.List;

public class PlayerInputHandler extends InputAdapter {

    private final Player player;
    private final GameScreen screen;
    private final List<Direction> pressedDirections = new ArrayList<>();

    public PlayerInputHandler(Player player, GameScreen screen) {
        this.player = player;
        this.screen = screen;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (player.isLocked()) {
            return false;
        }

        if (keycode == Input.Keys.Z) {
            if (!screen.getScriptRunner().isActive()) {
                player.requestInteraction();
            }
            return true;
        }

        Direction dir = getDirectionFromKeycode(keycode);
        if (dir != null) {
            if (!pressedDirections.contains(dir)) {
                pressedDirections.add(dir);
            }
            
            if (checkForWarp(dir)) {
                return true;
            }

            updatePlayerDirection();
            return true;
        }
        return false;
    }

    private boolean checkForWarp(Direction dir) {
        // ## CORREÇÃO: REMOVIDA A TRAVA DE ESTADO IDLE ##
        int targetX = player.getGridX() + dir.getDx();
        int targetY = player.getGridY() + dir.getDy();

        Warp targetWarp = screen.getWarpAt(targetX, targetY);

        if (targetWarp != null) {
            // ## LÓGICA REFEITA: CRIA UM SCRIPT DINÂMICO ##
            player.setNextDirection(null); // Limpa o input para o jogador não sair andando
            
            // Cria a sequência de comandos para a animação de warp
            Array<ScriptCommand> warpSequence = new Array<>();

            // 1. Comando para andar 1 passo para dentro do portal
            MoveToCommand move = new MoveToCommand();
            move.initialize(new JsonValue(JsonValue.ValueType.object)); // Inicializa vazio
            move.setTarget("player");
            move.setDirection(dir);
            move.setDistance(1);
            warpSequence.add(move);
            
            // 2. Comando para escurecer a tela
            FadeOutCommand fade = new FadeOutCommand();
            fade.initialize(new JsonValue(JsonValue.ValueType.object)); // Inicializa com valores padrão
            warpSequence.add(fade);

            // 3. Comando para efetivamente trocar de mapa
            WarpCommand warp = new WarpCommand(targetWarp);
            warpSequence.add(warp);
            
            // Executa a sequência usando o ScriptRunner
            screen.getScriptRunner().run(player, warpSequence);
            
            return true;
        }
        
        return false;
    }

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