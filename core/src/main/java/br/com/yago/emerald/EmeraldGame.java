package br.com.yago.emerald;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

import br.com.yago.emerald.assets.AssetsLoader;
import br.com.yago.emerald.config.GameConfig;
import br.com.yago.emerald.database.GameDatabase;
import br.com.yago.emerald.model.Direction;
import br.com.yago.emerald.model.Party;
import br.com.yago.emerald.model.Player;
import br.com.yago.emerald.model.Pokemon;
import br.com.yago.emerald.tools.MusicManager;
import br.com.yago.emerald.tools.SoundManager; // ## IMPORT ADICIONADO ##
import br.com.yago.emerald.view.BagScreen;
import br.com.yago.emerald.view.BattleScreen;
import br.com.yago.emerald.view.GameScreen;
import br.com.yago.emerald.view.PartyScreen;
import br.com.yago.emerald.view.SummaryScreen;

public class EmeraldGame extends Game {

    private AssetsLoader assetLoader;
    private SpriteBatch batch;
    private GameScreen gameScreen;
    private Player player;
    private Screen previousScreen;
    private String playerName;
    private BattleScreen battleScreen;
    public GameDatabase db;
    private MusicManager musicManager;
    private SoundManager soundManager; // ## ADICIONADO ##

    @Override
    public void create () {
        Gdx.app.log("EmeraldGame", "Jogo iniciado. Carregando assets e criando batch...");

        db = new GameDatabase();
        db.load();

        assetLoader = new AssetsLoader();
        assetLoader.load();
        assetLoader.manager.finishLoading();

        musicManager = new MusicManager(assetLoader.manager);
        soundManager = new SoundManager(assetLoader.manager, assetLoader); // ## ADICIONADO ##

        batch = new SpriteBatch();
        
        this.playerName = "YAGO";

        TiledMap tempMap = new TmxMapLoader().load("maps/alpha_map.tmx");
        TiledMapTileLayer collisionLayer = (TiledMapTileLayer) tempMap.getLayers().get("collision");
        
        player = new Player(
            this.playerName,
            assetLoader.manager.get(assetLoader.PLAYER_ATLAS), 
            collisionLayer,
            null,
            10 * GameConfig.TILE_SIZE,
            8 * GameConfig.TILE_SIZE,
            this
        );
        tempMap.dispose();

        Gdx.app.log("EmeraldGame", "Assets carregados. Configurando GameScreen inicial.");

        gameScreen = new GameScreen(this, assetLoader, batch, "maps/alpha_map.tmx", player, 10, 8, Direction.DOWN, false);
        setScreen(gameScreen);
        gameScreen.startFadeIn();
    }
    
    @Override
    public void render() {
        super.render();
        if (musicManager != null) {
            musicManager.update(Gdx.graphics.getDeltaTime());
        }
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
            musicManager.returnToMapMusic();
        }
    }

    public void openBagScreen() {
        if (gameScreen != null && !gameScreen.isFading()) {
            soundManager.playClickSfx(); // ## ADICIONADO ##
            gameScreen.startFadeOut(0.5f, () -> {
                setScreen(new BagScreen(this, gameScreen, batch, assetLoader));
            });
        }
    }

    public void closeBagScreen() {
        Screen oldScreen = getScreen();
        if (oldScreen instanceof BagScreen) {
            soundManager.playClickSfx(); // ## ADICIONADO ##
            setScreen(gameScreen);
            gameScreen.startFadeIn();
            oldScreen.dispose();
        }
    }

    public void openPartyScreen() {
        if (gameScreen != null && !gameScreen.isFading()) {
            soundManager.playClickSfx(); // ## ADICIONADO ##
            gameScreen.startFadeOut(0.6f, () -> {
                setScreen(new PartyScreen(this, gameScreen, batch, assetLoader));
            });
        }
    }
    
    public void openPartyScreen(BattleScreen battleScreen) {
        if (battleScreen != null) {
            soundManager.playClickSfx(); // ## ADICIONADO ##
            setScreen(new PartyScreen(this, gameScreen, batch, assetLoader, battleScreen, false));
        }
    }

    public void closePartyScreen() {
        Screen oldScreen = getScreen();
        if (oldScreen instanceof PartyScreen) {
            soundManager.playClickSfx(); // ## ADICIONADO ##
            setScreen(gameScreen);
            gameScreen.startFadeIn();
            oldScreen.dispose();
        }
    }
    
    public void openSummaryScreen(Party party, int initialIndex) {
        soundManager.playClickSfx(); // ## ADICIONADO ##
        this.previousScreen = getScreen();
        setScreen(new SummaryScreen(this, batch, assetLoader, party, initialIndex));
    }

    public void closeSummaryScreen() {
        Screen oldScreen = getScreen();
        if (oldScreen instanceof SummaryScreen) {
            soundManager.playClickSfx(); // ## ADICIONADO ##
            setScreen(previousScreen);
            previousScreen = null;
            oldScreen.dispose();
        }
    }

    public void warpTo(String mapFileName, int playerStartX, int playerStartY, Direction playerFacing) {
        Gdx.app.log("EmeraldGame", "WARP! Trocando para mapa: " + mapFileName);
        soundManager.playEnterMapSfx(); // ## ADICIONADO ##

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
        if (musicManager != null) {
            musicManager.dispose();
        }
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public MusicManager getMusicManager() {
        return musicManager;
    }

    // ## NOVO GETTER ADICIONADO ##
    public SoundManager getSoundManager() {
        return soundManager;
    }
}