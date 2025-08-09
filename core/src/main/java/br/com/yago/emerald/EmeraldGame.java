package br.com.yago.emerald;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import br.com.yago.emerald.assets.AssetsLoader;
import br.com.yago.emerald.model.Direction;
import br.com.yago.emerald.view.BagScreen;
import br.com.yago.emerald.view.GameScreen;
import br.com.yago.emerald.view.PartyScreen;

public class EmeraldGame extends Game {

    private AssetsLoader assetLoader;
    private SpriteBatch batch;
    private GameScreen gameScreen; // ## MANTÉM UMA REFERÊNCIA À TELA DO JOGO ##

    @Override
    public void create () {
        Gdx.app.log("EmeraldGame", "Jogo iniciado. Carregando assets e criando batch...");

        assetLoader = new AssetsLoader();
        assetLoader.load();
        assetLoader.manager.finishLoading();

        batch = new SpriteBatch();

        Gdx.app.log("EmeraldGame", "Assets carregados. Configurando GameScreen inicial.");

        gameScreen = new GameScreen(this, assetLoader, batch, "maps/alpha_map.tmx", 10, 8, Direction.DOWN, false);
        setScreen(gameScreen);
        gameScreen.startFadeIn();
    }

    // ## MÉTODO PARA ABRIR A MOCHILA COM FADE ##
    public void openBagScreen() {
        if (gameScreen != null && !gameScreen.isFading()) {
            // Usa o FadeManager da GameScreen para fazer o fade out
            gameScreen.startFadeOut(0.5f, () -> {
                // Quando o fade out terminar, troca para a tela da mochila
                setScreen(new BagScreen(this, gameScreen, batch, assetLoader));
            });
        }
    }

    // ## MÉTODO PARA FECHAR A MOCHILA COM FADE ##
    public void closeBagScreen() {
        Screen oldScreen = getScreen();
        if (oldScreen instanceof BagScreen) {
            // Volta para a tela do jogo, que já estará preta por causa do fade out anterior
            setScreen(gameScreen);
            // Inicia o fade in para revelar a tela do jogo
            gameScreen.startFadeIn();
            oldScreen.dispose();
        }
    }

    // ## MÉTODO PARA ABRIR A TELA DA PARTY COM FADE ##
    public void openPartyScreen() {
        if (gameScreen != null && !gameScreen.isFading()) {
            // Usa o FadeManager da GameScreen para fazer o fade out
            gameScreen.startFadeOut(0.6f, () -> {
                 // Quando o fade out terminar, troca para a tela da party
                setScreen(new PartyScreen(this, gameScreen, batch, assetLoader));
            });
        }
    }

    // ## MÉTODO PARA FECHAR A TELA DA PARTY COM FADE ##
    public void closePartyScreen() {
        Screen oldScreen = getScreen();
        if (oldScreen instanceof PartyScreen) {
            // Volta para a tela do jogo, que já estará preta por causa do fade out anterior
            setScreen(gameScreen);
            // Inicia o fade in para revelar a tela do jogo
            gameScreen.startFadeIn();
            oldScreen.dispose();
        }
    }

    public void warpTo(String mapFileName, int playerStartX, int playerStartY, Direction playerFacing) {
        Gdx.app.log("EmeraldGame", "WARP! Trocando para mapa: " + mapFileName);

        GameScreen oldScreen = (GameScreen) getScreen();

        gameScreen = new GameScreen(this, assetLoader, batch, mapFileName, playerStartX, playerStartY, playerFacing, true);
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