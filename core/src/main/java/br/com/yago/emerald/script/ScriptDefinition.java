package br.com.yago.emerald.script;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Representa um script com metadados: runMode (ONCE / ALWAYS), próximo script
 * a ser encadeado e os comandos reais.
 */
public class ScriptDefinition {

    public enum RunMode { ONCE, ALWAYS }

    private final String name;
    private final RunMode runMode;
    private final String nextScript;
    private final Array<ScriptCommand> commands;

    public ScriptDefinition(String name, RunMode runMode, String nextScript, Array<ScriptCommand> commands) {
        this.name = name;
        this.runMode = runMode;
        this.nextScript = (nextScript != null && nextScript.trim().isEmpty()) ? null : nextScript;
        this.commands = commands;
    }

    public String getName() {
        return name;
    }

    public RunMode getRunMode() {
        return runMode;
    }

    public String getNextScript() {
        return nextScript;
    }

    public Array<ScriptCommand> getCommands() {
        return commands;
    }

    public static ScriptDefinition fromJson(String scriptFileName, JsonValue root, ScriptLoader loader) {
        // backward compatible: se o root é array, assume ALWAYS e sem nextScript.
        if (root == null) {
            Gdx.app.error("ScriptDefinition", "Root nulo ao carregar script: " + scriptFileName);
            return new ScriptDefinition(scriptFileName, RunMode.ALWAYS, null, new Array<>());
        }

        Array<ScriptCommand> commands = new Array<>();
        RunMode mode = RunMode.ALWAYS;
        String next = null;

        if (root.isArray()) {
            // formato antigo: apenas lista de comandos
            for (JsonValue entry : root) {
                String type = entry.getString("type", "").toUpperCase();
                ScriptCommand cmd = loader.createCommand(type);
                if (cmd == null) continue;
                cmd.initialize(entry);
                commands.add(cmd);
            }
        } else if (root.isObject()) {
            String runModeStr = root.getString("runMode", "ALWAYS").toUpperCase();
            try {
                mode = RunMode.valueOf(runModeStr);
            } catch (IllegalArgumentException e) {
                Gdx.app.error("ScriptDefinition", "runMode inválido em " + scriptFileName + ": " + runModeStr + ", usando ALWAYS");
                mode = RunMode.ALWAYS;
            }
            next = root.getString("nextScript", null);
            JsonValue cmds = root.get("commands");
            if (cmds != null && cmds.isArray()) {
                for (JsonValue entry : cmds) {
                    String type = entry.getString("type", "").toUpperCase();
                    ScriptCommand cmd = loader.createCommand(type);
                    if (cmd == null) continue;
                    cmd.initialize(entry);
                    commands.add(cmd);
                }
            } else {
                Gdx.app.error("ScriptDefinition", "Sem 'commands' array válido em " + scriptFileName);
            }
        } else {
            Gdx.app.error("ScriptDefinition", "Formato desconhecido de script: " + scriptFileName);
        }

        return new ScriptDefinition(scriptFileName, mode, next, commands);
    }
}
