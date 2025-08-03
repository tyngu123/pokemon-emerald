package br.com.yago.emerald.script;

import br.com.yago.emerald.model.Character;
import br.com.yago.emerald.model.Direction;
import br.com.yago.emerald.model.Warp;
import br.com.yago.emerald.view.GameScreen;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Comando de script que executa o teletransporte para um novo mapa.
 */
public class WarpCommand implements ScriptCommand {

    private Warp warpData;

    @Override
    public void initialize(JsonValue data) {
        // Este comando será inicializado dinamicamente, não a partir de um JSON.
    }

    /**
     * Um construtor especial para criar este comando dinamicamente.
     */
    public WarpCommand(Warp warpData) {
        this.warpData = warpData;
    }

    @Override
    public void start(GameScreen screen, Character initiator) {
        // Chama o método de warp da GameScreen com os dados do portal.
        if (warpData != null) {
            screen.initiateWarp(warpData);
        }
    }

    @Override
    public void update(float delta) {
        // Ação instantânea.
    }

    @Override
    public boolean isFinished() {
        // Termina imediatamente.
        return true;
    }
}