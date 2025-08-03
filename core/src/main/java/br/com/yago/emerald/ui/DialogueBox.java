package br.com.yago.emerald.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

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
        this.background = new NinePatch(bg9, 5,5,5,5);
    }

    public void showText(String fullText) {
        paginateText(fullText);
        currentPageIndex = 0;
        startNewPage();
    }

    public void update(float delta) {
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

    public void render(SpriteBatch batch, float viewportWidth) {
        if (currentState == State.HIDDEN) return;

        float boxX = 10f, boxY = 10f;
        float boxW = viewportWidth - 20f, boxH = 180f;

        // ## CORREÇÃO: REMOVIDO batch.begin() e batch.end() DAQUI ##
        
        background.draw(batch, boxX, boxY, boxW, boxH);

        float textX = boxX + 96f;
        float textY = boxY + boxH - 15f;
        float textWidth = boxW - 180f;

        layout.setText(font, visibleText, Color.WHITE, textWidth, Align.left, true);
        font.draw(batch, layout, textX, textY);

        if (currentState == State.WAITING) {
            if ((int)(indicatorTimer * 4) % 2 == 0) {
                GlyphLayout.GlyphRun lastRun = layout.runs.get(layout.runs.size - 1);
                float arrowX = textX + lastRun.width + 5f;
                float arrowY = textY - layout.height - 2f;
                float arrowSize = 32f;
                batch.draw(arrow, arrowX, arrowY, arrowSize, arrowSize);
            }
        }
    }

    public boolean isFinished() {
        return currentState == State.HIDDEN;
    }

    private void paginateText(String fullText) {
        pages.clear();
        float usableWidth = Gdx.graphics.getWidth() - 40f;
        float usableHeight = 100f;

        String[] words = fullText.split(" ");
        StringBuilder currentPage = new StringBuilder();

        for (String word : words) {
            layout.setText(font, currentPage.toString() + " " + word,
                Color.WHITE, usableWidth, Align.left, true);

            if (layout.height <= usableHeight) {
                currentPage.append(word).append(" ");
            } else {
                pages.add(currentPage.toString().trim());
                currentPage = new StringBuilder(word + " ");
            }
        }
        if (currentPage.length() > 0) {
            pages.add(currentPage.toString().trim());
        }
    }
}