package br.com.yago.emerald.world;

import br.com.yago.emerald.assets.AssetsLoader;
import br.com.yago.emerald.model.Direction;
import br.com.yago.emerald.model.Entity;
import br.com.yago.emerald.model.NPC;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.utils.Array;

public class NpcLoader {

    public static void loadNpcs(TiledMap map, TiledMapTileLayer collisionLayer, AssetsLoader assetLoader, Array<Entity> entities) {
        MapObjects objects = map.getLayers().get("npcs").getObjects();

        for (MapObject object : objects) {
            float startX = object.getProperties().get("x", Float.class);
            float startY = object.getProperties().get("y", Float.class);

            // Lendo as propriedades
            String id = object.getProperties().get("id", "default_id", String.class);
            String npcType = object.getProperties().get("npcType", "player", String.class);
            String dialogueKey = object.getProperties().get("dialogueKey", "default_dialogue", String.class);
            String movementType = object.getProperties().get("movementType", "STATIONARY", String.class);
            String initialDirStr = object.getProperties().get("initialDirection", "DOWN", String.class);
            Direction initialDirection = Direction.valueOf(initialDirStr.toUpperCase());
            String scriptFile = object.getProperties().get("scriptFile", "", String.class);
            // ## MUDANÇA AQUI: Lendo a nova propriedade 'movementRange' ##
            int movementRange = object.getProperties().get("movementRange", 0, Integer.class);

            boolean isVisible = object.isVisible();

            String atlasPath = "sprites_overworld/" + npcType + ".atlas";
            TextureAtlas atlas = assetLoader.manager.get(atlasPath, TextureAtlas.class);

            // Passando o movementRange para o construtor do NPC
            NPC npc = new NPC(id, npcType, dialogueKey, movementType, initialDirection, movementRange,
                atlas, collisionLayer, entities, startX, startY, scriptFile, isVisible);
            entities.add(npc);
        }
    }
}
