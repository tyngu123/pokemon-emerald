package br.com.yago.emerald.view;

import br.com.yago.emerald.EmeraldGame;
import br.com.yago.emerald.assets.AssetsLoader;
import br.com.yago.emerald.model.ExperienceCalculator;
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
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

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
    private final AtlasFont statusFont; 
    private final GlyphLayout layout = new GlyphLayout();
    private final AssetManager assetManager;
    private final Texture background;
    private final TextureRegion battleTextbox;
    private final Texture dialogueArrow;
    private final TextureRegion platform;
    private final TextureRegion statusEnemy;
    private final TextureRegion statusPlayer;
    private final HealthBar playerHealthBar;
    private final HealthBar enemyHealthBar;
    private final Animation<TextureRegion> playerThrowAnimation;

    // ## NOVOS RECURSOS DO MENU DE AÇÃO ##
    private final TextureRegion battleActionUi;
    private final TextureRegion actionArrow;

    private final Pokemon wildPokemon;
    private final Pokemon playerPokemon;
    private Animation<TextureRegion> playerPokemonAnimation;
    private Animation<TextureRegion> wildPokemonAnimation;
    private float animationTimer = 0f;
    private float cameraPanTimer = 0f;
    private float playerSpriteTimer = 0f;
    private float transitionTimer = 0f;

    private final Vector2 playerOnScreenPos = new Vector2(-80, -25);
    private final Vector2 playerOffScreenPos = new Vector2(-160, -25);
    private final Vector2 playerCurrentPos = new Vector2();
    private final Vector2 pokemonCurrentPos = new Vector2();
    private final Vector2 enemyPlatformPos = new Vector2(80, 20);

    private final Vector2 playerStatusOnScreenPos = new Vector2(130, 50);
    private final Vector2 playerStatusOffScreenPos;
    private final Vector2 playerStatusCurrentPos = new Vector2();

    // ## MÁQUINA DE ESTADO ATUALIZADA ##
    private enum BattleState {
        WILD_INTRO_TYPING,
        WILD_INTRO_WAIT,
        GO_MESSAGE_TYPING,
        GO_MESSAGE_WAIT,
        PLAYER_THROWING,
        TRANSITION_OUT,
        STATUS_BAR_ENTERING,
        ACTION_SELECTION // Novo estado para o menu de ação
    }
    private BattleState currentState;

    private String fullMessage = "";
    private String visibleMessage = "";
    private int charIndex = 0;
    private float typewriterTimer = 0f;
    private final float charsPerSecond = 40f;
    private float indicatorTimer = 0f;

    // ## LÓGICA DO MENU DE AÇÃO ##
    private int selectedActionIndex = 0; // 0:FIGHT, 1:BAG, 2:POKEMON, 3:RUN
    private final Array<Vector2> actionArrowPositions = new Array<>();

    public BattleScreen(EmeraldGame game, GameScreen gameScreen, SpriteBatch batch, AssetsLoader assetLoader, Party playerParty, Pokemon wildPokemon) {
        this.game = game;
        this.gameScreen = gameScreen;
        this.batch = batch;
        this.shapeRenderer = new ShapeRenderer();
        
        this.battleCamera = new OrthographicCamera();
        this.battleViewport = new ExtendViewport(240 * 1f, 160 * 1f, battleCamera);
        this.uiCamera = new OrthographicCamera();
        this.uiViewport = new ExtendViewport(240, 160, uiCamera);
        
        this.assetManager = assetLoader.manager;
        
        this.background = assetManager.get(assetLoader.BATTLE_BG, Texture.class);

        this.whiteFont = assetManager.get(assetLoader.PARTY_FONT_NAME + ".otf", BitmapFont.class);
        this.dialogueArrow = assetManager.get(assetLoader.DIALOGUE_ARROW, Texture.class);
        
        TextureAtlas battleAtlas = assetManager.get(assetLoader.BATTLE_ATLAS, TextureAtlas.class);
        this.battleTextbox = battleAtlas.findRegion("battle_textbox");
        this.platform = battleAtlas.findRegion("plataform");
        this.statusEnemy = battleAtlas.findRegion("status_enemy");
        this.statusPlayer = battleAtlas.findRegion("status_player");
        
        // ## CARREGANDO AS TEXTURAS DO MENU ##
        this.battleActionUi = battleAtlas.findRegion("battle_action_ui");
        this.actionArrow = battleAtlas.findRegion("action_arrow");

        // ## DEFININDO POSIÇÕES DA SETA DE AÇÃO ##
        actionArrowPositions.add(new Vector2(128, 25)); // FIGHT
        actionArrowPositions.add(new Vector2(185, 25)); // BAG
        actionArrowPositions.add(new Vector2(128, 9));  // POKEMON
        actionArrowPositions.add(new Vector2(185, 9));  // RUN

        Array<TextureRegion> throwFrames = new Array<>();
        for (int i = 1; i <= 5; i++) {
            throwFrames.add(battleAtlas.findRegion("player_back" + i));
        }
        this.playerThrowAnimation = new Animation<>(0.1f, throwFrames, Animation.PlayMode.NORMAL);

        TextureAtlas alphabetAtlas = assetManager.get(assetLoader.ALPHABET_ATLAS, TextureAtlas.class);
        this.statusFont = new AtlasFont(alphabetAtlas, 1f, 1);
        
        this.playerHealthBar = new HealthBar(48, 3);
        this.enemyHealthBar = new HealthBar(48, 3);
        
        this.wildPokemon = wildPokemon;
        this.playerPokemon = playerParty.getPartySize() > 0 ? playerParty.getPokemon(0) : null;
        
        this.playerStatusOffScreenPos = new Vector2(uiViewport.getWorldWidth(), playerStatusOnScreenPos.y);
        this.playerStatusCurrentPos.set(playerStatusOffScreenPos);

        loadPlayerPokemonAnimation();
        loadWildPokemonAnimation();

        playerCurrentPos.set(playerOnScreenPos);
        startIntroMessage();
    }
    
    private void startIntroMessage() {
        setMessage("Um " + wildPokemon.getNickname().toUpperCase() + " selvagem apareceu!", BattleState.WILD_INTRO_TYPING);
    }
    
    private void startGoMessage() {
        setMessage("Vai " + playerPokemon.getNickname().toUpperCase() + "!", BattleState.GO_MESSAGE_TYPING);
    }
    
    // ## NOVO MÉTODO PARA A MENSAGEM DE AÇÃO ##
    private void startActionMessage() {
        setMessage("What will\n" + playerPokemon.getNickname().toUpperCase() + " do?", BattleState.ACTION_SELECTION);
    }

    private void setMessage(String message, BattleState nextState) {
        this.fullMessage = message;
        this.visibleMessage = "";
        this.charIndex = 0;
        this.typewriterTimer = 0f;
        this.currentState = nextState;
    }
    
    private void loadPlayerPokemonAnimation() {
        if (playerPokemon == null) return;
        String path = playerPokemon.getBackSpriteGifPath();
        if (!assetManager.isLoaded(path, Animation.class)) {
            AnimationParameters params = new AnimationParameters();
            assetManager.load(path, Animation.class, params);
            assetManager.finishLoading();
        }
        this.playerPokemonAnimation = assetManager.get(path, Animation.class);
    }

    private void loadWildPokemonAnimation() {
        if (wildPokemon == null) return;
        String path = wildPokemon.getFrontSpriteGifPath();
        if (!assetManager.isLoaded(path, Animation.class)) {
            AnimationParameters params = new AnimationParameters();
            assetManager.load(path, Animation.class, params);
            assetManager.finishLoading();
        }
        this.wildPokemonAnimation = assetManager.get(path, Animation.class);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                // ## LÓGICA DE INPUT ATUALIZADA ##
                if (currentState == BattleState.WILD_INTRO_TYPING) {
                    if (keycode == Input.Keys.Z || keycode == Input.Keys.X) {
                        visibleMessage = fullMessage;
                        charIndex = fullMessage.length();
                        currentState = BattleState.WILD_INTRO_WAIT;
                        return true;
                    }
                } else if (currentState == BattleState.WILD_INTRO_WAIT) {
                    if (keycode == Input.Keys.Z || keycode == Input.Keys.X) {
                        startGoMessage();
                        return true;
                    }
                } else if (currentState == BattleState.GO_MESSAGE_TYPING) {
                     if (keycode == Input.Keys.Z || keycode == Input.Keys.X) {
                        visibleMessage = fullMessage;
                        charIndex = fullMessage.length();
                        currentState = BattleState.GO_MESSAGE_WAIT;
                        return true;
                    }
                } else if (currentState == BattleState.GO_MESSAGE_WAIT) {
                    if (keycode == Input.Keys.Z || keycode == Input.Keys.X) {
                        currentState = BattleState.PLAYER_THROWING;
                        playerSpriteTimer = 0f;
                        return true;
                    }
                } else if (currentState == BattleState.ACTION_SELECTION) {
                    switch (keycode) {
                        case Input.Keys.UP:
                            if (selectedActionIndex == 2) selectedActionIndex = 0; // POKEMON -> FIGHT
                            if (selectedActionIndex == 3) selectedActionIndex = 1; // RUN -> BAG
                            return true;
                        case Input.Keys.DOWN:
                            if (selectedActionIndex == 0) selectedActionIndex = 2; // FIGHT -> POKEMON
                            if (selectedActionIndex == 1) selectedActionIndex = 3; // BAG -> RUN
                            return true;
                        case Input.Keys.LEFT:
                            if (selectedActionIndex == 1) selectedActionIndex = 0; // BAG -> FIGHT
                            if (selectedActionIndex == 3) selectedActionIndex = 2; // RUN -> POKEMON
                            return true;
                        case Input.Keys.RIGHT:
                            if (selectedActionIndex == 0) selectedActionIndex = 1; // FIGHT -> BAG
                            if (selectedActionIndex == 2) selectedActionIndex = 3; // POKEMON -> RUN
                            return true;
                        case Input.Keys.Z:
                            // Aqui você adicionaria a lógica para cada ação (lutar, usar item, etc.)
                            Gdx.app.log("BattleScreen", "Ação selecionada: " + selectedActionIndex);
                            // Por enquanto, vamos fechar a tela para teste
                            game.closeBattleScreen();
                            return true;
                    }
                }
                return false;
            }
        });
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
                    // ## TRANSIÇÃO PARA A SELEÇÃO DE AÇÃO ##
                    startActionMessage();
                }
                break;
            default:
                break;
        }
    }

    private void updateDialogue(float delta) {
        // ## LÓGICA ATUALIZADA PARA INCLUIR O NOVO ESTADO ##
        if (currentState == BattleState.WILD_INTRO_TYPING || 
            currentState == BattleState.GO_MESSAGE_TYPING ||
            currentState == BattleState.ACTION_SELECTION) {
                
            typewriterTimer += delta;
            if (typewriterTimer >= 1f / charsPerSecond) {
                typewriterTimer = 0f;
                if (charIndex < fullMessage.length()) {
                    visibleMessage += fullMessage.charAt(charIndex++);
                } else {
                    if (currentState == BattleState.WILD_INTRO_TYPING) {
                        currentState = BattleState.WILD_INTRO_WAIT;
                    } else if (currentState == BattleState.GO_MESSAGE_TYPING) {
                        currentState = BattleState.GO_MESSAGE_WAIT;
                    }
                }
            }
        } else if (currentState == BattleState.WILD_INTRO_WAIT || currentState == BattleState.GO_MESSAGE_WAIT) {
            indicatorTimer += delta;
        }
    }

    private void updateCamera(float delta) {
        cameraPanTimer += delta * 0.2f;
        float panX = (float) Math.sin(cameraPanTimer) * 40f;
        float panY = 20;
        float zoom = 1.0f + (float) Math.sin(cameraPanTimer * 0.5f) * 0.1f;
        battleCamera.position.set(panX, panY, 0);
        battleCamera.zoom = zoom;
        battleCamera.update();
    }
    
    @Override
    public void render(float delta) {
        animationTimer += delta;
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
                TextureRegion frame = wildPokemonAnimation.getKeyFrame(animationTimer, true);
                float pokeX = enemyPlatformPos.x - frame.getRegionWidth() / 2f;
                float pokeY = enemyPlatformCurrentY + 10;
                batch.draw(frame, pokeX, pokeY);
            }
            
            float playerPlatformCurrentY = playerOnScreenPos.y + playerHoverY;
            batch.draw(platform, playerOnScreenPos.x - platform.getRegionWidth() / 2f, playerPlatformCurrentY);

            if (currentState.ordinal() < BattleState.TRANSITION_OUT.ordinal()) {
                TextureRegion frame = playerThrowAnimation.getKeyFrame(playerSpriteTimer, false);
                float trainerX = playerCurrentPos.x - frame.getRegionWidth() / 2f;
                float trainerY = playerPlatformCurrentY + 4;
                batch.draw(frame, trainerX, trainerY);
            } else {
                if (playerPokemonAnimation != null) {
                    TextureRegion frame = playerPokemonAnimation.getKeyFrame(animationTimer, true);
                    float pokeX = pokemonCurrentPos.x - frame.getRegionWidth() / 2f;
                    float pokeY = playerPlatformCurrentY + 4;
                    batch.draw(frame, pokeX, pokeY);
                }
            }
        }
        batch.end();

        uiViewport.apply();
        batch.setProjectionMatrix(uiCamera.combined);
        
        if (playerPokemon != null && currentState.ordinal() >= BattleState.STATUS_BAR_ENTERING.ordinal()) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            
            float x = playerStatusCurrentPos.x;
            float y = playerStatusCurrentPos.y;
            
            float barWidth = 64f;
            float barHeight = 2f;
            float barX = x + 32;
            float barY = y + 2;
            
            shapeRenderer.setColor(Color.GRAY);
            shapeRenderer.rect(barX, barY, barWidth, barHeight);
            
            int totalExpForCurrentLevel = ExperienceCalculator.getTotalExpForLevel(playerPokemon.getLevel(), playerPokemon.getExperienceGroup());
            int expInCurrentLevel = playerPokemon.getExperiencePoints() - totalExpForCurrentLevel;
            int expNeededForNextLevel = playerPokemon.getExperienceToNextLevel();

            float progressPercentage = 0f;
            if (expNeededForNextLevel > 0) {
                progressPercentage = (float) expInCurrentLevel / expNeededForNextLevel;
            }
            
            shapeRenderer.setColor(Color.CYAN);
            shapeRenderer.rect(barX, barY, barWidth * progressPercentage, barHeight);
            
            shapeRenderer.end();
        }

        batch.begin();
        
        if (statusEnemy != null) {
            float x = 10; float y = 120;
            enemyHealthBar.draw(batch, wildPokemon, x + 39, y + 9);
            batch.draw(statusEnemy, x, y);
            String wildName = wildPokemon.getNickname().toUpperCase();
            if (wildPokemon.getGender() == Pokemon.Gender.MALE) wildName += "♂";
            else if (wildPokemon.getGender() == Pokemon.Gender.FEMALE) wildName += "♀";
            statusFont.draw(batch, wildName, x + 4, y + 16);
            statusFont.draw(batch, "" + wildPokemon.getLevel(), x + 74, y + 16);
        }

        if (statusPlayer != null && playerPokemon != null && currentState.ordinal() >= BattleState.STATUS_BAR_ENTERING.ordinal()) {
            float x = playerStatusCurrentPos.x;
            float y = playerStatusCurrentPos.y;
            
            playerHealthBar.draw(batch, playerPokemon, x + 48, y + 16);
            batch.draw(statusPlayer, x, y);
            
            String playerName = playerPokemon.getNickname().toUpperCase();
            if (playerPokemon.getGender() == Pokemon.Gender.MALE) playerName += "♂";
            else if (playerPokemon.getGender() == Pokemon.Gender.FEMALE) playerName += "♀";
            statusFont.draw(batch, playerName, x + 15, y + 22);
            statusFont.draw(batch, "" + playerPokemon.getLevel(), x + 22, y + 8);
            
            String hpText = playerPokemon.getCurrentHP() + "/" + playerPokemon.getMaxHP();
            float hpTextWidth = statusFont.getWidth(hpText);
            statusFont.draw(batch, hpText, x + 90 - hpTextWidth, y + 7);
        }

        if (battleTextbox != null) {
            batch.draw(battleTextbox, 0, 0, uiViewport.getWorldWidth(), battleTextbox.getRegionHeight());
        }

        // ## RENDERIZAÇÃO DO MENU DE AÇÃO ##
        if (currentState == BattleState.ACTION_SELECTION) {
            // Desenha a caixa de ações
            float actionBoxX = uiViewport.getWorldWidth() - battleActionUi.getRegionWidth();
            batch.draw(battleActionUi, actionBoxX, 0);

            // Desenha a seta na posição correta
            Vector2 arrowPos = actionArrowPositions.get(selectedActionIndex);
            batch.draw(actionArrow, arrowPos.x, arrowPos.y);
        }

        layout.setText(whiteFont, visibleMessage, whiteFont.getColor(), uiViewport.getWorldWidth() - 20, Align.left, true);
        whiteFont.draw(batch, layout, 10, 34);
        
        if (currentState == BattleState.WILD_INTRO_WAIT || currentState == BattleState.GO_MESSAGE_WAIT) {
            drawWaitingIndicator();
        }

        batch.end();
    }

    private void drawWaitingIndicator() {
        if (layout.runs.size > 0 && layout.runs.peek().glyphs.size > 0) {
            GlyphLayout.GlyphRun lastRun = layout.runs.peek();
            float runWidth = 0f;
            for (int i = 0; i < lastRun.xAdvances.size; i++) {
                runWidth += lastRun.xAdvances.get(i);
            }
            float drawX = 10f;
            float drawTopY = 28f;
            float arrowX = drawX + lastRun.x + runWidth + 2f;
            float lastLineBaselineY = drawTopY + lastRun.y;
            float arrowSize = whiteFont.getCapHeight() * 0.75f;
            float arrowBaseY = lastLineBaselineY - (arrowSize / 2f);
            float speed = 8f;
            float amplitude = 2f;
            float yOffset = (float) Math.sin(indicatorTimer * speed) * amplitude;
            float arrowY = arrowBaseY + yOffset;
            batch.draw(dialogueArrow, arrowX, arrowY, arrowSize, arrowSize);
        }
    }
    
    @Override
    public void hide() {
        Gdx.input.setInputProcessor(gameScreen.getInputHandler());
    }

    @Override
    public void resize(int width, int height) {
        battleViewport.update(width, height, false);
        uiViewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        playerHealthBar.dispose();
        enemyHealthBar.dispose();
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
}