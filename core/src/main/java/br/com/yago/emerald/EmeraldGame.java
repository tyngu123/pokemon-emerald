package br.com.yago.emerald;

import br.com.yago.emerald.assets.AssetsLoader;
import br.com.yago.emerald.database.GameDatabase;
import br.com.yago.emerald.model.Direction;
import br.com.yago.emerald.model.Party;
import br.com.yago.emerald.model.Player;
import br.com.yago.emerald.model.Pokemon;
import br.com.yago.emerald.view.BagScreen;
import br.com.yago.emerald.view.BattleScreen;
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
    private Screen previousScreen;
    private String playerName;
    private BattleScreen battleScreen;
    public GameDatabase db;

    @Override
    public void create () {
        Gdx.app.log("EmeraldGame", "Jogo iniciado. Carregando assets e criando batch...");

        // Carrega o banco de dados de JSONs antes de qualquer outra coisa
        db = new GameDatabase();
        db.load();

        assetLoader = new AssetsLoader();
        assetLoader.load();
        assetLoader.manager.finishLoading();

        batch = new SpriteBatch();
        
        this.playerName = "YAGO";

        TiledMap tempMap = new TmxMapLoader().load("maps/alpha_map.tmx");
        TiledMapTileLayer collisionLayer = (TiledMapTileLayer) tempMap.getLayers().get("collision");
        
        player = new Player(
            this.playerName,
            assetLoader.manager.get(assetLoader.PLAYER_ATLAS), 
            collisionLayer,
            null,
            10 * 16f,
            8 * 16f,
            this // Passa a instância do jogo para o Player
        );
        tempMap.dispose();

        Gdx.app.log("EmeraldGame", "Assets carregados. Configurando GameScreen inicial.");

        gameScreen = new GameScreen(this, assetLoader, batch, "maps/alpha_map.tmx", player, 10, 8, Direction.DOWN, false);
        setScreen(gameScreen);
        gameScreen.startFadeIn();
    }
    
    public void openBattleScreen(Pokemon wildPokemon) {
        if (gameScreen != null && !gameScreen.isFading()) {
            gameScreen.setInBattle(true);
            battleScreen = new BattleScreen(this, gameScreen, batch, assetLoader, player.getParty(), wildPokemon);
            setScreen(battleScreen);
        }
    }

    public void closeBattleScreen() {
        Screen oldScreen = getScreen();
        if (oldScreen instanceof BattleScreen) {
            setScreen(gameScreen); 
            gameScreen.startFadeIn();
            gameScreen.setInBattle(false);
            gameScreen.onBattleEnd();
            oldScreen.dispose();
        }
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
    
    public void openSummaryScreen(Party party, int initialIndex) {
        this.previousScreen = getScreen();
        setScreen(new SummaryScreen(this, batch, assetLoader, party, initialIndex));
    }

    public void closeSummaryScreen() {
        Screen oldScreen = getScreen();
        if (oldScreen instanceof SummaryScreen) {
            setScreen(previousScreen);
            previousScreen = null;
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