package br.com.yago.emerald.world;

import br.com.yago.emerald.assets.AssetsLoader;
import br.com.yago.emerald.model.Direction;
import br.com.yago.emerald.model.Entity;
import br.com.yago.emerald.model.NPC;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapLayer; // Importe MapLayer
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.utils.Array;

public class NpcLoader {

    public static void loadNpcs(TiledMap map, TiledMapTileLayer collisionLayer, AssetsLoader assetLoader, Array<Entity> entities) {
        // ## CORREÇÃO DEFINITIVA ##
        // Primeiro, pega a camada de NPCs.
        MapLayer layer = map.getLayers().get("npcs");

        // Se a camada não existir no mapa, simplesmente não faz nada e sai do método.
        if (layer == null) {
            return;
        }

        // Se a camada existe, continua o processo normalmente.
        MapObjects objects = layer.getObjects();

        for (MapObject object : objects) {
            float startX = object.getProperties().get("x", Float.class);
            float startY = object.getProperties().get("y", Float.class);

            String id = object.getProperties().get("id", "default_id", String.class);
            String npcType = object.getProperties().get("npcType", "player", String.class);
            String dialogueKey = object.getProperties().get("dialogueKey", "default_dialogue", String.class);
            String movementType = object.getProperties().get("movementType", "STATIONARY", String.class);
            String initialDirStr = object.getProperties().get("initialDirection", "DOWN", String.class);
            Direction initialDirection = Direction.valueOf(initialDirStr.toUpperCase());
            String scriptFile = object.getProperties().get("scriptFile", "", String.class);
            int movementRange = object.getProperties().get("movementRange", 0, Integer.class);

            boolean isVisible = object.isVisible();

            String atlasPath = "sprites_overworld/" + npcType + ".atlas";
            TextureAtlas atlas = assetLoader.manager.get(atlasPath, TextureAtlas.class);

            NPC npc = new NPC(id, npcType, dialogueKey, movementType, initialDirection, movementRange,
                atlas, collisionLayer, entities, startX, startY, scriptFile, isVisible);
            entities.add(npc);
        }
    }
}