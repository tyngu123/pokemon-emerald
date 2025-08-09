package br.com.yago.emerald;

import br.com.yago.emerald.assets.AssetsLoader;
import br.com.yago.emerald.model.Direction;
import br.com.yago.emerald.model.Party;
import br.com.yago.emerald.model.Player;
import br.com.yago.emerald.model.Pokemon;
import br.com.yago.emerald.view.BagScreen;
import br.com.yago.emerald.view.GameScreen;
import br.com.yago.emerald.view.PartyScreen;
import br.com.yago.emerald.view.SummaryScreen;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public class EmeraldGame extends Game {

    private AssetsLoader assetLoader;
    private SpriteBatch batch;
    private GameScreen gameScreen;
    private Player player;
    private Screen previousScreen; // Guarda a tela anterior (PartyScreen)

    @Override
    public void create () {
        Gdx.app.log("EmeraldGame", "Jogo iniciado. Carregando assets e criando batch...");

        assetLoader = new AssetsLoader();
        assetLoader.load();
        assetLoader.manager.finishLoading();

        batch = new SpriteBatch();

        TiledMap tempMap = new TmxMapLoader().load("maps/alpha_map.tmx");
        TiledMapTileLayer collisionLayer = (TiledMapTileLayer) tempMap.getLayers().get("collision");
        
        player = new Player(
            assetLoader.manager.get(assetLoader.PLAYER_ATLAS), 
            collisionLayer,
            null,
            10 * 16f,
            8 * 16f
        );
        tempMap.dispose();

        Gdx.app.log("EmeraldGame", "Assets carregados. Configurando GameScreen inicial.");

        gameScreen = new GameScreen(this, assetLoader, batch, "maps/alpha_map.tmx", player, 10, 8, Direction.DOWN, false);
        setScreen(gameScreen);
        gameScreen.startFadeIn();
    }

    public void openBagScreen() {
        if (gameScreen != null && !gameScreen.isFading()) {
            gameScreen.startFadeOut(0.5f, () -> {
                setScreen(new BagScreen(this, gameScreen, batch, assetLoader));
            });
        }
    }

    public void closeBagScreen() {
        Screen oldScreen = getScreen();
        if (oldScreen instanceof BagScreen) {
            setScreen(gameScreen);
            gameScreen.startFadeIn();
            oldScreen.dispose();
        }
    }

    public void openPartyScreen() {
        if (gameScreen != null && !gameScreen.isFading()) {
            gameScreen.startFadeOut(0.6f, () -> {
                setScreen(new PartyScreen(this, gameScreen, batch, assetLoader));
            });
        }
    }

    public void closePartyScreen() {
        Screen oldScreen = getScreen();
        if (oldScreen instanceof PartyScreen) {
            setScreen(gameScreen);
            gameScreen.startFadeIn();
            oldScreen.dispose();
        }
    }
    
    /**
     * ## MÉTODO ATUALIZADO ##
     * Agora aceita a Party e o índice inicial para a SummaryScreen.
     */
    public void openSummaryScreen(Party party, int initialIndex) {
        this.previousScreen = getScreen(); // Salva a PartyScreen atual
        setScreen(new SummaryScreen(this, batch, assetLoader, party, initialIndex));
    }

    public void closeSummaryScreen() {
        Screen oldScreen = getScreen();
        if (oldScreen instanceof SummaryScreen) {
            setScreen(previousScreen); // Volta para a PartyScreen que foi salva
            previousScreen = null; // Limpa a referência para evitar memory leak
            oldScreen.dispose();
        }
    }

    public void warpTo(String mapFileName, int playerStartX, int playerStartY, Direction playerFacing) {
        Gdx.app.log("EmeraldGame", "WARP! Trocando para mapa: " + mapFileName);

        GameScreen oldScreen = (GameScreen) getScreen();

        gameScreen = new GameScreen(this, assetLoader, batch, mapFileName, player, playerStartX, playerStartY, playerFacing, true);
        setScreen(gameScreen);

        gameScreen.startFadeIn();

        if (oldScreen != null) {
            oldScreen.dispose();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (assetLoader != null) {
            assetLoader.dispose();
        }
        if (batch != null) {
            batch.dispose();
        }
    }
}