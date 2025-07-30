package br.com.yago.emerald.script;

import br.com.yago.emerald.model.Character;
import br.com.yago.emerald.view.GameScreen;
import com.badlogic.gdx.utils.JsonValue;

/**
 * A interface base para todos os comandos de script.
 */
public interface ScriptCommand {
    /**
     * Inicializa o comando com os dados do JSON.
     * @param data O objeto JsonValue contendo os parâmetros do comando.
     */
    void initialize(JsonValue data);

    /**
     * Inicia a execução do comando.
     * @param screen A tela de jogo, para acesso a objetos globais como o Player.
     * @param initiator O personagem que iniciou o script (o NPC).
     */
    void start(GameScreen screen, Character initiator);

    /**
     * Chamado a cada frame enquanto o comando está ativo.
     * @param delta O tempo desde o último frame.
     */
    void update(float delta);

    /**
     * @return true se o comando terminou sua execução.
     */
    boolean isFinished();
}
