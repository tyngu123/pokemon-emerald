package br.com.yago.emerald;

import br.com.yago.emerald.assets.AssetsLoader;
import br.com.yago.emerald.model.Direction;
import br.com.yago.emerald.view.GameScreen;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch; // Importe o SpriteBatch

public class EmeraldGame extends Game {

    private AssetsLoader assetLoader;
    private SpriteBatch batch; // ## O SPRITEBATCH AGORA É CENTRALIZADO AQUI ##

    @Override
    public void create () {
        Gdx.app.log("EmeraldGame", "Jogo iniciado. Carregando assets e criando batch...");
        
        assetLoader = new AssetsLoader();
        assetLoader.load();
        assetLoader.manager.finishLoading();
        
        batch = new SpriteBatch(); // Cria o batch UMA ÚNICA VEZ.

        Gdx.app.log("EmeraldGame", "Assets carregados. Configurando GameScreen inicial.");
        
        // Passa o assetLoader e o batch para a tela inicial.
        GameScreen initialScreen = new GameScreen(this, assetLoader, batch, "maps/alpha_map.tmx", 10, 8, Direction.DOWN);
        setScreen(initialScreen);
        initialScreen.startFadeIn();
    }

    public void warpTo(String mapFileName, int playerStartX, int playerStartY, Direction playerFacing) {
        Gdx.app.log("EmeraldGame", "WARP! Trocando para mapa: " + mapFileName);
        
        GameScreen oldScreen = (GameScreen) getScreen();
        
        // Cria a nova tela, passando as referências centrais.
        GameScreen newScreen = new GameScreen(this, assetLoader, batch, mapFileName, playerStartX, playerStartY, playerFacing);
        setScreen(newScreen);
        
        newScreen.startFadeIn();
        
        if (oldScreen != null) {
            oldScreen.dispose();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        // Libera os recursos centrais quando o jogo fecha.
        if (assetLoader != null) {
            assetLoader.dispose();
        }
        if (batch != null) {
            batch.dispose();
        }
    }
}