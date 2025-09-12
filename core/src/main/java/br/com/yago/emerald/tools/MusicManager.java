package br.com.yago.emerald.tools;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.Interpolation;

public class MusicManager {

    private final AssetManager assetManager;
    private Music currentMusic;
    private Music nextMusic;

    private enum State { IDLE, FADING_OUT, FADING_IN }
    private State state = State.IDLE;

    private float fadeTimer = 0f;
    private final float FADE_DURATION = 0.1f; // Duração do fade em segundos
    private final float MAX_VOLUME = 0.5f;    // Volume máximo da música

    private String currentMapMusicPath;

    public MusicManager(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public void playMapMusic(String mapMusicFile) {
        if (mapMusicFile == null || mapMusicFile.isEmpty()) {
            stopMusic();
            currentMapMusicPath = null;
            return;
        }

        String path = "sounds/maps/" + mapMusicFile;
        if (path.equals(currentMapMusicPath) && state != State.FADING_OUT) {
            return; // Já está tocando ou fazendo fade in para esta música
        }

        currentMapMusicPath = path;
        playMusic(path);
    }

    public void playBattleMusic() {
        playMusic("sounds/battle/wildpokemon.mp3");
    }

    public void returnToMapMusic() {
        if (currentMapMusicPath != null) {
            playMusic(currentMapMusicPath);
        } else {
            stopMusic();
        }
    }

    private void playMusic(String path) {
        if (!assetManager.isLoaded(path, Music.class)) {
            assetManager.load(path, Music.class);
            assetManager.finishLoading(); // Garante que a música esteja pronta para tocar
        }
        this.nextMusic = assetManager.get(path, Music.class);

        if (currentMusic != null && currentMusic.isPlaying()) {
            state = State.FADING_OUT;
            fadeTimer = 0f;
        } else {
            currentMusic = nextMusic;
            nextMusic = null;
            currentMusic.setVolume(0);
            currentMusic.setLooping(true);
            currentMusic.play();
            state = State.FADING_IN;
            fadeTimer = 0f;
        }
    }

    private void stopMusic() {
        if (currentMusic != null) {
            nextMusic = null;
            state = State.FADING_OUT;
            fadeTimer = 0f;
        }
    }

    public void update(float delta) {
        if (state == State.IDLE) return;

        fadeTimer += delta;
        float progress = Math.min(1f, fadeTimer / FADE_DURATION);

        if (state == State.FADING_OUT) {
            if (currentMusic != null) {
                // ## CORREÇÃO AQUI ## Garante que o volume nunca seja negativo
                float volume = Interpolation.fade.apply(MAX_VOLUME, 0, progress);
                currentMusic.setVolume(Math.max(0f, volume));
            }
            if (progress >= 1f) {
                if (currentMusic != null) {
                    currentMusic.stop();
                }
                currentMusic = nextMusic;
                nextMusic = null;
                if (currentMusic != null) {
                    state = State.FADING_IN;
                    fadeTimer = 0f;
                    currentMusic.setVolume(0);
                    currentMusic.setLooping(true);
                    currentMusic.play();
                } else {
                    state = State.IDLE;
                }
            }
        } else if (state == State.FADING_IN) {
            if (currentMusic != null) {
                // ## CORREÇÃO AQUI ## Garante que o volume nunca seja negativo
                float volume = Interpolation.fade.apply(0, MAX_VOLUME, progress);
                currentMusic.setVolume(Math.max(0f, volume));
            }
            if (progress >= 1f) {
                state = State.IDLE;
            }
        }
    }

    public void dispose() {
        if (currentMusic != null) {
            currentMusic.dispose();
        }
    }
}