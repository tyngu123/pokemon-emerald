package br.com.yago.emerald.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import br.com.yago.emerald.EmeraldGame;
import br.com.yago.emerald.assets.AnimationLoader;
import br.com.yago.emerald.assets.AssetsLoader;
import br.com.yago.emerald.model.Party;
import br.com.yago.emerald.model.Pokemon;

public class SummaryScreen extends ScreenAdapter {

    private final EmeraldGame game;
    private final SpriteBatch batch;
    private final Viewport viewport;
    private final OrthographicCamera camera;
    private final Texture background;
    private final BitmapFont font;

    // ## CAMPOS ATUALIZADOS ##
    private final Party party;
    private int currentIndex;
    private Pokemon currentPokemon;
    private Animation<TextureRegion> pokemonAnimation; // Não é mais 'final'
    private float stateTimer = 0f;

    public SummaryScreen(EmeraldGame game, SpriteBatch batch, AssetsLoader assetLoader, Party party, int initialIndex) {
        this.game = game;
        this.batch = batch;
        this.party = party;
        this.camera = new OrthographicCamera();
        this.viewport = new ExtendViewport(240, 160, camera);
        
        this.background = assetLoader.manager.get(assetLoader.SUMMARY_UI, Texture.class);
        this.font = assetLoader.manager.get(assetLoader.PARTY_FONT_NAME + ".otf", BitmapFont.class);

        // Carrega o primeiro Pokémon a ser exibido
        loadPokemon(initialIndex);
    }
    
    /**
     * ## NOVO MÉTODO ##
     * Carrega e prepara um novo Pokémon para exibição.
     * @param index O índice do Pokémon na party.
     */
    private void loadPokemon(int index) {
        // 1. Limpa os recursos do Pokémon anterior para evitar vazamento de memória
        if (pokemonAnimation != null) {
            for (Object frameObject : pokemonAnimation.getKeyFrames()) {
                TextureRegion frame = (TextureRegion) frameObject;
                if (frame.getTexture() != null) {
                    frame.getTexture().dispose();
                }
            }
        }

        // 2. Atualiza o índice e o Pokémon atual
        this.currentIndex = index;
        this.currentPokemon = party.getPokemon(currentIndex);

        // 3. Carrega a nova animação e reseta o timer
        this.pokemonAnimation = AnimationLoader.loadGifAnimation(currentPokemon.getFrontSpriteGifPath());
        this.stateTimer = 0f;
    }
    
    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.X || keycode == Input.Keys.ESCAPE) {
                    game.closeSummaryScreen();
                    return true;
                }
                // ## NOVA LÓGICA DE NAVEGAÇÃO ##
                if (keycode == Input.Keys.DOWN) {
                    int nextIndex = (currentIndex + 1) % party.getPartySize();
                    loadPokemon(nextIndex);
                    return true;
                }
                if (keycode == Input.Keys.UP) {
                    int prevIndex = (currentIndex - 1 + party.getPartySize()) % party.getPartySize();
                    loadPokemon(prevIndex);
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
        batch.draw(background, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        if (pokemonAnimation != null) {
            TextureRegion currentFrame = pokemonAnimation.getKeyFrame(stateTimer, true);
            
            // ## TAMANHO DINÂMICO DO GIF ##
            // Pega a largura e altura originais do frame do GIF
            float gifWidth = currentFrame.getRegionWidth();
            float gifHeight = currentFrame.getRegionHeight();

            // Desenha o GIF com seu tamanho original
            batch.draw(currentFrame, 25, 60, gifWidth, gifHeight); 
        }

        // Usa o 'currentPokemon' para obter as informações
        String displayName = currentPokemon.getNickname() + " / " + currentPokemon.getSpeciesName();
        String level = "Lv" + currentPokemon.getLevel();
        
        font.draw(batch, displayName, 10, 150);
        font.draw(batch, level, 10, 130);

        batch.end();
    }
    
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }
    
    @Override
    public void hide() {
        // Devolve o InputProcessor para a tela anterior ao se esconder
        Gdx.input.setInputProcessor(Gdx.input.getInputProcessor());
    }

    @Override
    public void dispose() {
        // Chama o método para limpar os recursos do último Pokémon exibido
        if (pokemonAnimation != null) {
            for (Object frameObject : pokemonAnimation.getKeyFrames()) {
                TextureRegion frame = (TextureRegion) frameObject;
                if (frame.getTexture() != null) {
                    frame.getTexture().dispose();
                }
            }
        }
    }
}