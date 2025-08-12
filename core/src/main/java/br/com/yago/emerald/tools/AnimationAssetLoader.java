package br.com.yago.emerald.tools;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * Um AssetLoader customizado e OTIMIZADO para o AssetManager da LibGDX.
 * Ele separa corretamente a decodificação do GIF da criação da Textura para
 * evitar erros de contexto OpenGL e otimizar a performance.
 */
public class AnimationAssetLoader extends AsynchronousAssetLoader<Animation, AnimationAssetLoader.AnimationParameters> {

    // Irá guardar os dados decodificados do GIF entre a carga assíncrona e a síncrona.
    public GifDecoder decoder;

    public AnimationAssetLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, AnimationParameters parameter) {
        // Na thread de fundo, apenas lemos o arquivo e decodificamos os frames para Pixmaps.
        // Nenhum objeto OpenGL (como Textures) é criado aqui.
        decoder = new GifDecoder();
        decoder.read(file.read());
    }

    @Override
    public Animation<TextureRegion> loadSync(AssetManager manager, String fileName, FileHandle file, AnimationParameters parameter) {
        if (decoder == null) {
            return null;
        }

        // Agora, na thread principal de renderização, podemos criar a Textura.
        Pixmap atlasPixmap = null;
        Animation<TextureRegion> animation = null;

        try {
            int frameCount = decoder.getFrameCount();
            if (frameCount <= 0) {
                return null;
            }

            // Pega as dimensões do primeiro frame para calcular o tamanho do atlas.
            Pixmap firstFrame = decoder.getFrame(0);
            int width = firstFrame.getWidth();
            int height = firstFrame.getHeight();
            
            // Lógica para criar um "atlas" (uma imagem grande) com todos os frames do GIF.
            int vzones = (int)Math.sqrt((double)frameCount);
            int hzones = vzones;
            while(vzones * hzones < frameCount) hzones++;

            atlasPixmap = new Pixmap(width * hzones, height * vzones, Pixmap.Format.RGBA8888);

            // Desenha cada frame do GIF no atlas.
            for(int h = 0; h < hzones; h++) {
                for(int v = 0; v < vzones; v++) {
                    int frameID = v + h * vzones;
                    if(frameID < frameCount) {
                        Pixmap frame = decoder.getFrame(frameID);
                        atlasPixmap.drawPixmap(frame, h * width, v * height);
                    }
                }
            }

            // Cria UMA ÚNICA Textura a partir do atlas. Esta é a grande otimização.
            Texture atlasTexture = new Texture(atlasPixmap);
            atlasTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            
            // Cria as TextureRegions que apontam para as áreas corretas da nossa Textura.
            Array<TextureRegion> textureRegions = new Array<>();
            for(int h = 0; h < hzones; h++) {
                for(int v = 0; v < vzones; v++) {
                    int frameID = v + h * vzones;
                    if(frameID < frameCount) {
                        TextureRegion tr = new TextureRegion(atlasTexture, h * width, v * height, width, height);
                        textureRegions.add(tr);
                    }
                }
            }

            float frameDuration = decoder.getDelay(0) / 1000f; // em segundos
            PlayMode playMode = (parameter != null) ? parameter.playMode : PlayMode.LOOP;

            animation = new Animation<>(frameDuration, textureRegions, playMode);

        } finally {
            // Limpa os recursos da memória para evitar vazamentos.
            if (atlasPixmap != null) {
                atlasPixmap.dispose();
            }
            if (decoder != null) {
                for (int i = 0; i < decoder.getFrameCount(); i++) {
                    decoder.getFrame(i).dispose();
                }
            }
            decoder = null; // Limpa o decoder para o próximo asset.
        }
        
        return animation;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, AnimationParameters parameter) {
        return null;
    }

    // Parâmetros para permitir a customização do PlayMode ao carregar.
    static public class AnimationParameters extends AssetLoaderParameters<Animation> {
        public PlayMode playMode = PlayMode.LOOP;
        
        public AnimationParameters() {}

        public AnimationParameters(PlayMode playMode) {
            this.playMode = playMode;
        }
    }
}