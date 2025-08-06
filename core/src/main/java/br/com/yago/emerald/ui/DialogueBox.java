package br.com.yago.emerald.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;

public class DialogueBox {

    private final BitmapFont font;
    private final NinePatch background;
    private final Texture arrow;
    private final GlyphLayout layout = new GlyphLayout();

    private Array<String> pages = new Array<>();
    private int currentPageIndex = 0;

    private String currentPageText = "";
    private String visibleText     = "";
    private int    charIndex       = 0;
    private float  typewriterTimer = 0f;
    private final float charsPerSecond = 40f;

    private enum State { HIDDEN, TYPING, WAITING }
    private State currentState = State.HIDDEN;
    private float indicatorTimer = 0f;

    public DialogueBox(BitmapFont font, Texture bg9, Texture arrow) {
        this.font  = font;
        this.arrow = arrow;
        // ## ALTERAÇÃO AQUI: Garante que o NinePatch use as bordas corretas (8px) ##
        this.background = new NinePatch(bg9, 8, 8, 8, 8);
    }

    public void showText(String fullText) {
        paginateText(fullText, 320 - 30);
        currentPageIndex = 0;
        startNewPage();
    }

    public void update(float delta) {
        if (currentState == State.HIDDEN) return;
        
        switch (currentState) {
            case TYPING:  handleTyping(delta);  break;
            case WAITING: handleWaiting(delta); break;
            default: break;
        }
    }

    private void handleTyping(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            visibleText  = currentPageText;
            charIndex    = currentPageText.length();
            currentState = State.WAITING;
            return;
        }
        typewriterTimer += delta;
        if (typewriterTimer >= 1f/charsPerSecond) {
            typewriterTimer = 0f;
            if (charIndex < currentPageText.length()) {
                visibleText += currentPageText.charAt(charIndex++);
            } else {
                currentState = State.WAITING;
            }
        }
    }

    private void handleWaiting(float delta) {
        indicatorTimer += delta;
        if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            advancePage();
        }
    }

    private void advancePage() {
        currentPageIndex++;
        if (currentPageIndex >= pages.size) {
            currentState = State.HIDDEN;
        } else {
            startNewPage();
        }
    }

    private void startNewPage() {
        currentPageText   = pages.get(currentPageIndex);
        visibleText       = "";
        charIndex         = 0;
        typewriterTimer   = 0f;
        indicatorTimer    = 0f;
        currentState      = State.TYPING;
    }

    public void render(SpriteBatch batch, Viewport viewport) {
        if (currentState == State.HIDDEN) return;

        float boxHeight = 60f;
        float boxWidth = viewport.getWorldWidth() - 10f;
        float boxX = 5f;
        float boxY = 5f;

        background.draw(batch, boxX, boxY, boxWidth, boxHeight);

        float textMargin = 10f;
        float textX = boxX + textMargin;
        float textY = boxY + boxHeight - textMargin;
        float textWidth = boxWidth - (textMargin * 2);

        layout.setText(font, visibleText, font.getColor(), textWidth, Align.left, true);
        font.draw(batch, layout, textX, textY);
        
        if (currentState == State.WAITING) {
            if ((int)(indicatorTimer * 4) % 2 == 0) {
                float arrowSize = 8f;
                float arrowX = boxX + boxWidth - arrowSize - 10f;
                float arrowY = boxY + 10f;
                batch.draw(arrow, arrowX, arrowY, arrowSize, arrowSize);
            }
        }
    }

    public boolean isFinished() {
        return currentState == State.HIDDEN;
    }

    private void paginateText(String fullText, float boxWidth) {
        pages.clear();
        
        float usableWidth = boxWidth - 20f; 

        String[] words = fullText.split(" ");
        StringBuilder currentPage = new StringBuilder();
        int lineCount = 1;

        for (String word : words) {
            layout.setText(font, currentPage.toString() + " " + word);
            if (layout.width > usableWidth) {
                lineCount++;
                currentPage.append("\n");
            }
            
            if (lineCount > 2) {
                pages.add(currentPage.toString().trim());
                currentPage = new StringBuilder(word).append(" ");
                lineCount = 1;
            } else {
                 currentPage.append(word).append(" ");
            }
        }

        if (currentPage.toString().trim().length() > 0) {
            pages.add(currentPage.toString().trim());
        }
    }
}