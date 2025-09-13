package br.com.yago.emerald.view;

import br.com.yago.emerald.EmeraldGame;
import br.com.yago.emerald.assets.AssetsLoader;
import br.com.yago.emerald.model.BattleCalculator;
import br.com.yago.emerald.model.ExperienceCalculator;
import br.com.yago.emerald.model.Move;
import br.com.yago.emerald.model.Party;
import br.com.yago.emerald.model.Pokemon;
import br.com.yago.emerald.tools.AnimationAssetLoader.AnimationParameters;
import br.com.yago.emerald.ui.AtlasFont;
import br.com.yago.emerald.ui.HealthBar;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Comparator;

public class BattleScreen extends ScreenAdapter {

    private final EmeraldGame game;
    private final GameScreen gameScreen;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;

    private final OrthographicCamera battleCamera;
    private final Viewport battleViewport;
    private final OrthographicCamera uiCamera;
    private final Viewport uiViewport;

    private final BitmapFont whiteFont;
    private final BitmapFont grayFont;
    private final AtlasFont statusFont;
    private final GlyphLayout layout = new GlyphLayout();
    private final AssetManager assetManager;
    private final AssetsLoader assetLoader;
    private final Texture background;
    private final TextureRegion battleTextbox;
    private final Texture dialogueArrow;
    private final TextureRegion platform;
    private final TextureRegion statusEnemy;
    private final TextureRegion statusPlayer;
    private final HealthBar playerHealthBar;
    private final HealthBar enemyHealthBar;
    private final Animation<TextureRegion> playerThrowAnimation;
    private final FadeManager fadeManager;

    private final TextureRegion battleActionUi;
    private final TextureRegion actionArrow;
    private final TextureRegion battleFightUi;
    private final TextureAtlas typeAtlas;

    private final Pokemon wildPokemon;
    private Pokemon playerPokemon;
    private final Party playerParty;
    private Animation<TextureRegion> playerPokemonAnimation;
    private Animation<TextureRegion> wildPokemonAnimation;
    private float animationTimer = 0f;
    private float cameraPanTimer = 0f;
    private float playerSpriteTimer = 0f;
    private float transitionTimer = 0f;
    private float faintAnimationTimer = 0f;

    private final Vector2 playerOnScreenPos = new Vector2(-80, -25);
    private final Vector2 playerOffScreenPos = new Vector2(-160, -25);
    private final Vector2 playerTrainerCurrentPos = new Vector2();
    private final Vector2 playerPokemonCurrentPos = new Vector2();
    private final Vector2 enemyPokemonCurrentPos = new Vector2();
    
    private final Vector2 enemyPlatformPos = new Vector2(80, 20);

    private final Vector2 playerStatusOnScreenPos = new Vector2(130, 50);
    private final Vector2 playerStatusOffScreenPos;
    private final Vector2 playerStatusCurrentPos = new Vector2();

    private enum BattleState {
        BATTLE_START, WILD_INTRO_SLIDE, WILD_INTRO_MESSAGE,
        SEND_OUT_MESSAGE, PLAYER_THROW_ANIM, PLAYER_POKEMON_ENTER, STATUS_BARS_ENTER,
        ACTION_SELECTION, MOVE_SELECTION,
        TURN_PROCESSING, BATTLE_MESSAGE_TYPING, BATTLE_MESSAGE_WAIT,
        HP_BAR_ANIMATING,
        POKEMON_FAINTING_ANIM, FAINT_MESSAGE_TYPING,
        EXP_SEQUENCE_START,
        EXP_GAIN_MESSAGE,
        EXP_BAR_ANIMATING, 
        LEVEL_UP_FLASH,
        LEVEL_UP_MESSAGE,
        LEVEL_UP_STAT_SHOW,
        LEVEL_UP_STAT_WAIT,
        POST_BATTLE_ACTIONS, 
        PLAYER_SWITCH_OUT, PLAYER_SWITCH_IN,
        BATTLE_END_WAIT,
        SWITCH_MESSAGE_COME_BACK, // NOVO ESTADO
        SWITCH_MESSAGE_GO         // NOVO ESTADO
    }

    private BattleState currentState;

    private String fullMessage = "";
    private String visibleMessage = "";
    private int charIndex = 0;
    private float typewriterTimer = 0f;
    private final float charsPerSecond = 40f;
    private float indicatorTimer = 0f;

    private int selectedActionIndex = 0;
    private final Array<Vector2> actionArrowPositions = new Array<>();

    private int selectedMoveIndex = 0;
    private int lastSelectedMoveIndex = 0;
    private final Array<Vector2> movePositions = new Array<>();
    private final Array<Move> displayedMoves = new Array<>(4);

    private final Queue<TurnAction> turnQueue = new Queue<>();
    private final Queue<String> messageQueue = new Queue<>();
    private Pokemon faintedPokemon = null;

    private Pokemon hpAnimTargetPokemon;
    private float hpAnimStart;
    private float hpAnimEnd;
    private float hpAnimTimer;
    private final float HP_ANIM_DURATION = 0.7f;
    private boolean playCriticalHitSound = false;

    private float expAnimStartPercentage;
    private float expAnimCurrentPercentage;
    private float expAnimTargetPercentage;
    private float expAnimTimer;
    private float expAnimDuration;
    private final float EXP_BAR_BASE_DURATION = 1.4f; 
    private int totalExpGained;
    private final Queue<Pokemon.LevelUpResult> levelUpQueue = new Queue<>();
    
    private Pokemon.LevelUpResult currentLevelUpStats;
    private final NinePatch statBoxPatch;
    private boolean statsBoxVisible = false;
    private float levelUpPauseTimer = 0f;
    private int levelToDisplayInHud;

    private Pokemon pokemonToSwitchIn;
    private boolean isVoluntarySwitch = false;

    private static class TurnAction {
        Pokemon attacker;
        Pokemon defender;
        Move move;
        BattleCalculator.DamageResult result;

        TurnAction(Pokemon attacker, Pokemon defender, Move move) {
            this.attacker = attacker;
            this.defender = defender;
            this.move = move;
        }
    }

    public BattleScreen(EmeraldGame game, GameScreen gameScreen, SpriteBatch batch, AssetsLoader assetLoader, Party playerParty, Pokemon wildPokemon) {
        this.game = game;
        this.gameScreen = gameScreen;
        this.batch = batch;
        this.shapeRenderer = new ShapeRenderer();
        this.playerParty = playerParty;
        this.assetLoader = assetLoader;
        this.fadeManager = new FadeManager();
        
        this.battleCamera = new OrthographicCamera();
        this.battleViewport = new ExtendViewport(240, 160, battleCamera);
        this.uiCamera = new OrthographicCamera();
        this.uiViewport = new ExtendViewport(240, 160, uiCamera);
        
        this.assetManager = assetLoader.manager;
        
        this.background = assetManager.get(assetLoader.BATTLE_BG, Texture.class);

        this.whiteFont = assetManager.get(assetLoader.PARTY_FONT_NAME + ".otf", BitmapFont.class);
        this.grayFont = assetManager.get(assetLoader.DIALOGUE_FONT_NAME + ".otf", BitmapFont.class);
        this.dialogueArrow = assetManager.get(assetLoader.DIALOGUE_ARROW, Texture.class);
        
        TextureAtlas battleAtlas = assetManager.get(assetLoader.BATTLE_ATLAS, TextureAtlas.class);
        this.battleTextbox = battleAtlas.findRegion("battle_textbox");
        this.platform = battleAtlas.findRegion("plataform");
        this.statusEnemy = battleAtlas.findRegion("status_enemy");
        this.statusPlayer = battleAtlas.findRegion("status_player");
        
        this.battleActionUi = battleAtlas.findRegion("battle_action_ui");
        this.actionArrow = battleAtlas.findRegion("action_arrow");
        
        this.battleFightUi = battleAtlas.findRegion("move_action_ui");
        this.typeAtlas = assetManager.get(assetLoader.TYPE_ATLAS, TextureAtlas.class);

        actionArrowPositions.add(new Vector2(128, 25));
        actionArrowPositions.add(new Vector2(185, 25));
        actionArrowPositions.add(new Vector2(128, 9));
        actionArrowPositions.add(new Vector2(185, 9));
        
        movePositions.add(new Vector2(10, 40));
        movePositions.add(new Vector2(95, 40));
        movePositions.add(new Vector2(10, 20));
        movePositions.add(new Vector2(95, 20));

        Array<TextureRegion> throwFrames = new Array<>();
        for (int i = 1; i <= 5; i++) {
            throwFrames.add(battleAtlas.findRegion("player_back" + i));
        }
        this.playerThrowAnimation = new Animation<>(0.1f, throwFrames, Animation.PlayMode.NORMAL);

        TextureAtlas alphabetAtlas = assetManager.get(assetLoader.ALPHABET_ATLAS, TextureAtlas.class);
        this.statusFont = new AtlasFont(alphabetAtlas, 1f, 1);
        
        this.playerHealthBar = new HealthBar(48, 3);
        this.enemyHealthBar = new HealthBar(48, 3);
        this.statBoxPatch = new NinePatch(assetManager.get(assetLoader.BOX, Texture.class), 8, 8, 8, 8);
        
        this.wildPokemon = wildPokemon;
        this.playerPokemon = playerParty.getFirstAvailablePokemon();
        this.levelToDisplayInHud = this.playerPokemon.getLevel();
        
        this.playerStatusOffScreenPos = new Vector2(uiViewport.getWorldWidth(), playerStatusOnScreenPos.y);
        this.playerStatusCurrentPos.set(playerStatusOffScreenPos);

        loadPlayerPokemonAnimation();
        loadWildPokemonAnimation();

        playerTrainerCurrentPos.set(playerOnScreenPos);
        
        this.currentState = BattleState.BATTLE_START;

        int expInLevel = playerPokemon.getExperiencePoints() - ExperienceCalculator.getTotalExpForLevel(playerPokemon.getLevel(), playerPokemon.getExperienceGroup());
        int expForNext = ExperienceCalculator.getExpToNextLevel(playerPokemon.getLevel(), playerPokemon.getExperienceGroup());
        this.expAnimCurrentPercentage = (expForNext > 0) ? (float) expInLevel / expForNext : 0f;
    }
    
    private void setMessage(String message, BattleState typingState, BattleState waitState) {
        this.fullMessage = message;
        this.visibleMessage = "";
        this.charIndex = 0;
        this.typewriterTimer = 0f;
        this.currentState = typingState;
    }
    
    private void startActionMessage() {
        setMessage("What will " + playerPokemon.getNickname().toUpperCase() + "\ndo?", BattleState.ACTION_SELECTION, BattleState.ACTION_SELECTION);
    }

    private String getAttackerName(Pokemon pokemon) {
        return pokemon == wildPokemon ? "Wild " + pokemon.getNickname().toUpperCase() : pokemon.getNickname().toUpperCase();
    }
    
    private void processNextTurnAction() {
        if (turnQueue.isEmpty()) {
            startActionMessage();
            return;
        }

        TurnAction currentAction = turnQueue.first();
        
        if (currentAction.attacker.getCurrentHP() <= 0) {
            turnQueue.removeFirst();
            processNextTurnAction();
            return;
        }

        setMessage(getAttackerName(currentAction.attacker) + " used\n" + currentAction.move.getName().toUpperCase() + "!", BattleState.BATTLE_MESSAGE_TYPING, BattleState.BATTLE_MESSAGE_WAIT);
    }
    
    private void executeTurn(Move playerMove) {
        currentState = BattleState.TURN_PROCESSING;
        turnQueue.clear();
        messageQueue.clear();

        Move enemyMove = wildPokemon.getMoves().random();

        TurnAction playerAction = new TurnAction(playerPokemon, wildPokemon, playerMove);
        TurnAction enemyAction = new TurnAction(wildPokemon, playerPokemon, enemyMove);

        boolean playerGoesFirst = (playerMove.getPriority() > enemyMove.getPriority()) ||
                                  (playerMove.getPriority() == enemyMove.getPriority() && playerPokemon.getSpeed() >= wildPokemon.getSpeed());

        if (playerGoesFirst) {
            turnQueue.addLast(playerAction);
            if (wildPokemon.getCurrentHP() > 0) {
                turnQueue.addLast(enemyAction);
            }
        } else {
            turnQueue.addLast(enemyAction);
            if (playerPokemon.getCurrentHP() > 0) {
                turnQueue.addLast(playerAction);
            }
        }
        
        processNextTurnAction();
    }
    
    private void loadPlayerPokemonAnimation() {
        if (playerPokemon == null) return;
        String path = playerPokemon.getBackSpriteGifPath();
        if (!assetManager.isLoaded(path, Animation.class)) {
            assetManager.load(path, Animation.class, new AnimationParameters());
            assetManager.finishLoading();
        }
        this.playerPokemonAnimation = assetManager.get(path, Animation.class);
    }

    private void loadWildPokemonAnimation() {
        if (wildPokemon == null) return;
        String path = wildPokemon.getFrontSpriteGifPath();
        if (!assetManager.isLoaded(path, Animation.class)) {
            assetManager.load(path, Animation.class, new AnimationParameters());
            assetManager.finishLoading();
        }
        this.wildPokemonAnimation = assetManager.get(path, Animation.class);
    }

    @Override
    public void show() {
        fadeManager.fadeIn(0.5f);
        
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (isStateInputLocked()) return true;

                // ## CORREÇÃO APLICADA AQUI ##
                // Isola o comportamento da tecla X quando na seleção de movimento.
                if (currentState == BattleState.MOVE_SELECTION && (keycode == Input.Keys.X || keycode == Input.Keys.ESCAPE)) {
                    // Chama o handler específico, que sabe como tratar o X como "cancelar".
                    handleMoveSelectionInput(keycode);
                    return true;
                }

                // Para os outros casos, Z e X podem ter a mesma função (avançar texto/confirmar).
                if (keycode == Input.Keys.Z || keycode == Input.Keys.X) {
                    handleConfirmationInput();
                    return true;
                }
                
                // Trata as teclas direcionais para a navegação nos menus.
                switch(currentState) {
                    case ACTION_SELECTION: return handleActionSelectionInput(keycode);
                    case MOVE_SELECTION: return handleMoveSelectionInput(keycode);
                }
                return false;
            }
        });
    }

    private boolean isStateInputLocked() {
        return currentState == BattleState.BATTLE_START ||
               currentState == BattleState.WILD_INTRO_SLIDE ||
               currentState == BattleState.PLAYER_THROW_ANIM ||
               currentState == BattleState.PLAYER_POKEMON_ENTER ||
               currentState == BattleState.STATUS_BARS_ENTER ||
               currentState == BattleState.TURN_PROCESSING ||
               currentState == BattleState.HP_BAR_ANIMATING ||
               currentState == BattleState.POKEMON_FAINTING_ANIM ||
               currentState == BattleState.EXP_SEQUENCE_START ||
               currentState == BattleState.EXP_GAIN_MESSAGE ||
               currentState == BattleState.EXP_BAR_ANIMATING ||
               currentState == BattleState.LEVEL_UP_FLASH ||
               currentState == BattleState.LEVEL_UP_MESSAGE ||
               currentState == BattleState.LEVEL_UP_STAT_SHOW ||
               currentState == BattleState.PLAYER_SWITCH_OUT ||
               currentState == BattleState.PLAYER_SWITCH_IN;
    }
    
    private void handleConfirmationInput() {
        if (charIndex < fullMessage.length()) {
            visibleMessage = fullMessage;
            charIndex = fullMessage.length();

            if (currentState == BattleState.WILD_INTRO_MESSAGE) {
                currentState = BattleState.SEND_OUT_MESSAGE;
            } else if (currentState == BattleState.BATTLE_MESSAGE_TYPING) {
                currentState = BattleState.BATTLE_MESSAGE_WAIT;
            } else if (currentState == BattleState.FAINT_MESSAGE_TYPING) {
                currentState = BattleState.POST_BATTLE_ACTIONS;
            } else if (currentState == BattleState.EXP_GAIN_MESSAGE) {
                startExpBarAnimation();
            } else if (currentState == BattleState.LEVEL_UP_MESSAGE) {
                currentState = BattleState.LEVEL_UP_STAT_SHOW;
                levelUpPauseTimer = 0f;
            } else if (currentState == BattleState.SWITCH_MESSAGE_COME_BACK) {
                currentState = BattleState.PLAYER_SWITCH_OUT;
                transitionTimer = 0f;
            } else if (currentState == BattleState.SWITCH_MESSAGE_GO) {
                currentState = BattleState.PLAYER_SWITCH_IN;
                transitionTimer = 0f;
            }
            return;
        }

        switch(currentState) {
            case SEND_OUT_MESSAGE:
                setMessage("Go! " + playerPokemon.getNickname().toUpperCase() + "!", BattleState.PLAYER_THROW_ANIM, BattleState.PLAYER_THROW_ANIM);
                break;
            case BATTLE_MESSAGE_WAIT:
                handleBattleMessageConfirmation();
                break;
            case POST_BATTLE_ACTIONS:
                handlePostFaintActions();
                break;
            case LEVEL_UP_STAT_WAIT:
                statsBoxVisible = false;
                currentLevelUpStats = null;
                startExpBarAnimation();
                break;
            case BATTLE_END_WAIT:
                game.closeBattleScreen();
                break;
            case ACTION_SELECTION:
                handleActionSelectionInput(Input.Keys.Z);
                break;
            case MOVE_SELECTION:
                handleMoveSelectionInput(Input.Keys.Z);
                break;
            case SWITCH_MESSAGE_COME_BACK:
                currentState = BattleState.PLAYER_SWITCH_OUT;
                transitionTimer = 0f;
                break;
            case SWITCH_MESSAGE_GO:
                currentState = BattleState.PLAYER_SWITCH_IN;
                transitionTimer = 0f;
                break;
        }
    }
    
    private void handleBattleMessageConfirmation() {
        if (!messageQueue.isEmpty()) {
            setMessage(messageQueue.removeFirst(), BattleState.BATTLE_MESSAGE_TYPING, BattleState.BATTLE_MESSAGE_WAIT);
            return;
        }

        TurnAction action = turnQueue.first();
        if (action.result == null) {
            action.result = BattleCalculator.calculateDamage(action.attacker, action.defender, action.move);
            
            if (action.result.isCritical) {
                playCriticalHitSound = true;
            }

            hpAnimTargetPokemon = action.defender;
            hpAnimStart = hpAnimTargetPokemon.getCurrentHP();
            action.defender.takeDamage(action.result.damage);
            hpAnimEnd = hpAnimTargetPokemon.getCurrentHP();
            hpAnimTimer = 0f;
            currentState = BattleState.HP_BAR_ANIMATING;

            if (action.result.isCritical) messageQueue.addLast("A critical hit!");
            if (action.result.effectiveness > 1.5f) messageQueue.addLast("It's super effective!");
            if (action.result.effectiveness < 1.0f && action.result.effectiveness > 0) messageQueue.addLast("It's not very effective...");
        } else {
            if (action.defender.getCurrentHP() <= 0) {
                faintedPokemon = action.defender;
                currentState = BattleState.POKEMON_FAINTING_ANIM;
                faintAnimationTimer = 0f;
            } else {
                turnQueue.removeFirst();
                processNextTurnAction();
            }
        }
    }
    
    private void handlePostFaintActions() {
        if (faintedPokemon == wildPokemon && playerPokemon.getCurrentHP() > 0) {
            currentState = BattleState.EXP_SEQUENCE_START;
        } else if (faintedPokemon == playerPokemon) {
            Pokemon nextPokemon = playerParty.getFirstAvailablePokemon();
            if (nextPokemon != null) {
                game.setScreen(new PartyScreen(game, gameScreen, batch, assetLoader, this, true));
            } else {
                setMessage("You have no more POKEMON to fight!", BattleState.BATTLE_END_WAIT, BattleState.BATTLE_END_WAIT);
            }
        } else {
            currentState = BattleState.BATTLE_END_WAIT;
        }
    }
    
    private boolean handleActionSelectionInput(int keycode) {
        switch (keycode) {
            case Input.Keys.UP: if (selectedActionIndex == 2) selectedActionIndex = 0; if (selectedActionIndex == 3) selectedActionIndex = 1; return true;
            case Input.Keys.DOWN: if (selectedActionIndex == 0) selectedActionIndex = 2; if (selectedActionIndex == 1) selectedActionIndex = 3; return true;
            case Input.Keys.LEFT: if (selectedActionIndex == 1) selectedActionIndex = 0; if (selectedActionIndex == 3) selectedActionIndex = 2; return true;
            case Input.Keys.RIGHT: if (selectedActionIndex == 0) selectedActionIndex = 1; if (selectedActionIndex == 2) selectedActionIndex = 3; return true;
            case Input.Keys.Z:
                if (selectedActionIndex == 0) { // FIGHT
                    sortAndArrangeMoves();
                    currentState = BattleState.MOVE_SELECTION;
                    selectedMoveIndex = lastSelectedMoveIndex;
                } else if (selectedActionIndex == 2) { // POKEMON
                    game.openPartyScreen(this);
                } else {
                    Gdx.app.log("BattleScreen", "Ação não implementada: " + selectedActionIndex);
                }
                return true;
        }
        return false;
    }
    
    private boolean handleMoveSelectionInput(int keycode) {
        int moveCount = displayedMoves.size;
        if (moveCount == 0) return false;
        int oldIndex = selectedMoveIndex;
        switch (keycode) {
            case Input.Keys.UP: if (selectedMoveIndex >= 2) selectedMoveIndex -= 2; break;
            case Input.Keys.DOWN: if (selectedMoveIndex < 2 && selectedMoveIndex + 2 < moveCount) selectedMoveIndex += 2; break;
            case Input.Keys.LEFT: if (selectedMoveIndex == 1 || selectedMoveIndex == 3) selectedMoveIndex -= 1; break;
            case Input.Keys.RIGHT: if ((selectedMoveIndex == 0 || selectedMoveIndex == 2) && selectedMoveIndex + 1 < moveCount) selectedMoveIndex += 1; break;
            case Input.Keys.X: case Input.Keys.ESCAPE: 
                startActionMessage(); 
                return true;
            case Input.Keys.Z:
                if(currentState == BattleState.MOVE_SELECTION) {
                    lastSelectedMoveIndex = selectedMoveIndex;
                    executeTurn(displayedMoves.get(selectedMoveIndex));
                }
                return true;
        }
        if (selectedMoveIndex >= moveCount) selectedMoveIndex = oldIndex;
        return true;
    }

    private void sortAndArrangeMoves() {
        displayedMoves.clear();
        Array<Move> originalMoves = new Array<>(playerPokemon.getMoves());
        originalMoves.sort(Comparator.comparingInt((Move m) -> m.getName().length()).reversed());
        Move[] arranged = new Move[4];
        if (originalMoves.size > 0) arranged[0] = originalMoves.get(0);
        if (originalMoves.size > 1) arranged[2] = originalMoves.get(1);
        if (originalMoves.size > 2) arranged[1] = originalMoves.get(2);
        if (originalMoves.size > 3) arranged[3] = originalMoves.get(3);
        for (Move m : arranged) if (m != null) displayedMoves.add(m);
    }

    private void updateStateTransitions(float delta) {
        float transitionSpeed = 2.0f;
        switch (currentState) {
            case BATTLE_START: 
                currentState = BattleState.WILD_INTRO_SLIDE; 
                transitionTimer = 0f; 
                enemyPokemonCurrentPos.set(uiViewport.getWorldWidth() + 40, enemyPlatformPos.y + 10); 
                break;
            case WILD_INTRO_SLIDE:
                transitionTimer += delta * 1.5f;
                enemyPokemonCurrentPos.interpolate(new Vector2(enemyPlatformPos.x - wildPokemonAnimation.getKeyFrame(0).getRegionWidth() / 2f, enemyPlatformPos.y + 10), transitionTimer, Interpolation.sineOut);
                if (transitionTimer >= 1.0f) {
                    setMessage("Wild " + wildPokemon.getNickname().toUpperCase() + " appeared!", BattleState.WILD_INTRO_MESSAGE, BattleState.WILD_INTRO_MESSAGE);
                }
                break;
            case PLAYER_THROW_ANIM:
                playerSpriteTimer += delta;
                if (playerThrowAnimation.isAnimationFinished(playerSpriteTimer)) {
                    currentState = BattleState.PLAYER_POKEMON_ENTER;
                    transitionTimer = 0f;
                    playerPokemonCurrentPos.set(playerOffScreenPos);
                }
                break;
            case PLAYER_POKEMON_ENTER:
                transitionTimer += delta * transitionSpeed;
                playerTrainerCurrentPos.interpolate(playerOffScreenPos, transitionTimer, Interpolation.exp5In);
                playerPokemonCurrentPos.interpolate(playerOnScreenPos, transitionTimer, Interpolation.exp5Out);
                if (transitionTimer >= 1.0f) {
                    playerTrainerCurrentPos.set(playerOffScreenPos);
                    playerPokemonCurrentPos.set(playerOnScreenPos);
                    currentState = BattleState.STATUS_BARS_ENTER;
                    transitionTimer = 0f;
                }
                break;
            case STATUS_BARS_ENTER:
                transitionTimer += delta * transitionSpeed;
                playerStatusCurrentPos.interpolate(playerStatusOnScreenPos, transitionTimer, Interpolation.sineOut);
                if(transitionTimer >= 1.0f) {
                    playerStatusCurrentPos.set(playerStatusOnScreenPos);
                    startActionMessage();
                }
                break;
            case HP_BAR_ANIMATING:
                if (playCriticalHitSound) {
                    game.getSoundManager().playCriticalHitSfx();
                    playCriticalHitSound = false;
                }
                hpAnimTimer += delta;
                if (hpAnimTimer >= HP_ANIM_DURATION) {
                    currentState = BattleState.BATTLE_MESSAGE_WAIT;
                    handleBattleMessageConfirmation();
                }
                break;
            case POKEMON_FAINTING_ANIM:
                faintAnimationTimer += delta;
                if (faintAnimationTimer >= 0.6f) {
                    setMessage(getAttackerName(faintedPokemon) + " fainted!", BattleState.FAINT_MESSAGE_TYPING, BattleState.FAINT_MESSAGE_TYPING);
                }
                break;
            case EXP_SEQUENCE_START:
                totalExpGained = BattleCalculator.calculateExperienceGained(playerPokemon, wildPokemon);
                if (totalExpGained > 0) {
                    Array<Pokemon.LevelUpResult> levelUps = playerPokemon.gainExperience(totalExpGained);
                    levelUpQueue.clear();
                    for (Pokemon.LevelUpResult res : levelUps) {
                        levelUpQueue.addLast(res);
                    }
                    setMessage(playerPokemon.getNickname().toUpperCase() + " gained\n" + totalExpGained + " EXP. Points!", BattleState.EXP_GAIN_MESSAGE, BattleState.EXP_GAIN_MESSAGE);
                    levelUpPauseTimer = 0f;
                } else {
                    currentState = BattleState.BATTLE_END_WAIT;
                }
                break;
            case EXP_GAIN_MESSAGE:
                levelUpPauseTimer += delta;
                if (levelUpPauseTimer >= 0.35f) {
                    startExpBarAnimation();
                }
                break;
            case EXP_BAR_ANIMATING:
                updateExpAnimation(delta);
                break;
            case LEVEL_UP_FLASH:
                levelUpPauseTimer += delta;
                if (levelUpPauseTimer > 0.25f) {
                    this.levelToDisplayInHud = currentLevelUpStats.newLevel;
                    this.expAnimCurrentPercentage = 0f;
                    setMessage(playerPokemon.getNickname().toUpperCase() + " grew to\nLV. " + currentLevelUpStats.newLevel + "!", BattleState.LEVEL_UP_MESSAGE, BattleState.LEVEL_UP_MESSAGE);
                    levelUpPauseTimer = 0f;
                }
                break;
            case LEVEL_UP_MESSAGE:
                levelUpPauseTimer += delta;
                if (levelUpPauseTimer >= 0.30f) {
                    currentState = BattleState.LEVEL_UP_STAT_SHOW;
                    levelUpPauseTimer = 0f;
                }
                break;
            case LEVEL_UP_STAT_SHOW:
                levelUpPauseTimer += delta;
                if (levelUpPauseTimer > 0.2f) {
                    statsBoxVisible = true;
                    currentState = BattleState.LEVEL_UP_STAT_WAIT;
                }
                break;
            case PLAYER_SWITCH_OUT:
                transitionTimer += delta * transitionSpeed * 1.5f;
                playerPokemonCurrentPos.interpolate(playerOffScreenPos, transitionTimer, Interpolation.exp5In);
                if (transitionTimer >= 1f) {
                    playerPokemon = pokemonToSwitchIn;
                    this.levelToDisplayInHud = playerPokemon.getLevel();
                    loadPlayerPokemonAnimation();

                    // ## CORREÇÃO DA BARRA DE XP ##
                    // Recalcula a porcentagem de XP para o novo Pokémon que entrou.
                    int expInLevel = playerPokemon.getExperiencePoints() - ExperienceCalculator.getTotalExpForLevel(playerPokemon.getLevel(), playerPokemon.getExperienceGroup());
                    int expForNext = ExperienceCalculator.getExpToNextLevel(playerPokemon.getLevel(), playerPokemon.getExperienceGroup());
                    this.expAnimCurrentPercentage = (expForNext > 0) ? (float) expInLevel / expForNext : 0f;
                    
                    pokemonToSwitchIn = null;
                    setMessage("VAI " + playerPokemon.getNickname().toUpperCase() + "!", BattleState.SWITCH_MESSAGE_GO, BattleState.SWITCH_MESSAGE_GO);
                }
                break;
            case PLAYER_SWITCH_IN:
                transitionTimer += delta * transitionSpeed * 1.5f;
                playerPokemonCurrentPos.interpolate(playerOnScreenPos, transitionTimer, Interpolation.exp5Out);
                if(transitionTimer >= 1f) {
                    currentState = BattleState.STATUS_BARS_ENTER;
                    transitionTimer = 0f;
                    if (isVoluntarySwitch) {
                        isVoluntarySwitch = false; 
                        turnQueue.clear();
                        Move enemyMove = wildPokemon.getMoves().random();
                        turnQueue.addLast(new TurnAction(wildPokemon, playerPokemon, enemyMove));
                        processNextTurnAction();
                    }
                }
                break;
        }
    }

    private void startExpBarAnimation() {
        expAnimTimer = 0f;
        
        if (levelUpQueue.size > 0 || (currentLevelUpStats != null && expAnimCurrentPercentage < 1f) ) {
            expAnimTargetPercentage = 1f;
        } else {
            int expInLevel = playerPokemon.getExperiencePoints() - ExperienceCalculator.getTotalExpForLevel(playerPokemon.getLevel(), playerPokemon.getExperienceGroup());
            int expForNext = ExperienceCalculator.getExpToNextLevel(playerPokemon.getLevel(), playerPokemon.getExperienceGroup());
            expAnimTargetPercentage = (expForNext > 0) ? (float) expInLevel / expForNext : 0f;
        }
    
        expAnimStartPercentage = expAnimCurrentPercentage;
    
        float percentageToFill = expAnimTargetPercentage - expAnimStartPercentage;
        expAnimDuration = Math.max(EXP_BAR_BASE_DURATION * percentageToFill, 0.45f);
    
        if (expAnimDuration > 0.01f) {
            currentState = BattleState.EXP_BAR_ANIMATING;
        } else if (levelUpQueue.size > 0) {
            levelUpPauseTimer = 0f;
            currentLevelUpStats = levelUpQueue.removeFirst();
            currentState = BattleState.LEVEL_UP_FLASH;
        } else {
            currentState = BattleState.BATTLE_END_WAIT;
        }
    }

    private void updateExpAnimation(float delta) {
        expAnimTimer += delta;
        float progress = Math.min(1f, expAnimTimer / expAnimDuration);
        
        expAnimCurrentPercentage = Interpolation.pow2Out.apply(expAnimStartPercentage, expAnimTargetPercentage, progress);
        
        if (progress >= 1f) {
            expAnimCurrentPercentage = expAnimTargetPercentage; 
            
            if (expAnimCurrentPercentage >= 1f && (levelUpQueue.size > 0 || currentLevelUpStats != null)) {
                if(currentLevelUpStats == null) currentLevelUpStats = levelUpQueue.removeFirst();
                levelUpPauseTimer = 0f;
                currentState = BattleState.LEVEL_UP_FLASH;
            } else {
                currentState = BattleState.BATTLE_END_WAIT;
            }
        }
    }

    private void updateDialogue(float delta) {
        if (charIndex < fullMessage.length()) {
            typewriterTimer += delta;
            if (typewriterTimer >= 1f / charsPerSecond) {
                typewriterTimer = 0f;
                visibleMessage += fullMessage.charAt(charIndex++);

                if (charIndex == fullMessage.length()) {
                    if (currentState == BattleState.WILD_INTRO_MESSAGE) {
                        currentState = BattleState.SEND_OUT_MESSAGE;
                    } else if (currentState == BattleState.BATTLE_MESSAGE_TYPING) {
                        currentState = BattleState.BATTLE_MESSAGE_WAIT;
                    } else if (currentState == BattleState.FAINT_MESSAGE_TYPING) {
                        currentState = BattleState.POST_BATTLE_ACTIONS;
                    }
                }
            }
        } else {
            indicatorTimer += delta;
        }
    }


    private void updateCamera(float delta) {
        cameraPanTimer += delta * 0.2f;
        float panX = (float) Math.sin(cameraPanTimer) * 4f;
        float panY = (float) Math.cos(cameraPanTimer) * 2f + 10;
        battleCamera.position.set(panX, panY, 0);
        battleCamera.update();
    }
    
    @Override
    public void render(float delta) {
        animationTimer += delta;
        fadeManager.update(delta);
        updateStateTransitions(delta);
        updateDialogue(delta);
        updateCamera(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        uiViewport.apply();
        batch.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        batch.draw(background, 0, 0, uiViewport.getWorldWidth(), uiViewport.getWorldHeight());
        batch.end();

        battleViewport.apply();
        batch.setProjectionMatrix(battleCamera.combined);
        batch.begin();
        batch.enableBlending();
        
        if (platform != null) {
            float hoverSpeed = 1.05f, hoverAmplitude = 1.1f;
            float enemyHoverY = (float) Math.sin(animationTimer * hoverSpeed) * hoverAmplitude;
            float playerHoverY = (float) Math.sin((animationTimer * hoverSpeed) + 0.5f) * hoverAmplitude;
            float enemyPlatformCurrentY = enemyPlatformPos.y + enemyHoverY;

            batch.draw(platform, enemyPlatformPos.x - platform.getRegionWidth() / 2f, enemyPlatformCurrentY);
            
            if (wildPokemonAnimation != null) {
                TextureRegion frame = wildPokemonAnimation.getKeyFrame(animationTimer, true);
                Vector2 renderPos = enemyPokemonCurrentPos; 
                if (faintedPokemon == wildPokemon) {
                    float progress = faintAnimationTimer / 0.6f;
                    renderPos.y = Interpolation.pow2In.apply(renderPos.y, -150, progress);
                }
                batch.draw(frame, renderPos.x, renderPos.y);
            }
            
            float playerPlatformCurrentY = playerOnScreenPos.y + playerHoverY;
            batch.draw(platform, playerOnScreenPos.x - platform.getRegionWidth() / 2f, playerPlatformCurrentY);

            if (currentState.ordinal() < BattleState.PLAYER_POKEMON_ENTER.ordinal() || currentState == BattleState.PLAYER_SWITCH_OUT) {
                TextureRegion frame = playerThrowAnimation.getKeyFrame(playerSpriteTimer, false);
                float trainerX = playerTrainerCurrentPos.x - frame.getRegionWidth() / 2f;
                float trainerY = playerPlatformCurrentY + 4;
                batch.draw(frame, trainerX, trainerY);
            } else if (playerPokemonAnimation != null) {
                TextureRegion frame = playerPokemonAnimation.getKeyFrame(animationTimer, true);
                float pokeX = playerPokemonCurrentPos.x - frame.getRegionWidth() / 2f;
                float pokeY = playerPokemonCurrentPos.y + playerPlatformCurrentY - playerOnScreenPos.y + 4;
                if(faintedPokemon == playerPokemon) {
                     pokeY = Interpolation.pow2In.apply(pokeY, -150, faintAnimationTimer / 0.6f);
                }
                batch.draw(frame, pokeX, pokeY);
            }
        }
        batch.end();

        uiViewport.apply();
        batch.setProjectionMatrix(uiCamera.combined);
        
        if (playerPokemon != null && currentState.ordinal() >= BattleState.STATUS_BARS_ENTER.ordinal() && faintedPokemon != playerPokemon) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            float x = playerStatusCurrentPos.x, y = playerStatusCurrentPos.y;
            float barWidth = 64f, barHeight = 2f, barX = x + 32, barY = y + 2;
            shapeRenderer.setColor(Color.GRAY);
            shapeRenderer.rect(barX, barY, barWidth, barHeight);
            shapeRenderer.setColor(Color.CYAN);
            shapeRenderer.rect(barX, barY, barWidth * expAnimCurrentPercentage, barHeight); 
            shapeRenderer.end();
        }


        batch.begin();
        
        if (statusEnemy != null && faintedPokemon != wildPokemon) {
            float x = 10, y = 120;
            float hpPercentage = (currentState == BattleState.HP_BAR_ANIMATING && hpAnimTargetPokemon == wildPokemon) ? 
                Interpolation.linear.apply(hpAnimStart, hpAnimEnd, hpAnimTimer / HP_ANIM_DURATION) / wildPokemon.getMaxHP() :
                (float)wildPokemon.getCurrentHP() / wildPokemon.getMaxHP();
            enemyHealthBar.draw(batch, hpPercentage, x + 39, y + 9);
            
            batch.draw(statusEnemy, x, y);
            String wildName = wildPokemon.getNickname().toUpperCase();
            if (wildPokemon.getGender() == Pokemon.Gender.MALE) wildName += "♂";
            else if (wildPokemon.getGender() == Pokemon.Gender.FEMALE) wildName += "♀";
            statusFont.draw(batch, wildName, x + 4, y + 16);
            statusFont.draw(batch, "" + wildPokemon.getLevel(), x + 74, y + 16);
        }

        if (statusPlayer != null && playerPokemon != null && currentState.ordinal() >= BattleState.STATUS_BARS_ENTER.ordinal() && faintedPokemon != playerPokemon) {
            float x = playerStatusCurrentPos.x, y = playerStatusCurrentPos.y;
            
            float hpPercentage = (currentState == BattleState.HP_BAR_ANIMATING && hpAnimTargetPokemon == playerPokemon) ?
                Interpolation.linear.apply(hpAnimStart, hpAnimEnd, hpAnimTimer / HP_ANIM_DURATION) / playerPokemon.getMaxHP() :
                (float)playerPokemon.getCurrentHP() / playerPokemon.getMaxHP();
            playerHealthBar.draw(batch, hpPercentage, x + 48, y + 16);
            
            batch.draw(statusPlayer, x, y);
            
            String playerName = playerPokemon.getNickname().toUpperCase();
            if (playerPokemon.getGender() == Pokemon.Gender.MALE) playerName += "♂";
            else if (playerPokemon.getGender() == Pokemon.Gender.FEMALE) playerName += "♀";
            statusFont.draw(batch, playerName, x + 15, y + 22);
            
            statusFont.draw(batch, "" + levelToDisplayInHud, x + 22, y + 8);

            String hpText = (currentState == BattleState.HP_BAR_ANIMATING && hpAnimTargetPokemon == playerPokemon) ? 
                (int)Interpolation.linear.apply(hpAnimStart, hpAnimEnd, hpAnimTimer / HP_ANIM_DURATION) + "/" + playerPokemon.getMaxHP() :
                playerPokemon.getCurrentHP() + "/" + playerPokemon.getMaxHP();
            float hpTextWidth = statusFont.getWidth(hpText);
            statusFont.draw(batch, hpText, x + 90 - hpTextWidth, y + 7);
        }

        boolean showTextbox = currentState != BattleState.ACTION_SELECTION && currentState != BattleState.MOVE_SELECTION && currentState != BattleState.BATTLE_START;

        if (currentState == BattleState.ACTION_SELECTION) {
            batch.draw(battleTextbox, 0, 0, uiViewport.getWorldWidth(), battleTextbox.getRegionHeight());
            float actionBoxX = uiViewport.getWorldWidth() - battleActionUi.getRegionWidth();
            batch.draw(battleActionUi, actionBoxX, 0);
            Vector2 arrowPos = actionArrowPositions.get(selectedActionIndex);
            batch.draw(actionArrow, arrowPos.x, arrowPos.y);
            layout.setText(whiteFont, visibleMessage, whiteFont.getColor(), 110, Align.left, true);
            whiteFont.draw(batch, layout, 10, 34);
        } else if (currentState == BattleState.MOVE_SELECTION) {
            drawMoveSelectionUi();
        } else if (showTextbox) {
            batch.draw(battleTextbox, 0, 0, uiViewport.getWorldWidth(), battleTextbox.getRegionHeight());
            layout.setText(whiteFont, visibleMessage, whiteFont.getColor(), uiViewport.getWorldWidth() - 20, Align.left, true);
            whiteFont.draw(batch, layout, 10, 34);
        }
        
        boolean showWaitingIndicator = (charIndex >= fullMessage.length() && 
                                       (currentState == BattleState.BATTLE_MESSAGE_WAIT ||
                                        currentState == BattleState.SEND_OUT_MESSAGE ||
                                        currentState == BattleState.BATTLE_END_WAIT ||
                                        currentState == BattleState.SWITCH_MESSAGE_COME_BACK ||
                                        currentState == BattleState.SWITCH_MESSAGE_GO));
        if (showWaitingIndicator) {
             drawWaitingIndicator();
        }

        drawLevelUpStatBox();
        fadeManager.render(batch, uiViewport.getWorldWidth(), uiViewport.getWorldHeight());
        batch.end();
    }

    private void drawMoveSelectionUi() {
        batch.draw(battleFightUi, 0, 0);
        for (int i = 0; i < displayedMoves.size; i++) {
            Vector2 pos = movePositions.get(i);
            grayFont.draw(batch, displayedMoves.get(i).getName().toUpperCase(), pos.x, pos.y);
        }
        if (displayedMoves.size > 0) {
            Vector2 selectedPos = movePositions.get(selectedMoveIndex);
            batch.draw(actionArrow, selectedPos.x - 4, selectedPos.y - 12);
            Move selectedMove = displayedMoves.get(selectedMoveIndex);
            String ppText = selectedMove.getCurrentPP() + "/" + selectedMove.getMaxPP();
            grayFont.draw(batch, "PP", 185, 35);
            whiteFont.draw(batch, ppText, 205, 35);
            grayFont.draw(batch, "TYPE/", 185, 15);
            TextureRegion typeRegion = typeAtlas.findRegion(selectedMove.getType().name().toLowerCase());
            if (typeRegion != null) batch.draw(typeRegion, 215, 5, 32, 12);
        }
    }

    private void drawWaitingIndicator() {
        if (layout.runs.size > 0 && layout.runs.peek().glyphs.size > 0) {
            GlyphLayout.GlyphRun lastRun = layout.runs.peek();
            float runWidth = 0f;
            for (int i = 0; i < lastRun.xAdvances.size; i++) runWidth += lastRun.xAdvances.get(i);
            float drawX = 10f, drawTopY = 34f;
            float arrowX = drawX + lastRun.x + runWidth + 2f;
            float lastLineBaselineY = drawTopY - layout.height + lastRun.y + grayFont.getCapHeight();
            float arrowSize = whiteFont.getCapHeight() * 0.75f;
            float arrowBaseY = lastLineBaselineY - (arrowSize / 2f);
            float yOffset = (float) Math.sin(indicatorTimer * 8f) * 2f;
            batch.draw(dialogueArrow, arrowX, arrowBaseY + yOffset, arrowSize, arrowSize);
        }
    }

    private void drawLevelUpStatBox() {
        if (!statsBoxVisible || currentLevelUpStats == null) return;

        float boxWidth = 100f, boxHeight = 95f;
        float boxX = uiViewport.getWorldWidth() - boxWidth - 5;
        float boxY = (uiViewport.getWorldHeight() - boxHeight) / 2;
        statBoxPatch.draw(batch, boxX, boxY, boxWidth, boxHeight);
        BitmapFont font = whiteFont;
        BitmapFont valueFont = assetManager.get(assetLoader.INCREASED_STAT_FONT_NAME + ".otf", BitmapFont.class);
        float leftPad = boxX + 8, rightPad = boxX + boxWidth - 8, currentY = boxY + boxHeight - 12;

        font.draw(batch, "MAX. HP", leftPad, currentY);
        layout.setText(valueFont, "+" + currentLevelUpStats.hpIncrease);
        valueFont.draw(batch, layout, rightPad - layout.width, currentY); currentY -= 14;
        font.draw(batch, "ATTACK", leftPad, currentY);
        layout.setText(valueFont, "+" + currentLevelUpStats.attackIncrease);
        valueFont.draw(batch, layout, rightPad - layout.width, currentY); currentY -= 14;
        font.draw(batch, "DEFENSE", leftPad, currentY);
        layout.setText(valueFont, "+" + currentLevelUpStats.defenseIncrease);
        valueFont.draw(batch, layout, rightPad - layout.width, currentY); currentY -= 14;
        font.draw(batch, "SP. ATK", leftPad, currentY);
        layout.setText(valueFont, "+" + currentLevelUpStats.spAttackIncrease);
        valueFont.draw(batch, layout, rightPad - layout.width, currentY); currentY -= 14;
        font.draw(batch, "SP. DEF", leftPad, currentY);
        layout.setText(valueFont, "+" + currentLevelUpStats.spDefenseIncrease);
        valueFont.draw(batch, layout, rightPad - layout.width, currentY); currentY -= 14;
        font.draw(batch, "SPEED", leftPad, currentY);
        layout.setText(valueFont, "+" + currentLevelUpStats.speedIncrease);
        valueFont.draw(batch, layout, rightPad - layout.width, currentY);
    }
    
    @Override
    public void hide() {
        Gdx.input.setInputProcessor(gameScreen.getInputHandler());
    }

    @Override
    public void resize(int width, int height) {
        battleViewport.update(width, height, true);
        uiViewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        playerHealthBar.dispose();
        enemyHealthBar.dispose();
        fadeManager.dispose();
        if (playerPokemon != null) {
            String playerPath = playerPokemon.getBackSpriteGifPath();
            if (assetManager.isLoaded(playerPath)) assetManager.unload(playerPath);
        }
        if (wildPokemon != null) {
            String wildPath = wildPokemon.getFrontSpriteGifPath();
            if (assetManager.isLoaded(wildPath)) assetManager.unload(wildPath);
        }
    }

    public void playerSendsNewPokemon(Pokemon newPokemon, boolean isVoluntary) {
        this.pokemonToSwitchIn = newPokemon;
        this.isVoluntarySwitch = isVoluntary;
        this.faintedPokemon = null;
        setMessage("Muito bem, volte " + playerPokemon.getNickname().toUpperCase() + "!", BattleState.SWITCH_MESSAGE_COME_BACK, BattleState.SWITCH_MESSAGE_COME_BACK);
    }

    public Pokemon getPlayerPokemon() {
        return playerPokemon;
    }
}