package br.com.yago.emerald.assets;

import br.com.yago.emerald.tools.GifDecoder;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.Pixmap;
import java.io.InputStream;

/**
 * Carrega animações GIF usando a classe GifDecoder fornecida.
 * Compatível com Desktop/Android/HTML por usar FileHandle interno.
 */
public class AnimationLoader {

    public static Animation<TextureRegion> loadGifAnimation(String internalPath) {
        FileHandle fh = Gdx.files.internal(internalPath);

        if (!fh.exists()) {
            Gdx.app.error("AnimationLoader", "GIF não encontrado (internal): " + internalPath);
            return null;
        }

        Array<TextureRegion> frames = new Array<>();
        float frameDuration = 0.1f; // fallback

        try (InputStream in = fh.read()) {
            GifDecoder decoder = new GifDecoder();
            decoder.read(in);

            if (decoder.getFrameCount() > 0) {
                int delayMs = decoder.getDelay(0);
                if (delayMs > 0) {
                    // ## ALTERAÇÃO AQUI ##
                    // A duração original (delayMs / 1000f) é dividida por 2 para
                    // deixar a animação 2x mais rápida.
                    frameDuration = (delayMs / 1000f) / 3f;
                }
            }

            for (int i = 0; i < decoder.getFrameCount(); i++) {
                Pixmap pm = decoder.getFrame(i);
                Texture texture = new Texture(pm);
                texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                frames.add(new TextureRegion(texture));
                pm.dispose(); // evita vazamento de memória do Pixmap
            }

            return new Animation<>(frameDuration, frames, Animation.PlayMode.LOOP);

        } catch (Exception e) {
            Gdx.app.error("AnimationLoader", "Erro ao decodificar GIF: " + internalPath, e);
            return null;
        }
    }
}