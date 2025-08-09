package br.com.yago.emerald.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.Hinting;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;

import br.com.yago.emerald.tools.AnimationAssetLoader;

public class AssetsLoader {

    public final AssetManager manager = new AssetManager();

    // UI
    public final String FONT_FILE    = "ui/font.otf";
    public final String FONT_NAME    = "gameFont";
    public final String DIALOGUE_FONT_NAME = "dialogueFont";
    public final String BAG_FONT_NAME = "bagFont";
    public final String DIALOGUE_BOX = "ui/dialoguebox.png";
    public final String PARTY_FONT_NAME = "partyFont";
    public final String DIALOGUE_ARROW = "ui/arrow.png";
    public final String HORIZONTAL_ARROW = "ui/horizontal_arrow.png";
    public final String TOWN_BOX = "ui/townbox.png";
    public final String BOX = "ui/box.png";
    public final String BAG_UI = "ui/bag_ui.png";
    public final String SUMMARY_UI = "ui/summary_ui.png";

    // Atlases
    public final String PLAYER_ATLAS      = "sprites_overworld/player.atlas";
    public final String PROF_BIRCH_ATLAS  = "sprites_overworld/professor_birch.atlas";
    public final String YOUNGSTER_ATLAS   = "sprites_overworld/youngster.atlas";
    public final String MOM_ATLAS   = "sprites_overworld/mom.atlas";
    public final String NPC1 = "sprites_overworld/npc1.atlas";
    public final String NPC2 = "sprites_overworld/npc001.atlas";
    public final String DOOR_ATLAS = "sprites_overworld/doors.atlas";
    public final String BAG_ATLAS = "ui/bag.atlas";
    public final String ITEM_ATLAS = "items/items.atlas";
    public final String POKEMON_ICON_ATLAS = "sprites/pokemons/icons/icons.atlas";
    public final String PARTY_UI_ATLAS = "ui/party.atlas";
    public final String TYPE_ATLAS = "ui/type.atlas";
    public final String POKEBALLS_ATLAS = "ui/pokeballs.atlas";
    public final String SUMMARY_ATLAS = "ui/summary.atlas"; // ## NOVO ATLAS ADICIONADO ##

    public void load() {
    	FileHandleResolver resolver = new InternalFileHandleResolver();
        manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        manager.setLoader(BitmapFont.class, ".otf", new FreetypeFontLoader(resolver));
        manager.setLoader(Animation.class, ".gif", new AnimationAssetLoader(resolver));
        
        FreetypeFontLoader.FreeTypeFontLoaderParameter menuFontParams = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        menuFontParams.fontFileName = FONT_FILE;
        menuFontParams.fontParameters.size = 16;
        menuFontParams.fontParameters.color = new Color(0.282f, 0.282f, 0.282f, 1f);
        menuFontParams.fontParameters.shadowColor = new Color(0.815f, 0.815f, 0.784f, 1f);
        menuFontParams.fontParameters.shadowOffsetX = 1;
        menuFontParams.fontParameters.shadowOffsetY = 1;
        menuFontParams.fontParameters.minFilter = TextureFilter.Nearest;
        menuFontParams.fontParameters.magFilter = TextureFilter.Nearest;
        menuFontParams.fontParameters.hinting = Hinting.Full;
        manager.load(FONT_NAME + ".otf", BitmapFont.class, menuFontParams);

        FreetypeFontLoader.FreeTypeFontLoaderParameter dialogueFontParams = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        dialogueFontParams.fontFileName = FONT_FILE;
        dialogueFontParams.fontParameters.size = 16;
        dialogueFontParams.fontParameters.color = new Color(0.282f, 0.282f, 0.282f, 1f);
        dialogueFontParams.fontParameters.shadowColor = new Color(0.815f, 0.815f, 0.784f, 1f);
        dialogueFontParams.fontParameters.shadowOffsetX = 1;
        dialogueFontParams.fontParameters.shadowOffsetY = 1;
        dialogueFontParams.fontParameters.minFilter = TextureFilter.Nearest;
        dialogueFontParams.fontParameters.magFilter = TextureFilter.Nearest;
        dialogueFontParams.fontParameters.hinting = Hinting.Full;
        manager.load(DIALOGUE_FONT_NAME + ".otf", BitmapFont.class, dialogueFontParams);

        FreetypeFontLoader.FreeTypeFontLoaderParameter bagFontParams = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        bagFontParams.fontFileName = FONT_FILE;
        bagFontParams.fontParameters.size = 16;
        bagFontParams.fontParameters.color = new Color(0.282f, 0.282f, 0.282f, 1f);
        bagFontParams.fontParameters.shadowColor = new Color(0.815f, 0.815f, 0.784f, 0.6f);
        bagFontParams.fontParameters.shadowOffsetX = 1;
        bagFontParams.fontParameters.shadowOffsetY = 1;
        bagFontParams.fontParameters.minFilter = TextureFilter.Nearest;
        bagFontParams.fontParameters.magFilter = TextureFilter.Nearest;
        bagFontParams.fontParameters.hinting = Hinting.Full;
        manager.load(BAG_FONT_NAME + ".otf", BitmapFont.class, bagFontParams);
        
        FreetypeFontLoader.FreeTypeFontLoaderParameter partyFontParams = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        partyFontParams.fontFileName = FONT_FILE;
        partyFontParams.fontParameters.size = 16;
        partyFontParams.fontParameters.color = Color.WHITE;
        partyFontParams.fontParameters.shadowColor = new Color(0.6f, 0.6f, 0.6f, 1f);
        partyFontParams.fontParameters.shadowOffsetX = 1;
        partyFontParams.fontParameters.shadowOffsetY = 1;
        partyFontParams.fontParameters.minFilter = TextureFilter.Nearest;
        partyFontParams.fontParameters.magFilter = TextureFilter.Nearest;
        partyFontParams.fontParameters.hinting = Hinting.Full;
        manager.load(PARTY_FONT_NAME + ".otf", BitmapFont.class, partyFontParams);


        TextureLoader.TextureParameter textureParams = new TextureLoader.TextureParameter();
        textureParams.minFilter = TextureFilter.Nearest;
        textureParams.magFilter = TextureFilter.Nearest;

        // UI Textures
        manager.load(DIALOGUE_BOX,   Texture.class, textureParams);
        manager.load(DIALOGUE_ARROW, Texture.class, textureParams);
        manager.load(HORIZONTAL_ARROW, Texture.class, textureParams);
        manager.load(TOWN_BOX,       Texture.class, textureParams);
        manager.load(BOX, Texture.class, textureParams);
        manager.load(BAG_UI, Texture.class, textureParams);
        manager.load(SUMMARY_UI, Texture.class, textureParams);
        
        // Atlases
        manager.load(PLAYER_ATLAS,     TextureAtlas.class);
        manager.load(PROF_BIRCH_ATLAS, TextureAtlas.class);
        manager.load(YOUNGSTER_ATLAS,  TextureAtlas.class);
        manager.load(MOM_ATLAS,  TextureAtlas.class);
        manager.load(NPC1,  TextureAtlas.class);
        manager.load(NPC2,  TextureAtlas.class);
        manager.load(DOOR_ATLAS, TextureAtlas.class);
        manager.load(BAG_ATLAS, TextureAtlas.class);
        manager.load(ITEM_ATLAS, TextureAtlas.class);
        manager.load(POKEMON_ICON_ATLAS, TextureAtlas.class);
        manager.load(PARTY_UI_ATLAS, TextureAtlas.class);
        manager.load(TYPE_ATLAS, TextureAtlas.class);
        manager.load(POKEBALLS_ATLAS, TextureAtlas.class);
        manager.load(SUMMARY_ATLAS, TextureAtlas.class); // ## CARREGANDO O NOVO ATLAS ##
    }

    public void dispose() {
        manager.dispose();
    }
}