package br.com.yago.emerald;

import br.com.yago.emerald.assets.AssetsLoader;
import br.com.yago.emerald.model.Direction;
import br.com.yago.emerald.view.GameScreen;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class EmeraldGame extends Game {

    private AssetsLoader assetLoader;
    private SpriteBatch batch;

    @Override
    public void create () {
        Gdx.app.log("EmeraldGame", "Jogo iniciado. Carregando assets e criando batch...");
        
        assetLoader = new AssetsLoader();
        assetLoader.load();
        assetLoader.manager.finishLoading();
        
        batch = new SpriteBatch();

        Gdx.app.log("EmeraldGame", "Assets carregados. Configurando GameScreen inicial.");
        
        // ## MUDANÇA AQUI: Passa 'false' para o novo parâmetro 'isWarpIn' ##
        GameScreen initialScreen = new GameScreen(this, assetLoader, batch, "maps/alpha_map.tmx", 10, 8, Direction.DOWN, false);
        setScreen(initialScreen);
        initialScreen.startFadeIn();
    }

    public void warpTo(String mapFileName, int playerStartX, int playerStartY, Direction playerFacing) {
        Gdx.app.log("EmeraldGame", "WARP! Trocando para mapa: " + mapFileName);
        
        GameScreen oldScreen = (GameScreen) getScreen();
        
        // ## MUDANÇA AQUI: Passa 'true' para o novo parâmetro 'isWarpIn' ##
        GameScreen newScreen = new GameScreen(this, assetLoader, batch, mapFileName, playerStartX, playerStartY, playerFacing, true);
        setScreen(newScreen);
        
        newScreen.startFadeIn();
        
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