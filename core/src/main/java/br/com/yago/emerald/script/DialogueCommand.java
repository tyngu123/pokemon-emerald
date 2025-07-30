package br.com.yago.emerald.script;

import br.com.yago.emerald.model.Character;
import br.com.yago.emerald.view.GameScreen;
import com.badlogic.gdx.utils.JsonValue;

public class DialogueCommand implements ScriptCommand {

    private String text;
    private GameScreen screen; // Guarda a referência da tela

    @Override
    public void initialize(JsonValue data) {
        this.text = data.getString("text", "");
    }

    @Override
    public void start(GameScreen screen, Character initiator) {
        this.screen = screen;
        screen.getDialogueBox().showText(text);
    }

    @Override
    public void update(float delta) {
        // A lógica de update está na DialogueBox, que é atualizada pela GameScreen
    }

    /**
     * O comando agora é "inteligente" e sabe quando terminou,
     * perguntando diretamente para a caixa de diálogo.
     */
    @Override
    public boolean isFinished() {
        return screen.getDialogueBox().isFinished();
    }
}
