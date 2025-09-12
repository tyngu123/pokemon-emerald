package br.com.yago.emerald.tools;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import br.com.yago.emerald.assets.AssetsLoader;

/**
 * Gerencia e reproduz todos os efeitos sonoros (SFX) do jogo.
 */
public class SoundManager {

    private final AssetManager assetManager;
    private final AssetsLoader assets;

    private Sound sfxClick;
    private Sound sfxEnterMap;

    public SoundManager(AssetManager assetManager, AssetsLoader assets) {
        this.assetManager = assetManager;
        this.assets = assets;
        loadSounds();
    }

    private void loadSounds() {
        // Pega os sons que já foram carregados pelo AssetsLoader
        this.sfxClick = assetManager.get(assets.SFX_CLICK, Sound.class);
        this.sfxEnterMap = assetManager.get(assets.SFX_ENTER_MAP, Sound.class);
    }

    /**
     * Toca o som de clique de menu/confirmação.
     */
    public void playClickSfx() {
        if (sfxClick != null) {
            sfxClick.play(0.7f); // O volume pode ser ajustado (0.0 a 1.0)
        }
    }

    /**
     * Toca o som de entrar/sair de uma área (warp).
     */
    public void playEnterMapSfx() {
        if (sfxEnterMap != null) {
            sfxEnterMap.play(0.8f);
        }
    }

    // Não precisa de um método dispose aqui, pois o AssetManager já gerencia isso.
}