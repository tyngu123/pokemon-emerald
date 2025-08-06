package br.com.yago.emerald.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Uma caixa de menu estilo RPG que aparece no lado direito da tela.
 * Gerencia seu próprio estado, seleção de itens e renderização.
 */
public class MenuBox {

    private enum State { HIDDEN, SHOWN }

    private final BitmapFont font;
    private final NinePatch background;
    private final Texture arrow;
    private final GlyphLayout layout = new GlyphLayout();

    private final Array<String> options = new Array<>();
    private int selectedIndex = 0;

    private State currentState = State.HIDDEN;

    public MenuBox(BitmapFont font, Texture boxTexture, Texture arrow) {
        this.font = font;
        this.arrow = arrow;
        // O NinePatch permite que a imagem de fundo se estique corretamente
        this.background = new NinePatch(boxTexture, 8,8,8, 8); 

        // Adicionando as opções iniciais do menu
        options.add("BAG");
        options.add("SAVE");
        options.add("OPTION");
        options.add("EXIT");
    }
    
    /**
     * Alterna a visibilidade do menu.
     */
    public void toggle() {
        if (currentState == State.HIDDEN) {
            show();
        } else {
            hide();
        }
    }

    public void show() {
        this.selectedIndex = 0; // Reseta a seleção para o topo
        this.currentState = State.SHOWN;
    }

    public void hide() {
        this.currentState = State.HIDDEN;
    }

    /**
     * Move a seleção para cima, com loop.
     */
    public void moveSelectionUp() {
        if (options.size > 0) {
            selectedIndex = (selectedIndex - 1 + options.size) % options.size;
        }
    }

    /**
     * Move a seleção para baixo, com loop.
     */
    public void moveSelectionDown() {
        if (options.size > 0) {
            selectedIndex = (selectedIndex + 1) % options.size;
        }
    }

    public void render(SpriteBatch batch, Viewport viewport) {
        if (currentState == State.HIDDEN) return;

        // ## CORREÇÃO 2: Coordenadas e tamanhos ajustados para a viewport de 320x240 ##
        float boxWidth = 100f; // Largura da caixa em pixels de jogo
        float boxHeight = 220f; // Altura da caixa
        float boxX = viewport.getWorldWidth() - boxWidth - 5f; // Alinha à direita com 5px de margem
        float boxY = viewport.getWorldHeight() - boxHeight - 5f; // Alinha ao topo com 5px de margem

        background.draw(batch, boxX, boxY, boxWidth, boxHeight);

        // Ajusta a escala da fonte para ficar nítida na resolução baixa
        font.getData().setScale(0.7f);

        for (int i = 0; i < options.size; i++) {
            String optionText = options.get(i);
            layout.setText(font, optionText);
            
            float textY = boxY + boxHeight - 20f - (i * 20f); // Espaçamento vertical
            float textX = boxX + 25f; // Posição do texto
            
            font.draw(batch, layout, textX, textY);
        }

        if (options.size > 0) {
            float arrowSize = 10f; // Tamanho da seta em pixels de jogo
            float arrowX = boxX + 10f; // Posição da seta
            
            layout.setText(font, options.get(selectedIndex)); // Recalcula o layout para a altura do texto
            float arrowY = boxY + boxHeight - 20f - (selectedIndex * 20f) - (layout.height / 2) - (arrowSize / 2) + 1f;

            batch.draw(arrow, arrowX, arrowY, arrowSize, arrowSize);
        }
        
        // Reseta a escala da fonte para não afetar outras UIs
        font.getData().setScale(1.0f);
    }

    public boolean isVisible() {
        return currentState == State.SHOWN;
    }
    
    public String getSelectedOption() {
        if (options.size > 0) {
            return options.get(selectedIndex);
        }
        return null;
    }
}