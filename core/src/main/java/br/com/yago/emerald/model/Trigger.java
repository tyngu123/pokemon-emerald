package br.com.yago.emerald.model;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import br.com.yago.emerald.script.ScriptDefinition;
import br.com.yago.emerald.script.ScriptRunner;

public class Trigger extends Entity {

	public enum TriggerMode {
		ON_ENTER, ON_EXIT
	}

	public enum RunMode {
		ONCE, ALWAYS
	}

	private final String scriptFile;
	private final RunMode runMode;
	private final TriggerMode triggerMode;

	// --- LÓGICA DE ESTADO SIMPLIFICADA ---
	private boolean playerIsInside = false;
	private boolean wasInsideLastFrame = false;
	
	// A única trava necessária. Verdadeiro se o gatilho já foi usado.
	private boolean hasBeenTriggered = false;

	// Campos para armazenar os eventos de transição
	private boolean isEntering = false;
	private boolean isExiting = false;

	public Trigger(float x, float y, float width, float height, String scriptFile, String runModeStr,
			String triggerModeStr) {
		super(x, y, width, height);
		this.scriptFile = scriptFile;
		this.runMode = RunMode.valueOf(runModeStr.toUpperCase());
		this.triggerMode = TriggerMode.valueOf(triggerModeStr.toUpperCase());
	}

	@Override
	public void update(float delta) {
		// Este método não é necessário para a lógica do Trigger.
	}

	/**
	 * ## LÓGICA DE ATUALIZAÇÃO REATORADA ##
	 * Atualiza o estado do gatilho e reseta a trava do modo ALWAYS quando o jogador sai.
	 */
	public void updateState(Player player) {
		this.wasInsideLastFrame = this.playerIsInside;
		this.playerIsInside = getBounds().overlaps(player.getBounds());

		this.isEntering = playerIsInside && !wasInsideLastFrame;
		this.isExiting = !playerIsInside && wasInsideLastFrame;

		// A lógica chave que corrige os dois modos:
		if (isExiting && runMode == RunMode.ALWAYS) {
			// Se o jogador sai e o modo é ALWAYS, o gatilho é resetado e pode disparar de novo.
			this.hasBeenTriggered = false;
		}
	}

	/**
	 * Tenta disparar o script. A verificação é agora muito mais simples.
	 */
	public void attemptToFire(Player player, ScriptRunner scriptRunner) {
		boolean eventHappened = (isEntering && triggerMode == TriggerMode.ON_ENTER) ||
								(isExiting && triggerMode == TriggerMode.ON_EXIT);

		// Só dispara se um evento ocorreu e a trava única não estiver ativa.
		if (eventHappened && !hasBeenTriggered) {
			fire(player, scriptRunner);
		}
	}

	/**
	 * Dispara o script e ativa a trava.
	 */
	private void fire(Player player, ScriptRunner scriptRunner) {
		ScriptDefinition def = scriptRunner.getLoader().loadScriptDefinition(scriptFile);
		if (def != null) {
			scriptRunner.runDefinition(player, def, null);
			
			// Ativa a trava para TODOS os modos. O reset só acontece para ALWAYS em updateState.
			this.hasBeenTriggered = true;
		}
	}

	@Override
	public TextureRegion getCurrentFrame() {
		return null; // Triggers são invisíveis.
	}
}