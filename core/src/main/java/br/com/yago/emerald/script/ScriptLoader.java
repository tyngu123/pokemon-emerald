package br.com.yago.emerald.script;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class ScriptLoader {
    private final Json json = new Json();

    public Array<ScriptCommand> loadScript(String scriptFileName) {
        Array<ScriptCommand> commands = new Array<>();
        try {
            Array<JsonValue> root = json.fromJson(
                Array.class,
                Gdx.files.internal("scripts/" + scriptFileName)
            );
            if (root == null) {
                Gdx.app.error("ScriptLoader", "Script vazio ou mal formatado: " + scriptFileName);
                return commands;
            }
            for (JsonValue cmdData : root) {
                String type = cmdData.getString("type").toUpperCase();
                ScriptCommand cmd = createCommand(type);
                if (cmd != null) {
                    cmd.initialize(cmdData);
                    commands.add(cmd);
                }
            }
        } catch (Exception e) {
            Gdx.app.error("ScriptLoader", "Erro ao carregar script: " + scriptFileName, e);
        }
        return commands;
    }

    private ScriptCommand createCommand(String type) {
        switch (type) {
            case "FACE_PLAYER":     return new FacePlayerCommand();
            case "WAIT":            return new WaitCommand();
            case "SHOW_DIALOGUE":   return new DialogueCommand();
            case "SET_MOVEMENT":    return new SetMovementCommand();
            // ## ADICIONAR ESTA LINHA ##
            case "MOVE_TO":         return new MoveToCommand();
            default:
                Gdx.app.error("ScriptLoader", "Tipo desconhecido: " + type);
                return null;
        }
    }
}
