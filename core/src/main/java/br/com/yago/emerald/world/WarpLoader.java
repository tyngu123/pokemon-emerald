package br.com.yago.emerald.world;

import br.com.yago.emerald.model.Warp;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class WarpLoader {

    public static void loadWarps(TiledMap map, Array<Warp> outWarps) {
        MapLayer layer = map.getLayers().get("warps");
        if (layer == null) {
            return;
        }

        for (MapObject object : layer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                
                String targetMap = object.getProperties().get("targetMap", String.class);
                int targetX = object.getProperties().get("targetX", 0, Integer.class);
                int targetY = object.getProperties().get("targetY", 0, Integer.class);
                String targetFacing = object.getProperties().get("targetFacing", "DOWN", String.class);

                // ## LENDO AS NOVAS PROPRIEDADES DO TILED ##
                boolean isDoor = object.getProperties().get("isDoor", false, Boolean.class);
                String doorType = object.getProperties().get("doorType", "", String.class);
                int doorX = object.getProperties().get("doorX", (int)(rect.x / 16), Integer.class);
                int doorY = object.getProperties().get("doorY", (int)(rect.y / 16), Integer.class);

                if (targetMap != null && !targetMap.isEmpty()) {
                    outWarps.add(new Warp(rect.x, rect.y, rect.width, rect.height, targetMap, targetX, targetY, targetFacing,
                                          isDoor, doorType, doorX, doorY));
                }
            }
        }
    }
}