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
        this.background = new NinePatch(boxTexture, 8,8,8, 8); 

        options.add("BAG");
        options.add("SAVE");
        options.add("OPTION");
        options.add("EXIT");
    }
    
    public void toggle() {
        if (currentState == State.HIDDEN) {
            show();
        } else {
            hide();
        }
    }

    public void show() {
        this.selectedIndex = 0;
        this.currentState = State.SHOWN;
    }

    public void hide() {
        this.currentState = State.HIDDEN;
    }

    public void moveSelectionUp() {
        if (options.size > 0) {
            selectedIndex = (selectedIndex - 1 + options.size) % options.size;
        }
    }

    public void moveSelectionDown() {
        if (options.size > 0) {
            selectedIndex = (selectedIndex + 1) % options.size;
        }
    }

    public void render(SpriteBatch batch, Viewport viewport) {
        if (currentState == State.HIDDEN) return;

        // Mantendo os tamanhos da caixa, você pode ajustá-los se necessário
        float boxWidth = 80f;
        float boxHeight = 160f; // Altura ajustada para o novo espaçamento
        float boxX = viewport.getWorldWidth() - boxWidth ;
        float boxY = viewport.getWorldHeight() - boxHeight;

        background.draw(batch, boxX, boxY, boxWidth, boxHeight);

        // A fonte do menu já vem no tamanho correto, não precisa de setScale
        // font.getData().setScale(1.0f); // Removido para usar o tamanho original

        // ## ALTERAÇÃO PARA A MARGEM VERTICAL ##
        float verticalPadding = 10f; // Espaço do topo da caixa até o primeiro item
        float itemSpacing = 20f;     // Espaço entre cada item do menu

        for (int i = 0; i < options.size; i++) {
            String optionText = options.get(i);
            layout.setText(font, optionText);
            
            // Nova fórmula para a posição Y do texto
            float textY = boxY + boxHeight - verticalPadding - (i * itemSpacing);
            float textX = boxX + 15f; // Posição do texto ajustada
            
            font.draw(batch, layout, textX, textY);
        }

        if (options.size > 0) {
            float arrowSize = 7f; // Seta do tamanho da fonte
            float arrowX = boxX + 7f; // Posição da seta
            
            layout.setText(font, options.get(selectedIndex));
            
            // Nova fórmula para a posição Y da seta, correspondendo ao texto
            float arrowY = boxY + boxHeight - verticalPadding - (selectedIndex * itemSpacing) - (layout.height / 2) - (arrowSize / 2);

            batch.draw(arrow, arrowX, arrowY, arrowSize, arrowSize);
        }
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