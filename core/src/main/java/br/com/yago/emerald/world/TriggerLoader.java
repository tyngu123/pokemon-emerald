package br.com.yago.emerald.world;

import br.com.yago.emerald.model.Trigger;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class TriggerLoader {

    public static void loadTriggers(TiledMap map, Array<Trigger> outTriggers) {
        MapLayer layer = map.getLayers().get("triggers");
        if (layer == null) return;

        for (MapObject object : layer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                String scriptFile = object.getProperties().get("scriptFile", String.class);
                String runMode = object.getProperties().get("runMode", "ALWAYS", String.class);
                // ## MUDANÇA AQUI: Lendo a nova propriedade ##
                String triggerOn = object.getProperties().get("triggerOn", "ON_ENTER", String.class);

                if (scriptFile != null && !scriptFile.isEmpty()) {
                    // Passa a nova propriedade para o construtor
                    outTriggers.add(new Trigger(rect.x, rect.y, rect.width, rect.height, scriptFile, runMode, triggerOn));
                }
            }
        }
    }
}