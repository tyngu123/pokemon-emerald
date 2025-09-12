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

import br.com.yago.emerald.tools.SoundManager;

public class DialogueBox {

    private final BitmapFont font;
    private final NinePatch background;
    private final Texture arrow;
    private final GlyphLayout layout = new GlyphLayout();
    private final SoundManager soundManager;

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

    public DialogueBox(BitmapFont font, Texture bg9, Texture arrow, SoundManager soundManager) {
        this.font  = font;
        this.arrow = arrow;
        this.background = new NinePatch(bg9, 8, 8, 8, 8);
        this.soundManager = soundManager;
    }

    public void showText(String fullText) {
        paginateText(fullText, 200f - 10f - 20f);
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
            // ## SOM REMOVIDO DAQUI ##
            // O som de clique não toca mais ao adiantar o texto.
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
            // ## SOM MANTIDO AQUI ##
            // O som de clique toca ao confirmar a leitura e ir para a próxima página.
            soundManager.playClickSfx();
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
            if (layout.runs.size > 0) {
                GlyphLayout.GlyphRun lastRun = layout.runs.get(layout.runs.size - 1);
                
                float arrowSize = font.getCapHeight();
                float arrowX = textX + lastRun.width + 3f;
                float baseY = (textY - layout.height + font.getCapHeight() / 2f) - (arrowSize / 2f);

                float speed = 8f;
                float amplitude = 2f;
                float yOffset = (float) Math.sin(indicatorTimer * speed) * amplitude;
                float arrowY = baseY + yOffset;

                batch.draw(arrow, arrowX, arrowY, arrowSize, arrowSize);
            }
        }
    }

    public boolean isFinished() {
        return currentState == State.HIDDEN;
    }

    private void paginateText(String fullText, float usableWidth) {
        pages.clear();

        String[] words = fullText.split(" ");
        StringBuilder currentPage = new StringBuilder();
        
        for (String word : words) {
            String testString = currentPage.length() > 0 ? currentPage.toString() + " " + word : word;
            layout.setText(font, testString, font.getColor(), usableWidth, Align.left, true);

            if (layout.runs.size > 2) {
                pages.add(currentPage.toString().trim());
                currentPage = new StringBuilder(word).append(" ");
            } else {
                currentPage.append(word).append(" ");
            }
        }
        
        if (currentPage.toString().trim().length() > 0) {
            pages.add(currentPage.toString().trim());
        }
    }
}