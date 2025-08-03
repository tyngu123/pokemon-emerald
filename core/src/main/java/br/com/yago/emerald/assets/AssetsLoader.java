package br.com.yago.emerald.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.graphics.Color;

public class AssetsLoader {

    public final AssetManager manager = new AssetManager();

    // UI
    public final String FONT_FILE    = "ui/font.otf";
    public final String FONT_NAME    = "gameFont";
    public final String DIALOGUE_BOX = "ui/dialoguebox.png";
    public final String DIALOGUE_ARROW = "ui/arrow.png";
    public final String TOWN_BOX = "ui/townbox.png"; // ## NOVO ASSET ##

    // Atlases
    public final String PLAYER_ATLAS      = "sprites_overworld/player.atlas";
    public final String PROF_BIRCH_ATLAS  = "sprites_overworld/professor_birch.atlas";
    public final String YOUNGSTER_ATLAS   = "sprites_overworld/youngster.atlas";
    public final String MOM_ATLAS   = "sprites_overworld/mom.atlas";
    public final String NPC1 = "sprites_overworld/npc1.atlas";

    public void load() {
        FileHandleResolver resolver = new InternalFileHandleResolver();
        manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        manager.setLoader(BitmapFont.class, ".otf", new FreetypeFontLoader(resolver));

        // Fonte
        FreetypeFontLoader.FreeTypeFontLoaderParameter fontParams =
            new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        fontParams.fontFileName = FONT_FILE;
        fontParams.fontParameters.size = 64;
        fontParams.fontParameters.color = new Color(0.22f, 0.22f, 0.22f, 1f);
        fontParams.fontParameters.shadowColor = new Color(0.9f, 0.9f, 0.9f, 1f);
        fontParams.fontParameters.shadowOffsetX = 1;
        fontParams.fontParameters.shadowOffsetY = 1;
        fontParams.fontParameters.minFilter = TextureFilter.Nearest;
        fontParams.fontParameters.magFilter = TextureFilter.Nearest;
        manager.load(FONT_NAME + ".otf", BitmapFont.class, fontParams);

        // UI Textures
        manager.load(DIALOGUE_BOX,   Texture.class);
        manager.load(DIALOGUE_ARROW, Texture.class);
        manager.load(TOWN_BOX,       Texture.class); // ## CARREGA A NOVA TEXTURA ##

        // Atlases
        manager.load(PLAYER_ATLAS,     TextureAtlas.class);
        manager.load(PROF_BIRCH_ATLAS, TextureAtlas.class);
        manager.load(YOUNGSTER_ATLAS,  TextureAtlas.class);
        manager.load(MOM_ATLAS,  TextureAtlas.class);
        manager.load(NPC1,  TextureAtlas.class);
    }

    public void dispose() {
        manager.dispose();
    }
}