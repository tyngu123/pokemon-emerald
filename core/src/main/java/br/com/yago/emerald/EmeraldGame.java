package br.com.yago.emerald;

import br.com.yago.emerald.assets.AssetsLoader;
import br.com.yago.emerald.model.Direction;
import br.com.yago.emerald.model.Player;
import br.com.yago.emerald.view.BagScreen;
import br.com.yago.emerald.view.GameScreen;
import br.com.yago.emerald.view.PartyScreen;
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
    private Player player; // O Player agora é um membro da classe principal

    @Override
    public void create () {
        Gdx.app.log("EmeraldGame", "Jogo iniciado. Carregando assets e criando batch...");

        assetLoader = new AssetsLoader();
        assetLoader.load();
        assetLoader.manager.finishLoading();

        batch = new SpriteBatch();

        // O jogador é criado apenas UMA VEZ, aqui.
        // Carregamos um mapa inicial apenas para ter uma referência de `collisionLayer` para o construtor,
        // mas ele será atualizado a cada warp.
        TiledMap tempMap = new TmxMapLoader().load("maps/alpha_map.tmx");
        TiledMapTileLayer collisionLayer = (TiledMapTileLayer) tempMap.getLayers().get("collision");
        
        player = new Player(
            assetLoader.manager.get(assetLoader.PLAYER_ATLAS),
            collisionLayer, // Camada de colisão inicial
            null,           // Lista de entidades será definida na GameScreen
            10 * 16f,       // Posição X inicial em pixels
            8 * 16f         // Posição Y inicial em pixels
        );
        tempMap.dispose(); // O mapa temporário não é mais necessário

        Gdx.app.log("EmeraldGame", "Assets carregados. Configurando GameScreen inicial.");

        // A instância do player é passada para a GameScreen
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

    public void warpTo(String mapFileName, int playerStartX, int playerStartY, Direction playerFacing) {
        Gdx.app.log("EmeraldGame", "WARP! Trocando para mapa: " + mapFileName);

        GameScreen oldScreen = (GameScreen) getScreen();

        // A MESMA instância do player é passada para a nova GameScreen
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