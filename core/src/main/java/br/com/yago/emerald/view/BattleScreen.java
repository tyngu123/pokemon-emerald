package br.com.yago.emerald.view;

import br.com.yago.emerald.EmeraldGame;
import br.com.yago.emerald.assets.AssetsLoader;
import br.com.yago.emerald.model.Party;
import br.com.yago.emerald.model.Pokemon;
import br.com.yago.emerald.tools.AnimationAssetLoader.AnimationParameters;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class BattleScreen extends ScreenAdapter {

    private final EmeraldGame game;
    private final GameScreen gameScreen;
    private final SpriteBatch batch;
    private final Viewport viewport;
    private final OrthographicCamera camera;
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();
    private final AssetManager assetManager;

    private final Pokemon wildPokemon;
    private final Pokemon playerPokemon;
    private Animation<TextureRegion> playerPokemonAnimation;
    private float stateTimer = 0f;

    public BattleScreen(EmeraldGame game, GameScreen gameScreen, SpriteBatch batch, AssetsLoader assetLoader, Party playerParty, Pokemon wildPokemon) {
        this.game = game;
        this.gameScreen = gameScreen;
        this.batch = batch;
        this.camera = new OrthographicCamera();
        this.viewport = new ExtendViewport(240, 160, camera);
        this.font = assetLoader.manager.get(assetLoader.DIALOGUE_FONT_NAME + ".otf", BitmapFont.class);
        this.assetManager = assetLoader.manager;
        
        this.wildPokemon = wildPokemon;
        this.playerPokemon = playerParty.getPokemon(0); // Pega o primeiro Pokémon da equipe
        
        // Carrega a animação do Pokémon do jogador
        loadPlayerPokemonAnimation();
    }
    
    private void loadPlayerPokemonAnimation() {
        String path = playerPokemon.getBackSpriteGifPath();
        if (!assetManager.isLoaded(path, Animation.class)) {
            AnimationParameters params = new AnimationParameters();
            assetManager.load(path, Animation.class, params);
            assetManager.finishLoading(); // Força o carregamento síncrono
        }
        this.playerPokemonAnimation = assetManager.get(path, Animation.class);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.Z || keycode == Input.Keys.X) {
                    game.closeBattleScreen();
                    return true;
                }
                return false;
            }
        });
    }
    
    @Override
    public void render(float delta) {
        stateTimer += delta;

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        
        // Desenha o Pokémon do jogador
        if (playerPokemonAnimation != null) {
            TextureRegion currentFrame = playerPokemonAnimation.getKeyFrame(stateTimer, true);
            // Posição ajustada para o sprite de costas (canto inferior esquerdo da tela)
            batch.draw(currentFrame, 40, 40, 64, 64);
        }

        String message = "A wild " + wildPokemon.getSpeciesName() + " appeared!";
        layout.setText(font, message, Color.WHITE, viewport.getWorldWidth() - 20, Align.center, true);
        font.draw(batch, layout, 10, 20); // Posição do texto ajustada para a parte de baixo
        batch.end();
    }
    
    @Override
    public void hide() {
        Gdx.input.setInputProcessor(gameScreen.getInputHandler());
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        // Libera a animação da memória quando a tela de batalha é fechada
        String path = playerPokemon.getBackSpriteGifPath();
        if (assetManager.isLoaded(path)) {
            assetManager.unload(path);
        }
    }
}