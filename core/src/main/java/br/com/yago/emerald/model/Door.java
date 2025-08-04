package br.com.yago.emerald.model;

import br.com.yago.emerald.config.GameConfig;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Uma entidade permanente que representa uma porta no mapa.
 * Pode estar no estado FECHADA ou ABRINDO.
 */
public class Door extends Entity {

    private enum DoorState { CLOSED, OPENING }
    private DoorState currentState = DoorState.CLOSED;

    private final Animation<TextureRegion> openAnimation;
    private float stateTimer = 0f;

    public Door(TextureAtlas atlas, String doorType, int gridX, int gridY) {
        // ## CORREÇÃO DE TAMANHO ##
        // A porta agora é criada com 16x22, igual ao jogador.
        super(gridX * GameConfig.TILE_SIZE, gridY * GameConfig.TILE_SIZE, GameConfig.PLAYER_WIDTH, GameConfig.PLAYER_HEIGHT);
        
        this.openAnimation = new Animation<>(0.1f, atlas.findRegions(doorType), Animation.PlayMode.NORMAL);
    }

    /**
     * Inicia a animação de abertura da porta.
     */
    public void open() {
        this.stateTimer = 0f;
        this.currentState = DoorState.OPENING;
    }

    @Override
    public void update(float delta) {
        if (currentState == DoorState.OPENING) {
            stateTimer += delta;
        }
    }

    @Override
    public TextureRegion getCurrentFrame() {
        if (currentState == DoorState.OPENING) {
            // Se estiver abrindo, retorna o frame atual da animação.
            return openAnimation.getKeyFrame(stateTimer);
        } else {
            // Se estiver fechada, retorna sempre o primeiro frame.
            return openAnimation.getKeyFrames()[0];
        }
    }
    
    /**
     * Verifica se a animação de ABERTURA já terminou.
     */
    public boolean isOpeningFinished() {
        return currentState == DoorState.OPENING && openAnimation.isAnimationFinished(stateTimer);
    }
}