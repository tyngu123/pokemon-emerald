package br.com.yago.emerald.script;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class ScriptLoader {
	private final Json json = new Json();

	/**
	 * Verifica se o arquivo de script existe fisicamente dentro de scripts/.
	 */
	public boolean scriptFileExists(String scriptFileName) {
		if (scriptFileName == null || scriptFileName.trim().isEmpty())
			return false;
		return Gdx.files.internal("scripts/" + scriptFileName.trim()).exists();
	}

	/**
	 * Carrega só os comandos (compatível legado).
	 */
	public Array<ScriptCommand> loadScript(String scriptFileName) {
		Array<ScriptCommand> commands = new Array<>();
		try {
			JsonValue root = json.fromJson(null, Gdx.files.internal("scripts/" + scriptFileName));
			if (root == null) {
				Gdx.app.error("ScriptLoader", "Script vazio: " + scriptFileName);
				return commands;
			}

			// se for objeto com metadata, delega à ScriptDefinition e pega os comandos
			if (!root.isArray()) {
				ScriptDefinition def = ScriptDefinition.fromJson(scriptFileName, root, this);
				return def.getCommands();
			}

			// array simples
			for (JsonValue entry : root) {
				String type = entry.getString("type", "").toUpperCase();
				ScriptCommand cmd = createCommand(type);
				if (cmd == null)
					continue;
				cmd.initialize(entry);
				commands.add(cmd);
			}
		} catch (Exception e) {
			Gdx.app.error("ScriptLoader", "Erro carregando script: " + scriptFileName, e);
		}
		return commands;
	}

	/**
	 * Carrega a definição completa (com runMode/nextScript).
	 */
	public ScriptDefinition loadScriptDefinition(String scriptFileName) {
		try {
			JsonValue root = json.fromJson(null, Gdx.files.internal("scripts/" + scriptFileName));
			return ScriptDefinition.fromJson(scriptFileName, root, this);
		} catch (Exception e) {
			Gdx.app.error("ScriptLoader", "Erro carregando definição de script: " + scriptFileName, e);
			return new ScriptDefinition(scriptFileName, ScriptDefinition.RunMode.ALWAYS, null, new Array<>());
		}
	}

	public ScriptCommand createCommand(String type) {
		switch (type) {
		case "FACE_PLAYER":
			return new FacePlayerCommand();
		case "WAIT":
			return new WaitCommand();
		case "SHOW_DIALOGUE":
			return new DialogueCommand();
		case "SET_MOVEMENT":
			return new SetMovementCommand();
		case "MOVE_TO":
			return new MoveToCommand();
		case "TURN_TO":
			return new TurnToCommand(); // Nossa nova adição
		default:
			Gdx.app.error("ScriptLoader", "Tipo desconhecido: " + type);
			return null;
		}
	}
}
