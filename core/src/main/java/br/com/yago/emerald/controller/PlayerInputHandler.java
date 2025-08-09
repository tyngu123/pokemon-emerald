package br.com.yago.emerald.controller;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;

import br.com.yago.emerald.EmeraldGame;
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
import br.com.yago.emerald.ui.MenuBox;
import br.com.yago.emerald.view.GameScreen;

public class PlayerInputHandler extends InputAdapter {

    private final Player player;
    private final GameScreen screen;
    private final List<Direction> pressedDirections = new ArrayList<>();
    private final EmeraldGame game; // ## REFERÊNCIA AO JOGO PRINCIPAL ##

    public PlayerInputHandler(Player player, GameScreen screen, EmeraldGame game) { // ## CONSTRUTOR ATUALIZADO ##
        this.player = player;
        this.screen = screen;
        this.game = game;
    }

    public void update(float delta) {
        // Se o menu estiver visível, não faz nada relacionado ao movimento.
        if (screen.getMenuBox().isVisible()) {
            // Limpa as direções pressionadas para evitar movimento "preso" ao fechar o menu.
            pressedDirections.clear();
            player.setNextDirection(null);
            return;
        }

        // Lógica de movimento original
        if (player.isLocked() || !pressedDirections.isEmpty()) {
            updatePlayerDirection();
        } else {
            player.setNextDirection(null);
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        MenuBox menu = screen.getMenuBox();

        // Se o menu estiver visível, os inputs controlam apenas o menu.
        if (menu.isVisible()) {
            switch (keycode) {
                case Input.Keys.UP:
                case Input.Keys.W:
                    menu.moveSelectionUp();
                    break;
                case Input.Keys.DOWN:
                case Input.Keys.S:
                    menu.moveSelectionDown();
                    break;
                case Input.Keys.Z: // Botão de Ação/Seleção
                    String selectedOption = menu.getSelectedOption();
                 // ## LÓGICA PARA ABRIR A TELA DA PARTY ##
                    if ("POKEMON".equals(selectedOption)) {
                        game.openPartyScreen();
                    } 
                    // ## LÓGICA PARA ABRIR A MOCHILA ##
                    if ("BAG".equals(selectedOption)) {
                        game.openBagScreen();
                    } 
                    else if ("EXIT".equals(selectedOption)) {
                        menu.toggle();
                        player.setLocked(menu.isVisible());
                    }
                    break;
                case Input.Keys.ENTER:
                case Input.Keys.X:
                    menu.toggle();
                    player.setLocked(menu.isVisible());
                    break;
            }
            return true; // Consome o input para que não afete o jogo
        } else {
            // Se o menu NÃO estiver visível, a lógica de jogo normal se aplica.
            if (player.isLocked()) {
                return false;
            }

            if (keycode == Input.Keys.ENTER) {
                // Só abre o menu se nenhum script ou diálogo estiver ativo
                if (!screen.getScriptRunner().isActive() && screen.getDialogueBox().isFinished()) {
                    menu.toggle();
                    player.setLocked(menu.isVisible()); // Bloqueia o jogador
                    return true;
                }
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
    }

    private boolean checkForWarp(Direction dir) {
        if (player.getCurrentState() != Character.State.IDLE) return false;

        int targetX = player.getGridX() + dir.getDx();
        int targetY = player.getGridY() + dir.getDy();

        Warp targetWarp = screen.getWarpAt(targetX, targetY);

        if (targetWarp != null) {
            player.setNextDirection(null);
            
            Array<ScriptCommand> warpSequence = new Array<>();

            if (targetWarp.isDoor()) {
                AnimateDoorCommand animateDoor = new AnimateDoorCommand(targetWarp);
                warpSequence.add(animateDoor);
                
                MoveToCommand move = new MoveToCommand();
                move.initialize(new JsonValue(JsonValue.ValueType.object));
                move.setTarget("player");
                move.setDirection(dir);
                move.setDistance(1);
                move.setIgnoreCollision(true);
                warpSequence.add(move);
                
                WaitCommand wait = new WaitCommand();
                JsonValue waitData = new JsonValue(JsonValue.ValueType.object);
                waitData.addChild("duration", new JsonValue(0.2f));
                wait.initialize(waitData);
                warpSequence.add(wait);
                
                SetVisibilityCommand setInvisible = new SetVisibilityCommand();
                setInvisible.initialize(new JsonValue(JsonValue.ValueType.object));
                setInvisible.setTarget("player");
                setInvisible.setVisible(false);
                warpSequence.add(setInvisible);
                
                FadeOutCommand fade = new FadeOutCommand();
                fade.initialize(new JsonValue(JsonValue.ValueType.object));
                warpSequence.add(fade);

                WarpCommand warp = new WarpCommand(targetWarp);
                warpSequence.add(warp);
                
            } else {
                FadeOutCommand fade = new FadeOutCommand();
                fade.initialize(new JsonValue(JsonValue.ValueType.object));
                warpSequence.add(fade);

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
        // Só processa o keyUp se o menu estiver fechado
        if (!screen.getMenuBox().isVisible()) {
            Direction dir = getDirectionFromKeycode(keycode);
            if (dir != null) {
                pressedDirections.remove(dir);
                return true;
            }
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