package br.com.yago.emerald;

import br.com.yago.emerald.model.Direction;
import br.com.yago.emerald.view.GameScreen;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

public class EmeraldGame extends Game {

    @Override
    public void create () {
        Gdx.app.log("EmeraldGame", "Jogo iniciado. Configurando GameScreen inicial.");
        // Inicia o jogo no mapa padrão e posição inicial
        setScreen(new GameScreen(this, "maps/alpha_map.tmx", 10, 8, Direction.DOWN));
    }

    /**
     * O método principal para teletransporte.
     * Ele destrói a tela antiga e cria uma nova com os parâmetros de destino.
     */
    public void warpTo(String mapFileName, int playerStartX, int playerStartY, Direction playerFacing) {
        Gdx.app.log("EmeraldGame", "WARP! Trocando para mapa: " + mapFileName);
        
        // Pega a tela atual para poder destruí-la depois
        GameScreen oldScreen = (GameScreen) getScreen();
        
        // Cria e define a nova tela
        setScreen(new GameScreen(this, mapFileName, playerStartX, playerStartY, playerFacing));
        
        // Libera os recursos da tela antiga para evitar vazamento de memória
        if (oldScreen != null) {
            oldScreen.dispose();
        }
    }
}