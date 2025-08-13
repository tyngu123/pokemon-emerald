package br.com.yago.emerald.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ObjectMap;

public class AtlasFont {

    private final ObjectMap<Character, TextureRegion> charToRegionMap;
    private final float scale;
    private final int charSpacing;
    private final float spaceWidth;

    public AtlasFont(TextureAtlas atlas, float scale, int charSpacing) {
        this.charToRegionMap = new ObjectMap<>();
        this.scale = scale;
        this.charSpacing = charSpacing;
        this.spaceWidth = atlas.findRegion("alphabet-0").getRegionWidth() * scale; 
        mapCharacters(atlas);
    }

    private void mapCharacters(TextureAtlas atlas) {
        // Mapeia letras maiúsculas (A-Z -> alphabet-0 a alphabet-25)
        for (int i = 0; i < 26; i++) {
            char c = (char) ('A' + i);
            charToRegionMap.put(c, atlas.findRegion("alphabet-" + i));
        }
        // Mapeia letras minúsculas (a-z -> alphabet-26 a alphabet-51)
        for (int i = 0; i < 26; i++) {
            char c = (char) ('a' + i);
            charToRegionMap.put(c, atlas.findRegion("alphabet-" + (i + 26)));
        }
        
        // Mapeia números (0-9 -> alphabet-54 a alphabet-63)
        for (int i = 0; i < 10; i++) {
            char c = (char) ('0' + i);
            charToRegionMap.put(c, atlas.findRegion("alphabet-" + (i + 54)));
        }
        
        // ## NOVOS MAPEAMENTOS ADICIONADOS ##
        charToRegionMap.put('/', atlas.findRegion("alphabet-74"));
        charToRegionMap.put('♂', atlas.findRegion("alphabet-64")); // Símbolo Macho
        charToRegionMap.put('♀', atlas.findRegion("alphabet-69")); // Símbolo Fêmea
        
        // Mapeia outros caracteres necessários
        charToRegionMap.put('L', atlas.findRegion("alphabet-11"));
        charToRegionMap.put('v', atlas.findRegion("alphabet-47"));
    }

    public void draw(SpriteBatch batch, CharSequence text, float x, float y) {
        float currentX = x;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                currentX += spaceWidth;
                continue;
            }
            
            TextureRegion region = charToRegionMap.get(c);
            if (region != null) {
                batch.draw(region, currentX, y, region.getRegionWidth() * scale, region.getRegionHeight() * scale);
                currentX += (region.getRegionWidth() * scale) + charSpacing;
            }
        }
    }

    public float getWidth(CharSequence text) {
        float width = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                width += spaceWidth;
                continue;
            }
            TextureRegion region = charToRegionMap.get(c);
            if (region != null) {
                width += (region.getRegionWidth() * scale) + charSpacing;
            }
        }
        return width > 0 ? width - charSpacing : 0;
    }
}