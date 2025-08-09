package br.com.yago.emerald.tools;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * Um AssetLoader customizado para o AssetManager da LibGDX.
 * Ele carrega um arquivo .gif e o converte em um objeto Animation<TextureRegion>.
 */
public class AnimationAssetLoader extends AsynchronousAssetLoader<Animation, AnimationAssetLoader.AnimationParameters> {

    private Array<TextureRegion> frames;
    private float frameDuration;

    public AnimationAssetLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, AnimationParameters parameter) {
        frames = null;

        GifDecoder decoder = new GifDecoder();
        decoder.read(file.read());

        if (decoder.getFrameCount() == 0) return;

        frameDuration = decoder.getDelay(0) / 1000f; // Converte para segundos
        frames = new Array<>();

        for (int i = 0; i < decoder.getFrameCount(); i++) {
            Texture texture = new Texture(decoder.getFrame(i));
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            frames.add(new TextureRegion(texture));
        }
    }

    @Override
    public Animation loadSync(AssetManager manager, String fileName, FileHandle file, AnimationParameters parameter) {
        if (frames == null) return null;
        
        Animation<TextureRegion> animation = new Animation<>(frameDuration, frames, Animation.PlayMode.LOOP);
        frames = null; // Limpa para a próxima carga
        return animation;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, AnimationParameters parameter) {
        return null;
    }

    static public class AnimationParameters extends AssetLoaderParameters<Animation> {
    }
}