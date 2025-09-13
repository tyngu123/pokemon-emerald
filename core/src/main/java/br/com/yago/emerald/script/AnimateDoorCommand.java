package br.com.yago.emerald.script;

import br.com.yago.emerald.model.Character;
import br.com.yago.emerald.model.Door;
import br.com.yago.emerald.model.Warp;
import br.com.yago.emerald.view.GameScreen;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Comando que encontra uma entidade Porta existente no mapa e a anima.
 */
public class AnimateDoorCommand implements ScriptCommand {

    private final Warp warpData;
    private Door doorEntity;

    public AnimateDoorCommand(Warp warpData) {
        this.warpData = warpData;
    }
    
    @Override
    public void initialize(JsonValue data) { /* Criado dinamicamente */ }

    @Override
    public void start(GameScreen screen, Character initiator) {
        if (warpData != null && warpData.isDoor()) {
            // Procura pela entidade Door existente nas coordenadas especificadas pelo warp.
            this.doorEntity = screen.getDoorAt(warpData.getDoorX(), warpData.getDoorY());
            if (this.doorEntity != null) {
                // Se encontrou a porta, manda ela abrir, passando o SoundManager.
                this.doorEntity.open(screen.getGame().getSoundManager());
            }
        }
    }

    @Override
    public void update(float delta) { /* A entidade Door se atualiza sozinha */ }

    @Override
    public boolean isFinished() {
        // Se este warp não tem uma porta, termina imediatamente.
        if (doorEntity == null) {
            return true;
        }
        
        // Se for uma porta, espera a animação de abertura terminar.
        return doorEntity.isOpeningFinished();
    }
}