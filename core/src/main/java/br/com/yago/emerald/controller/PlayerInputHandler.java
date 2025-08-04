package br.com.yago.emerald.controller;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;

import br.com.yago.emerald.model.Character;
import br.com.yago.emerald.model.Direction;
import br.com.yago.emerald.model.Player;
import br.com.yago.emerald.model.Warp;
import br.com.yago.emerald.script.AnimateDoorCommand;
import br.com.yago.emerald.script.FadeOutCommand;
import br.com.yago.emerald.script.MoveToCommand;
import br.com.yago.emerald.script.ScriptCommand;
import br.com.yago.emerald.script.SetVisibilityCommand;
import br.com.yago.emerald.script.WaitCommand;
import br.com.yago.emerald.script.WarpCommand;
import br.com.yago.emerald.view.GameScreen;

public class PlayerInputHandler extends InputAdapter {

    private final Player player;
    private final GameScreen screen;
    private final List<Direction> pressedDirections = new ArrayList<>();

    public PlayerInputHandler(Player player, GameScreen screen) {
        this.player = player;
        this.screen = screen;
    }

    public void update(float delta) {
        if (player.isLocked() || !pressedDirections.isEmpty()) {
            updatePlayerDirection();
        } else {
            player.setNextDirection(null);
        }
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
            
            if (player.getCurrentState() == Character.State.IDLE) {
                player.setFacingDirection(dir);
            }
            
            if (checkForWarp(dir)) {
                return true;
            }

            return true;
        }
        return false;
    }

    private boolean checkForWarp(Direction dir) {
        if (player.getCurrentState() != Character.State.IDLE) return false;

        int targetX = player.getGridX() + dir.getDx();
        int targetY = player.getGridY() + dir.getDy();

        Warp targetWarp = screen.getWarpAt(targetX, targetY);

        if (targetWarp != null) {
            player.setNextDirection(null); // Impede o jogador de continuar andando
            
            Array<ScriptCommand> warpSequence = new Array<>();

            // ## LÓGICA CONDICIONAL PARA O TIPO DE WARP ##
            if (targetWarp.isDoor()) {
                // Se for uma PORTA, monta a sequência completa com o passo e a animação.
                // Passo 1: Animar a porta
                AnimateDoorCommand animateDoor = new AnimateDoorCommand(targetWarp);
                warpSequence.add(animateDoor);
                
                // Passo 2: Mover o jogador para dentro do portal
                MoveToCommand move = new MoveToCommand();
                move.initialize(new JsonValue(JsonValue.ValueType.object));
                move.setTarget("player");
                move.setDirection(dir);
                move.setDistance(1);
                move.setIgnoreCollision(true);
                warpSequence.add(move);
                
             // 3. Esperar um pouco
                WaitCommand wait = new WaitCommand();
                JsonValue waitData = new JsonValue(JsonValue.ValueType.object);
                waitData.addChild("duration", new JsonValue(0.2f));
                wait.initialize(waitData);
                warpSequence.add(wait);
                
             // 4. Tornar o jogador invisível
                SetVisibilityCommand setInvisible = new SetVisibilityCommand();
                setInvisible.initialize(new JsonValue(JsonValue.ValueType.object));
                setInvisible.setTarget("player");
                setInvisible.setVisible(false);
                warpSequence.add(setInvisible);
                
                // Passo 3: Fade Out
                FadeOutCommand fade = new FadeOutCommand();
                fade.initialize(new JsonValue(JsonValue.ValueType.object));
                warpSequence.add(fade);

                // Passo 4: Trocar de mapa
                WarpCommand warp = new WarpCommand(targetWarp);
                warpSequence.add(warp);
                
            } else {
                // Se NÃO for uma porta (ex: transição de mapa), monta uma sequência simples.
                // Passo 1: Fade Out
                FadeOutCommand fade = new FadeOutCommand();
                fade.initialize(new JsonValue(JsonValue.ValueType.object));
                warpSequence.add(fade);

                // Passo 2: Trocar de mapa
                WarpCommand warp = new WarpCommand(targetWarp);
                warpSequence.add(warp);
            }
            
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