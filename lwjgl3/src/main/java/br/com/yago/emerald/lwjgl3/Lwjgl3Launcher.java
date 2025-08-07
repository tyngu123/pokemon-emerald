package br.com.yago.emerald.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import br.com.yago.emerald.EmeraldGame;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void main (String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setForegroundFPS(60);
        config.setTitle("Project Emerald Alpha");
        config.setWindowIcon("icon.png");
        // Configura o tamanho inicial da janela para ser um múltiplo do tamanho da viewport
        config.setWindowedMode(240 * 5, 160 * 5);
        config.setResizable(false);
        new Lwjgl3Application(new EmeraldGame(), config);
    }

}
