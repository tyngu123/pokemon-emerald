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
    private final FadeManager fadeManager; // ## ADICIONADO ##

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
    private final Vector2 playerCurrentPos = new Vector2();
    private final Vector2 pokemonCurrentPos = new Vector2();
    private final Vector2 enemyPlatformPos = new Vector2(80, 20);
    private final Vector2 wildPokemonRenderPos = new Vector2();

    private final Vector2 playerStatusOnScreenPos = new Vector2(130, 50);
    private final Vector2 playerStatusOffScreenPos;
    private final Vector2 playerStatusCurrentPos = new Vector2();

    private enum BattleState {
        WILD_INTRO_TYPING, WILD_INTRO_WAIT,
        GO_MESSAGE_TYPING, GO_MESSAGE_WAIT,
        PLAYER_THROWING, TRANSITION_OUT, STATUS_BAR_ENTERING,
        ACTION_SELECTION, MOVE_SELECTION,
        TURN_PROCESSING,
        BATTLE_MESSAGE,
        HP_ANIMATING,
        POKEMON_FAINTING,
        EXP_MESSAGE,
        EXP_ANIMATING_FILL,
        LEVEL_UP_MESSAGE,
        LEVEL_UP_PAUSE,
        SHOWING_STATS,
        WAIT_FOR_STATS_DISMISS,
        PLAYER_SWITCH_OUT, PLAYER_SWITCH_IN,
        WAIT_FOR_END
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

    private float expAnimStartPercentage;
    private float expAnimEndPercentage;
    private float expAnimTimer;
    private final float EXP_ANIM_DURATION = 1.2f;
    private int expGained;

    private final Queue<Pokemon.LevelUpResult> levelUpQueue = new Queue<>();
    private Pokemon.LevelUpResult currentLevelUpStats;
    private final NinePatch statBoxPatch;
    private boolean statsBoxVisible = false;

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
        this.fadeManager = new FadeManager(); // ## ADICIONADO ##
        
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

        actionArrowPositions.add(new Vector2(128, 25)); // FIGHT
        actionArrowPositions.add(new Vector2(185, 25)); // BAG
        actionArrowPositions.add(new Vector2(128, 9));  // POKEMON
        actionArrowPositions.add(new Vector2(185, 9));  // RUN
        
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
        
        this.playerStatusOffScreenPos = new Vector2(uiViewport.getWorldWidth(), playerStatusOnScreenPos.y);
        this.playerStatusCurrentPos.set(playerStatusOffScreenPos);

        loadPlayerPokemonAnimation();
        loadWildPokemonAnimation();

        playerCurrentPos.set(playerOnScreenPos);
        startIntroMessage();
    }
    
    private void startIntroMessage() {
        setMessage("Wild " + wildPokemon.getNickname().toUpperCase() + "\napareceu!", BattleState.WILD_INTRO_TYPING);
    }
    
    private void startGoMessage() {
        setMessage("Go! " + playerPokemon.getNickname().toUpperCase() + "!", BattleState.GO_MESSAGE_TYPING);
    }
    
    private void startActionMessage() {
        setMessage("O que " + playerPokemon.getNickname().toUpperCase() + "\n vai fazer?", BattleState.ACTION_SELECTION);
    }

    private void setMessage(String message, BattleState nextState) {
        this.fullMessage = message;
        this.visibleMessage = "";
        this.charIndex = 0;
        this.typewriterTimer = 0f;
        this.currentState = nextState;
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

        setMessage(getAttackerName(currentAction.attacker) + " usou\n" + currentAction.move.getName().toUpperCase() + "!", BattleState.BATTLE_MESSAGE);
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
        // ## ADICIONADO: Inicia o fade in quando a tela é exibida ##
        fadeManager.fadeIn(0.5f);
        
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode != Input.Keys.Z && keycode != Input.Keys.X && keycode != Input.Keys.UP && keycode != Input.Keys.DOWN && keycode != Input.Keys.LEFT && keycode != Input.Keys.RIGHT && keycode != Input.Keys.ESCAPE) return false;

                if (currentState == BattleState.TURN_PROCESSING || currentState == BattleState.POKEMON_FAINTING ||
                    currentState == BattleState.HP_ANIMATING || currentState == BattleState.EXP_ANIMATING_FILL ||
                    currentState == BattleState.SHOWING_STATS || currentState == BattleState.PLAYER_SWITCH_OUT ||
                    currentState == BattleState.PLAYER_SWITCH_IN) {
                    return true;
                }
                
                if (currentState == BattleState.WAIT_FOR_END && (keycode == Input.Keys.Z || keycode == Input.Keys.X)) {
                    game.closeBattleScreen();
                    return true;
                }
                
                switch(currentState) {
                    case WILD_INTRO_TYPING:
                    case GO_MESSAGE_TYPING:
                        if (keycode == Input.Keys.Z || keycode == Input.Keys.X) {
                            visibleMessage = fullMessage;
                            charIndex = fullMessage.length();
                            currentState = BattleState.GO_MESSAGE_WAIT;
                        }
                        return true;

                    case BATTLE_MESSAGE:
                        if (keycode == Input.Keys.Z || keycode == Input.Keys.X) {
                            if (charIndex < fullMessage.length()) {
                                visibleMessage = fullMessage;
                                charIndex = fullMessage.length();
                            } else {
                                if (fullMessage.contains("fainted!")) {
                                    if (faintedPokemon == wildPokemon && playerPokemon.getCurrentHP() > 0) {
                                        expGained = BattleCalculator.calculateExperienceGained(playerPokemon, wildPokemon);
                                        String expMessage = playerPokemon.getNickname().toUpperCase() + " gained\n" + expGained + " EXP. Points!";
                                        setMessage(expMessage, BattleState.EXP_MESSAGE);
                                    } else if (faintedPokemon == playerPokemon) {
                                        Pokemon nextPokemon = playerParty.getFirstAvailablePokemon();
                                        if (nextPokemon != null) {
                                            game.setScreen(new PartyScreen(game, gameScreen, batch, assetLoader, BattleScreen.this, true));
                                        } else {
                                            setMessage("You have no more POKEMON to fight!", BattleState.WAIT_FOR_END);
                                        }
                                    } else {
                                        currentState = BattleState.WAIT_FOR_END;
                                    }
                                } else {
                                    TurnAction action = turnQueue.first();
                                    if (action.result == null) {
                                        action.result = BattleCalculator.calculateDamage(action.attacker, action.defender, action.move);
                                        
                                        hpAnimTargetPokemon = action.defender;
                                        hpAnimStart = hpAnimTargetPokemon.getCurrentHP();
                                        action.defender.takeDamage(action.result.damage);
                                        hpAnimEnd = hpAnimTargetPokemon.getCurrentHP();
                                        hpAnimTimer = 0f;
                                        currentState = BattleState.HP_ANIMATING;

                                        if (action.result.isCritical) messageQueue.addLast("A critical hit!");
                                        if (action.result.effectiveness > 1.5f) messageQueue.addLast("It's super effective!");
                                        if (action.result.effectiveness < 1.0f && action.result.effectiveness > 0) messageQueue.addLast("It's not very effective...");
                                    } else {
                                        if (!messageQueue.isEmpty()) {
                                            setMessage(messageQueue.removeFirst(), BattleState.BATTLE_MESSAGE);
                                        } else {
                                            if (action.defender.getCurrentHP() <= 0) {
                                                faintedPokemon = action.defender;
                                                currentState = BattleState.POKEMON_FAINTING;
                                                faintAnimationTimer = 0f;
                                            } else {
                                                turnQueue.removeFirst();
                                                processNextTurnAction();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        return true;

                    case EXP_MESSAGE:
                        if (keycode == Input.Keys.Z || keycode == Input.Keys.X) {
                            if (charIndex < fullMessage.length()) {
                                visibleMessage = fullMessage;
                                charIndex = fullMessage.length();
                            } else {
                                int totalExpForCurrentLevel = ExperienceCalculator.getTotalExpForLevel(playerPokemon.getLevel(), playerPokemon.getExperienceGroup());
                                int expInCurrentLevel = playerPokemon.getExperiencePoints() - totalExpForCurrentLevel;
                                int expNeededForNextLevel = ExperienceCalculator.getExpToNextLevel(playerPokemon.getLevel(), playerPokemon.getExperienceGroup());
                                expAnimStartPercentage = (expNeededForNextLevel > 0) ? (float)expInCurrentLevel / expNeededForNextLevel : 1f;

                                Array<Pokemon.LevelUpResult> levelUpResults = playerPokemon.gainExperience(expGained);
                                levelUpQueue.clear();
                                for(Pokemon.LevelUpResult res : levelUpResults) {
                                    levelUpQueue.addLast(res);
                                }

                                if (levelUpQueue.size > 0) {
                                    expAnimEndPercentage = 1f;
                                } else {
                                   totalExpForCurrentLevel = ExperienceCalculator.getTotalExpForLevel(playerPokemon.getLevel(), playerPokemon.getExperienceGroup());
                                   expInCurrentLevel = playerPokemon.getExperiencePoints() - totalExpForCurrentLevel;
                                   expNeededForNextLevel = playerPokemon.getExperienceToNextLevel();
                                   expAnimEndPercentage = (expNeededForNextLevel > 0) ? (float)expInCurrentLevel / expNeededForNextLevel : 1f;
                                }
                                currentState = BattleState.EXP_ANIMATING_FILL;
                                expAnimTimer = 0f;
                            }
                        }
                        return true;
                    
                    case LEVEL_UP_MESSAGE:
                        if (keycode == Input.Keys.Z || keycode == Input.Keys.X) {
                             if (charIndex < fullMessage.length()) {
                                visibleMessage = fullMessage;
                                charIndex = fullMessage.length();
                            } else {
                                currentState = BattleState.LEVEL_UP_PAUSE;
                            }
                        }
                        return true;

                    case WAIT_FOR_STATS_DISMISS:
                        if (keycode == Input.Keys.Z || keycode == Input.Keys.X) {
                            statsBoxVisible = false;
                            currentLevelUpStats = null;
                            if (levelUpQueue.size > 0) {
                                currentState = BattleState.LEVEL_UP_PAUSE;
                            } else {
                                currentState = BattleState.WAIT_FOR_END;
                            }
                        }
                        return true;

                    case WILD_INTRO_WAIT:
                        if (keycode == Input.Keys.Z || keycode == Input.Keys.X) startGoMessage();
                        return true;

                    case GO_MESSAGE_WAIT:
                        if (keycode == Input.Keys.Z || keycode == Input.Keys.X) {
                            if (isVoluntarySwitch) {
                                currentState = BattleState.PLAYER_SWITCH_IN;
                                transitionTimer = 0f;
                            } else {
                                currentState = BattleState.PLAYER_THROWING;
                                playerSpriteTimer = 0f;
                            }
                        }
                        return true;
                        
                    case ACTION_SELECTION:
                        return handleActionSelectionInput(keycode);
                        
                    case MOVE_SELECTION:
                        return handleMoveSelectionInput(keycode);
                }
                return false;
            }
        });
    }
    
    private boolean handleActionSelectionInput(int keycode) {
        switch (keycode) {
            case Input.Keys.UP:
                if (selectedActionIndex == 2) selectedActionIndex = 0;
                if (selectedActionIndex == 3) selectedActionIndex = 1;
                return true;
            case Input.Keys.DOWN:
                if (selectedActionIndex == 0) selectedActionIndex = 2;
                if (selectedActionIndex == 1) selectedActionIndex = 3;
                return true;
            case Input.Keys.LEFT:
                if (selectedActionIndex == 1) selectedActionIndex = 0;
                if (selectedActionIndex == 3) selectedActionIndex = 2;
                return true;
            case Input.Keys.RIGHT:
                if (selectedActionIndex == 0) selectedActionIndex = 1;
                if (selectedActionIndex == 2) selectedActionIndex = 3;
                return true;
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
            case Input.Keys.UP:
                if (selectedMoveIndex >= 2) selectedMoveIndex -= 2;
                break;
            case Input.Keys.DOWN:
                if (selectedMoveIndex < 2 && selectedMoveIndex + 2 < moveCount) selectedMoveIndex += 2;
                break;
            case Input.Keys.LEFT:
                if (selectedMoveIndex == 1 || selectedMoveIndex == 3) selectedMoveIndex -= 1;
                break;
            case Input.Keys.RIGHT:
                if ((selectedMoveIndex == 0 || selectedMoveIndex == 2) && selectedMoveIndex + 1 < moveCount) selectedMoveIndex += 1;
                break;
            case Input.Keys.X:
            case Input.Keys.ESCAPE:
                startActionMessage();
                return true;
            case Input.Keys.Z:
                if(currentState == BattleState.MOVE_SELECTION) {
                    lastSelectedMoveIndex = selectedMoveIndex;
                    Move selectedMove = displayedMoves.get(selectedMoveIndex);
                    executeTurn(selectedMove);
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
            case PLAYER_THROWING:
                playerSpriteTimer += delta;
                if (playerThrowAnimation.isAnimationFinished(playerSpriteTimer)) {
                    currentState = BattleState.TRANSITION_OUT;
                    transitionTimer = 0f;
                    pokemonCurrentPos.set(playerOffScreenPos);
                }
                break;
            case TRANSITION_OUT:
                transitionTimer += delta * transitionSpeed;
                playerCurrentPos.interpolate(playerOffScreenPos, transitionTimer, Interpolation.exp5In);
                pokemonCurrentPos.interpolate(playerOnScreenPos, transitionTimer, Interpolation.exp5Out);
                if (transitionTimer >= 1.0f) {
                    playerCurrentPos.set(playerOffScreenPos);
                    pokemonCurrentPos.set(playerOnScreenPos);
                    currentState = BattleState.STATUS_BAR_ENTERING;
                    transitionTimer = 0f;
                }
                break;
            case STATUS_BAR_ENTERING:
                transitionTimer += delta * transitionSpeed;
                playerStatusCurrentPos.interpolate(playerStatusOnScreenPos, transitionTimer, Interpolation.sineOut);
                if(transitionTimer >= 1.0f) {
                    playerStatusCurrentPos.set(playerStatusOnScreenPos);
                    startActionMessage();
                }
                break;
            case HP_ANIMATING:
                hpAnimTimer += delta;
                if (hpAnimTimer >= HP_ANIM_DURATION) {
                    if (messageQueue.isEmpty()) {
                        TurnAction action = turnQueue.first();
                        if (action.defender.getCurrentHP() <= 0) {
                            faintedPokemon = action.defender;
                            currentState = BattleState.POKEMON_FAINTING;
                            faintAnimationTimer = 0f;
                        } else {
                            turnQueue.removeFirst();
                            processNextTurnAction();
                        }
                    } else {
                        setMessage(messageQueue.removeFirst(), BattleState.BATTLE_MESSAGE);
                    }
                }
                break;
            case POKEMON_FAINTING:
                faintAnimationTimer += delta;
                float duration = 0.6f;
                if (faintAnimationTimer >= duration) {
                    setMessage(getAttackerName(faintedPokemon) + " fainted!", BattleState.BATTLE_MESSAGE);
                }
                break;
            case EXP_ANIMATING_FILL:
                expAnimTimer += delta;
                if (expAnimTimer >= EXP_ANIM_DURATION) {
                    if (levelUpQueue.size > 0) {
                        Pokemon.LevelUpResult nextLevel = levelUpQueue.first();
                        setMessage(playerPokemon.getNickname().toUpperCase() + " grew to\nLV. " + nextLevel.newLevel + "!", BattleState.LEVEL_UP_MESSAGE);
                    } else {
                        currentState = BattleState.WAIT_FOR_END;
                    }
                }
                break;
            case LEVEL_UP_MESSAGE:
                if (charIndex >= fullMessage.length()) {
                    if (Gdx.input.isKeyJustPressed(Input.Keys.Z) || Gdx.input.isKeyJustPressed(Input.Keys.X)) {
                         currentState = BattleState.LEVEL_UP_PAUSE;
                    }
                }
                break;
            case LEVEL_UP_PAUSE:
                currentLevelUpStats = levelUpQueue.removeFirst();
                expAnimTimer = 0f;
                if (levelUpQueue.size > 0) {
                    expAnimStartPercentage = 0f;
                    expAnimEndPercentage = 1f;
                    currentState = BattleState.EXP_ANIMATING_FILL;
                } else {
                    currentState = BattleState.SHOWING_STATS;
                }
                break;
            case SHOWING_STATS:
                expAnimTimer += delta;
                if (expAnimTimer > 0.2f) {
                    statsBoxVisible = true;
                    currentState = BattleState.WAIT_FOR_STATS_DISMISS;
                }
                break;
            case PLAYER_SWITCH_OUT:
                transitionTimer += delta * transitionSpeed * 1.5f;
                pokemonCurrentPos.interpolate(playerOffScreenPos, transitionTimer, Interpolation.exp5In);
                if (transitionTimer >= 1f) {
                    playerPokemon = pokemonToSwitchIn;
                    loadPlayerPokemonAnimation();
                    pokemonToSwitchIn = null;
                    startGoMessage();
                }
                break;
            case PLAYER_SWITCH_IN:
                transitionTimer += delta * transitionSpeed * 1.5f;
                pokemonCurrentPos.interpolate(playerOnScreenPos, transitionTimer, Interpolation.exp5Out);
                if(transitionTimer >= 1f) {
                    if (isVoluntarySwitch) {
                        isVoluntarySwitch = false; 
                        turnQueue.clear();
                        Move enemyMove = wildPokemon.getMoves().random();
                        turnQueue.addLast(new TurnAction(wildPokemon, playerPokemon, enemyMove));
                        processNextTurnAction();
                    } else {
                        startActionMessage();
                    }
                }
                break;
            default:
                break;
        }
    }

    private void updateDialogue(float delta) {
        if (charIndex < fullMessage.length()) {
            boolean isTypingState = currentState == BattleState.WILD_INTRO_TYPING || 
                                    currentState == BattleState.GO_MESSAGE_TYPING ||
                                    currentState == BattleState.ACTION_SELECTION ||
                                    currentState == BattleState.BATTLE_MESSAGE ||
                                    currentState == BattleState.EXP_MESSAGE ||
                                    currentState == BattleState.LEVEL_UP_MESSAGE ||
                                    currentState == BattleState.WAIT_FOR_END;
            if (isTypingState) {
                typewriterTimer += delta;
                if (typewriterTimer >= 1f / charsPerSecond) {
                    typewriterTimer = 0f;
                    visibleMessage += fullMessage.charAt(charIndex++);
                }
            }
        } 
        else {
            if (currentState == BattleState.WILD_INTRO_TYPING) {
                currentState = BattleState.WILD_INTRO_WAIT;
            } else if (currentState == BattleState.GO_MESSAGE_TYPING) {
                currentState = BattleState.GO_MESSAGE_WAIT;
            }

            if (currentState == BattleState.WILD_INTRO_WAIT || 
                currentState == BattleState.WAIT_FOR_END) {
                indicatorTimer += delta;
            }
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
        fadeManager.update(delta); // ## ADICIONADO ##
        updateDialogue(delta);
        updateCamera(delta);
        updateStateTransitions(delta);

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
            float hoverSpeed = 1.05f;
            float hoverAmplitude = 1.1f;
            float enemyHoverY = (float) Math.sin(animationTimer * hoverSpeed) * hoverAmplitude;
            float playerHoverY = (float) Math.sin((animationTimer * hoverSpeed) + 0.5f) * hoverAmplitude;
            float enemyPlatformCurrentY = enemyPlatformPos.y + enemyHoverY;

            batch.draw(platform, enemyPlatformPos.x - platform.getRegionWidth() / 2f, enemyPlatformCurrentY);
            
            if (wildPokemonAnimation != null) {
                if (faintedPokemon != wildPokemon) {
                    TextureRegion frame = wildPokemonAnimation.getKeyFrame(animationTimer, true);
                    wildPokemonRenderPos.set(enemyPlatformPos.x - frame.getRegionWidth() / 2f, enemyPlatformCurrentY + 10);
                    batch.draw(frame, wildPokemonRenderPos.x, wildPokemonRenderPos.y);
                } else {
                    TextureRegion frame = wildPokemonAnimation.getKeyFrame(animationTimer, true);
                    float progress = faintAnimationTimer / 0.6f;
                    float y = Interpolation.pow2In.apply(wildPokemonRenderPos.y, -150, progress);
                    batch.draw(frame, wildPokemonRenderPos.x, y);
                }
            }
            
            float playerPlatformCurrentY = playerOnScreenPos.y + playerHoverY;
            batch.draw(platform, playerOnScreenPos.x - platform.getRegionWidth() / 2f, playerPlatformCurrentY);

            if (currentState.ordinal() < BattleState.TRANSITION_OUT.ordinal() || currentState == BattleState.PLAYER_SWITCH_OUT) {
                 if (currentState != BattleState.PLAYER_SWITCH_OUT) {
                    TextureRegion frame = playerThrowAnimation.getKeyFrame(playerSpriteTimer, false);
                    float trainerX = playerCurrentPos.x - frame.getRegionWidth() / 2f;
                    float trainerY = playerPlatformCurrentY + 4;
                    batch.draw(frame, trainerX, trainerY);
                 }
            } else {
                if (playerPokemonAnimation != null) {
                     if(faintedPokemon != playerPokemon) {
                        TextureRegion frame = playerPokemonAnimation.getKeyFrame(animationTimer, true);
                        float pokeX = pokemonCurrentPos.x - frame.getRegionWidth() / 2f;
                        float pokeY = pokemonCurrentPos.y + playerPlatformCurrentY - playerOnScreenPos.y + 4;
                        batch.draw(frame, pokeX, pokeY);
                    } else {
                         TextureRegion frame = playerPokemonAnimation.getKeyFrame(animationTimer, true);
                         float pokeX = pokemonCurrentPos.x - frame.getRegionWidth() / 2f;
                         float initialY = pokemonCurrentPos.y + playerPlatformCurrentY - playerOnScreenPos.y + 4;
                         float progress = faintAnimationTimer / 0.6f;
                         float y = Interpolation.pow2In.apply(initialY, -150, progress);
                         batch.draw(frame, pokeX, y);
                    }
                }
            }
        }
        batch.end();

        uiViewport.apply();
        batch.setProjectionMatrix(uiCamera.combined);
        
        if (playerPokemon != null && currentState.ordinal() >= BattleState.STATUS_BAR_ENTERING.ordinal() && faintedPokemon != playerPokemon) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            
            float x = playerStatusCurrentPos.x; float y = playerStatusCurrentPos.y;
            float barWidth = 64f; float barHeight = 2f;
            float barX = x + 32; float barY = y + 2;
            
            shapeRenderer.setColor(Color.GRAY);
            shapeRenderer.rect(barX, barY, barWidth, barHeight);
            
            float progressPercentage;
            if(currentState == BattleState.EXP_ANIMATING_FILL) {
                float progress = Math.min(1f, expAnimTimer / EXP_ANIM_DURATION);
                progressPercentage = Interpolation.linear.apply(expAnimStartPercentage, expAnimEndPercentage, progress);
            } else {
                int totalExpForCurrentLevel = ExperienceCalculator.getTotalExpForLevel(playerPokemon.getLevel(), playerPokemon.getExperienceGroup());
                int expInCurrentLevel = playerPokemon.getExperiencePoints() - totalExpForCurrentLevel;
                int expNeededForNextLevel = playerPokemon.getExperienceToNextLevel();
                progressPercentage = (expNeededForNextLevel > 0) ? (float) expInCurrentLevel / expNeededForNextLevel : 0f;
            }
            
            shapeRenderer.setColor(Color.CYAN);
            shapeRenderer.rect(barX, barY, barWidth * progressPercentage, barHeight);
            
            shapeRenderer.end();
        }

        batch.begin();
        
        if (statusEnemy != null && faintedPokemon != wildPokemon) {
            float x = 10; float y = 120;
            if (currentState == BattleState.HP_ANIMATING && hpAnimTargetPokemon == wildPokemon) {
                float progress = hpAnimTimer / HP_ANIM_DURATION;
                float animatedHp = Interpolation.linear.apply(hpAnimStart, hpAnimEnd, progress);
                enemyHealthBar.draw(batch, animatedHp / wildPokemon.getMaxHP(), x + 39, y + 9);
            } else {
                enemyHealthBar.draw(batch, wildPokemon, x + 39, y + 9);
            }
            batch.draw(statusEnemy, x, y);
            String wildName = wildPokemon.getNickname().toUpperCase();
            if (wildPokemon.getGender() == Pokemon.Gender.MALE) wildName += "♂";
            else if (wildPokemon.getGender() == Pokemon.Gender.FEMALE) wildName += "♀";
            statusFont.draw(batch, wildName, x + 4, y + 16);
            statusFont.draw(batch, "" + wildPokemon.getLevel(), x + 74, y + 16);
        }

        if (statusPlayer != null && playerPokemon != null && currentState.ordinal() >= BattleState.STATUS_BAR_ENTERING.ordinal() && faintedPokemon != playerPokemon) {
            float x = playerStatusCurrentPos.x; float y = playerStatusCurrentPos.y;
            
            if (currentState == BattleState.HP_ANIMATING && hpAnimTargetPokemon == playerPokemon) {
                float progress = hpAnimTimer / HP_ANIM_DURATION;
                float animatedHp = Interpolation.linear.apply(hpAnimStart, hpAnimEnd, progress);
                playerHealthBar.draw(batch, animatedHp / playerPokemon.getMaxHP(), x + 48, y + 16);
            } else {
                playerHealthBar.draw(batch, playerPokemon, x + 48, y + 16);
            }
            batch.draw(statusPlayer, x, y);
            
            String playerName = playerPokemon.getNickname().toUpperCase();
            if (playerPokemon.getGender() == Pokemon.Gender.MALE) playerName += "♂";
            else if (playerPokemon.getGender() == Pokemon.Gender.FEMALE) playerName += "♀";
            statusFont.draw(batch, playerName, x + 15, y + 22);
            statusFont.draw(batch, "" + playerPokemon.getLevel(), x + 22, y + 8);
            
            String hpText = (currentState == BattleState.HP_ANIMATING && hpAnimTargetPokemon == playerPokemon) ? 
                            (int)Interpolation.linear.apply(hpAnimStart, hpAnimEnd, hpAnimTimer / HP_ANIM_DURATION) + "/" + playerPokemon.getMaxHP() :
                            playerPokemon.getCurrentHP() + "/" + playerPokemon.getMaxHP();
            float hpTextWidth = statusFont.getWidth(hpText);
            statusFont.draw(batch, hpText, x + 90 - hpTextWidth, y + 7);
        }

        boolean showTextbox = currentState != BattleState.ACTION_SELECTION && 
                              currentState != BattleState.MOVE_SELECTION;

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
        
        if (currentState == BattleState.WILD_INTRO_WAIT || currentState == BattleState.WAIT_FOR_END || (charIndex >= fullMessage.length() && (currentState == BattleState.BATTLE_MESSAGE || currentState == BattleState.EXP_MESSAGE || currentState == BattleState.LEVEL_UP_MESSAGE))) {
             drawWaitingIndicator();
        }

        drawLevelUpStatBox();
        
        // ## ADICIONADO: Renderiza o fade in/out sobre toda a tela ##
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
            float arrowX = selectedPos.x - 4; 
            float arrowY = selectedPos.y - 12;
            batch.draw(actionArrow, arrowX, arrowY);
            
            Move selectedMove = displayedMoves.get(selectedMoveIndex);
            
            String ppText = selectedMove.getCurrentPP() + "/" + selectedMove.getMaxPP();
            layout.setText(grayFont, "PP");
            grayFont.draw(batch, "PP", 185, 35);
            whiteFont.draw(batch, ppText, 205, 35);

            layout.setText(grayFont, "TIPO/");
            grayFont.draw(batch, "TIPO/", 185, 15);
            TextureRegion typeRegion = typeAtlas.findRegion(selectedMove.getType().name().toLowerCase());
            if (typeRegion != null) {
                batch.draw(typeRegion, 215, 5, 32, 12);
            }
        }
    }

    private void drawWaitingIndicator() {
        if (layout.runs.size > 0 && layout.runs.peek().glyphs.size > 0) {
            GlyphLayout.GlyphRun lastRun = layout.runs.peek();
            float runWidth = 0f;
            for (int i = 0; i < lastRun.xAdvances.size; i++) {
                runWidth += lastRun.xAdvances.get(i);
            }
            float drawX = 10f;
            float drawTopY = 34f;
            float arrowX = drawX + lastRun.x + runWidth + 2f;
            float lastLineBaselineY = drawTopY - layout.height + lastRun.y + grayFont.getCapHeight();
            float arrowSize = whiteFont.getCapHeight() * 0.75f;
            float arrowBaseY = lastLineBaselineY - (arrowSize / 2f);
            float speed = 8f;
            float amplitude = 2f;
            float yOffset = (float) Math.sin(indicatorTimer * speed) * amplitude;
            float arrowY = arrowBaseY + yOffset;
            batch.draw(dialogueArrow, arrowX, arrowY, arrowSize, arrowSize);
        }
    }

    private void drawLevelUpStatBox() {
        if (!statsBoxVisible || currentLevelUpStats == null) return;

        float boxWidth = 100f;
        float boxHeight = 95f;
        float boxX = uiViewport.getWorldWidth() - boxWidth - 5;
        float boxY = (uiViewport.getWorldHeight() - boxHeight) / 2;

        statBoxPatch.draw(batch, boxX, boxY, boxWidth, boxHeight);

        BitmapFont font = whiteFont;
        BitmapFont valueFont = assetManager.get(assetLoader.INCREASED_STAT_FONT_NAME + ".otf", BitmapFont.class);

        float leftPad = boxX + 8;
        float rightPad = boxX + boxWidth - 8;
        float currentY = boxY + boxHeight - 12;

        font.draw(batch, "MAX. HP", leftPad, currentY);
        layout.setText(valueFont, "+" + currentLevelUpStats.hpIncrease);
        valueFont.draw(batch, layout, rightPad - layout.width, currentY);
        currentY -= 14;

        font.draw(batch, "ATTACK", leftPad, currentY);
        layout.setText(valueFont, "+" + currentLevelUpStats.attackIncrease);
        valueFont.draw(batch, layout, rightPad - layout.width, currentY);
        currentY -= 14;

        font.draw(batch, "DEFENSE", leftPad, currentY);
        layout.setText(valueFont, "+" + currentLevelUpStats.defenseIncrease);
        valueFont.draw(batch, layout, rightPad - layout.width, currentY);
        currentY -= 14;

        font.draw(batch, "SP. ATK", leftPad, currentY);
        layout.setText(valueFont, "+" + currentLevelUpStats.spAttackIncrease);
        valueFont.draw(batch, layout, rightPad - layout.width, currentY);
        currentY -= 14;
        
        font.draw(batch, "SP. DEF", leftPad, currentY);
        layout.setText(valueFont, "+" + currentLevelUpStats.spDefenseIncrease);
        valueFont.draw(batch, layout, rightPad - layout.width, currentY);
        currentY -= 14;

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
        fadeManager.dispose(); // ## ADICIONADO ##
        if (playerPokemon != null) {
            String playerPath = playerPokemon.getBackSpriteGifPath();
            if (assetManager.isLoaded(playerPath)) {
                assetManager.unload(playerPath);
            }
        }
        if (wildPokemon != null) {
            String wildPath = wildPokemon.getFrontSpriteGifPath();
            if (assetManager.isLoaded(wildPath)) {
                assetManager.unload(wildPath);
            }
        }
    }

    public void playerSendsNewPokemon(Pokemon newPokemon, boolean isVoluntary) {
        this.pokemonToSwitchIn = newPokemon;
        this.isVoluntarySwitch = isVoluntary;
        
        this.faintedPokemon = null;
        this.currentState = BattleState.PLAYER_SWITCH_OUT;
        this.transitionTimer = 0f;
    }

    public Pokemon getPlayerPokemon() {
        return playerPokemon;
    }
}