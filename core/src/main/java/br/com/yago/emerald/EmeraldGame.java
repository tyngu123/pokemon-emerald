package br.com.yago.emerald;

import br.com.yago.emerald.view.GameScreen;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

/**
 * Classe principal e ponto de entrada do jogo.
 * A classe herda de Game, que oferece um bom sistema de gerenciamento de telas (Screens).
 */
public class EmeraldGame extends Game {

    @Override
    public void create () {
        // O trabalho da classe Game é, principalmente, gerenciar e alternar entre
        // diferentes telas (ex: MainMenuScreen, GameScreen, BattleScreen).
        // Por enquanto, vamos direto para a tela do jogo.
        Gdx.app.log("EmeraldGame", "Jogo iniciado. Configurando GameScreen.");
        setScreen(new GameScreen());
    }
}
