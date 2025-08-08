package br.com.yago.emerald.view;

import br.com.yago.emerald.EmeraldGame;
import br.com.yago.emerald.assets.AssetsLoader;
import br.com.yago.emerald.model.Inventory;
import br.com.yago.emerald.model.Item;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class BagScreen extends ScreenAdapter {

    private final EmeraldGame game;
    private final GameScreen gameScreen;
    private final SpriteBatch batch;
    private final AssetsLoader assetLoader;
    private final Texture background;
    private final Texture arrow;
    private final Viewport viewport;
    private final OrthographicCamera camera;

    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    private final TextureAtlas bagAtlas;
    private final TextureRegion leftArrowRegion;
    private final TextureRegion rightArrowRegion;
    private final TextureAtlas itemAtlas;
    private final NinePatch menuBoxPatch;

    // ## MÁQUINA DE ESTADO ATUALIZADA ##
    private enum BagState { BROWSE, ACTION_MENU, TOSS_QUANTITY, TOSS_CONFIRM }
    private BagState bagState = BagState.BROWSE;

    private final Array<Item.ItemCategory> categories = new Array<>();
    private int currentCategoryIndex = 0;

    private final Inventory inventory;
    private final Array<Item> itemsToDisplay = new Array<>();
    private int selectedItemIndex = 0;

    private final Array<String> actionMenuOptions = new Array<>(new String[]{"USE", "GIVE", "TOSS", "CANCEL"});
    private int selectedActionIndex = 0;

    private final Array<String> confirmMenuOptions = new Array<>(new String[]{"YES", "NO"});
    private int selectedConfirmIndex = 0;
    
    // ## NOVO: Variável para guardar a quantidade a ser descartada ##
    private int tossQuantity = 1;

    private enum AnimationState { IDLE, SLIDING_LEFT, SLIDING_RIGHT }
    private AnimationState animationState = AnimationState.IDLE;
    private float animationTimer = 0f;
    private static final float ANIMATION_DURATION = 0.1f;

    private float arrowAnimTimer = 0f;

    private final float iconX = 35f;
    private final float iconY = 60f;
    private final float titleX = 30f;
    private final float titleY = 150f;
    private final float leftArrowX = titleX - 15f;
    private final float rightArrowX = titleX + 60f;
    private final float arrowsY = titleY - 12f;

    public BagScreen(EmeraldGame game, GameScreen gameScreen, SpriteBatch batch, AssetsLoader assetLoader) {
        this.game = game;
        this.gameScreen = gameScreen;
        this.batch = batch;
        this.assetLoader = assetLoader;
        this.inventory = gameScreen.getPlayer().getInventory();
        this.camera = new OrthographicCamera();
        this.viewport = new ExtendViewport(240, 160, camera);
        this.background = assetLoader.manager.get(assetLoader.BAG_UI, Texture.class);
        this.arrow = assetLoader.manager.get(assetLoader.HORIZONTAL_ARROW, Texture.class);
        this.font = assetLoader.manager.get(assetLoader.BAG_FONT_NAME + ".otf", BitmapFont.class);
        this.bagAtlas = assetLoader.manager.get(assetLoader.BAG_ATLAS, TextureAtlas.class);
        this.leftArrowRegion = bagAtlas.findRegion("leftarrow");
        this.rightArrowRegion = bagAtlas.findRegion("rightarrow");
        this.itemAtlas = assetLoader.manager.get(assetLoader.ITEM_ATLAS, TextureAtlas.class);
        this.menuBoxPatch = new NinePatch(assetLoader.manager.get(assetLoader.BOX, Texture.class), 8, 8, 8, 8);
        for (Item.ItemCategory cat : Item.ItemCategory.values()) {
            categories.add(cat);
        }
        updateItemList();
    }

    private void updateItemList() {
        itemsToDisplay.clear();
        Item.ItemCategory currentCategory = categories.get(currentCategoryIndex);
        itemsToDisplay.addAll(inventory.getItemsByCategory(currentCategory));
        if (selectedItemIndex >= itemsToDisplay.size && itemsToDisplay.size > 0) {
            selectedItemIndex = itemsToDisplay.size - 1;
        } else if (itemsToDisplay.size == 0) {
            selectedItemIndex = 0;
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                switch (bagState) {
                    case BROWSE: return handleBrowseInput(keycode);
                    case ACTION_MENU: return handleActionMenuInput(keycode);
                    case TOSS_QUANTITY: return handleTossQuantityInput(keycode);
                    case TOSS_CONFIRM: return handleTossConfirmInput(keycode);
                }
                return false;
            }
        });
    }

    private boolean handleBrowseInput(int keycode) {
        if (animationState != AnimationState.IDLE) return true;
        if (keycode == Input.Keys.RIGHT) {
            currentCategoryIndex = (currentCategoryIndex + 1) % categories.size;
            animationState = AnimationState.SLIDING_LEFT;
            animationTimer = 0;
            updateItemList();
            return true;
        }
        if (keycode == Input.Keys.LEFT) {
            currentCategoryIndex = (currentCategoryIndex - 1 + categories.size) % categories.size;
            animationState = AnimationState.SLIDING_RIGHT;
            animationTimer = 0;
            updateItemList();
            return true;
        }
        if (keycode == Input.Keys.DOWN) {
            selectedItemIndex = (selectedItemIndex + 1) % (itemsToDisplay.size + 1);
            return true;
        }
        if (keycode == Input.Keys.UP) {
            selectedItemIndex = (selectedItemIndex - 1 + (itemsToDisplay.size + 1)) % (itemsToDisplay.size + 1);
            return true;
        }
        if (keycode == Input.Keys.Z) {
            if (selectedItemIndex == itemsToDisplay.size) {
                game.closeBagScreen();
            } else if (itemsToDisplay.size > 0) {
                bagState = BagState.ACTION_MENU;
                selectedActionIndex = 0;
            }
            return true;
        }
        if (keycode == Input.Keys.X || keycode == Input.Keys.ESCAPE) {
            game.closeBagScreen();
            return true;
        }
        return false;
    }

    private boolean handleActionMenuInput(int keycode) {
        if (keycode == Input.Keys.DOWN) {
            selectedActionIndex = (selectedActionIndex + 1) % actionMenuOptions.size;
            return true;
        }
        if (keycode == Input.Keys.UP) {
            selectedActionIndex = (selectedActionIndex - 1 + actionMenuOptions.size) % actionMenuOptions.size;
            return true;
        }
        if (keycode == Input.Keys.X || keycode == Input.Keys.ESCAPE) {
            bagState = BagState.BROWSE;
            return true;
        }
        if (keycode == Input.Keys.Z) {
            String selectedOption = actionMenuOptions.get(selectedActionIndex);
            switch (selectedOption) {
                case "TOSS":
                    Item item = itemsToDisplay.get(selectedItemIndex);
                    if (item.getQuantity() > 1) {
                        tossQuantity = 1;
                        bagState = BagState.TOSS_QUANTITY;
                    } else {
                        tossQuantity = 1;
                        bagState = BagState.TOSS_CONFIRM;
                    }
                    break;
                case "CANCEL":
                    bagState = BagState.BROWSE;
                    break;
            }
            return true;
        }
        return false;
    }

    private boolean handleTossQuantityInput(int keycode) {
        Item item = itemsToDisplay.get(selectedItemIndex);
        if (keycode == Input.Keys.UP) {
            tossQuantity = Math.min(item.getQuantity(), tossQuantity + 1);
            return true;
        }
        if (keycode == Input.Keys.DOWN) {
            tossQuantity = Math.max(1, tossQuantity - 1);
            return true;
        }
        if (keycode == Input.Keys.Z) {
            bagState = BagState.TOSS_CONFIRM;
            selectedConfirmIndex = 0;
            return true;
        }
        if (keycode == Input.Keys.X || keycode == Input.Keys.ESCAPE) {
            bagState = BagState.ACTION_MENU;
            return true;
        }
        return false;
    }

    private boolean handleTossConfirmInput(int keycode) {
        if (keycode == Input.Keys.DOWN || keycode == Input.Keys.UP) {
            selectedConfirmIndex = (selectedConfirmIndex + 1) % confirmMenuOptions.size;
            return true;
        }
        if (keycode == Input.Keys.X || keycode == Input.Keys.ESCAPE) {
            Item item = itemsToDisplay.get(selectedItemIndex);
            if (item.getQuantity() > 1) {
                bagState = BagState.TOSS_QUANTITY;
            } else {
                bagState = BagState.ACTION_MENU;
            }
            return true;
        }
        if (keycode == Input.Keys.Z) {
            String selectedOption = confirmMenuOptions.get(selectedConfirmIndex);
            if ("YES".equals(selectedOption)) {
                Item itemToToss = itemsToDisplay.get(selectedItemIndex);
                inventory.removeItem(itemToToss, tossQuantity);
                updateItemList();
                bagState = BagState.BROWSE;
            } else {
                bagState = BagState.ACTION_MENU;
            }
            return true;
        }
        return false;
    }

    private void update(float delta) {
        arrowAnimTimer += delta;
        if (animationState != AnimationState.IDLE) {
            animationTimer += delta;
            if (animationTimer >= ANIMATION_DURATION) {
                animationState = AnimationState.IDLE;
                animationTimer = 0;
            }
        }
    }

    @Override
    public void render(float delta) {
        update(delta);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(background, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        drawCategoryIcon();
        drawCategoryTitle();
        drawItemList();
        drawCategoryArrows();
        drawItemDescription();
        if (bagState == BagState.ACTION_MENU) {
            drawActionMenu();
        } else if (bagState == BagState.TOSS_QUANTITY) {
            drawTossQuantitySelector();
        } else if (bagState == BagState.TOSS_CONFIRM) {
            drawTossConfirmation();
        }
        batch.end();
    }

    private void drawCategoryIcon() {
        Item.ItemCategory selectedCategory = categories.get(currentCategoryIndex);
        String regionName = "";
        switch (selectedCategory) {
            case ITEMS: regionName = "items"; break;
            case POKEBALLS: regionName = "pokeballs"; break;
            case TMS_HMS: regionName = "tmhm"; break;
            case BERRIES: regionName = "berries"; break;
            case KEY_ITEMS: regionName = "keyitems"; break;
        }
        if (!regionName.isEmpty()) {
            TextureRegion icon = bagAtlas.findRegion(regionName);
            if (icon != null) batch.draw(icon, iconX, iconY);
        }
    }

    private void drawCategoryTitle() {
        String currentTitle = categories.get(currentCategoryIndex).getDisplayName();
        layout.setText(font, currentTitle);
        float textX = titleX + ((rightArrowX - leftArrowX) / 2) - (layout.width / 2) - 5;
        if (animationState == AnimationState.IDLE) {
            font.draw(batch, layout, textX, titleY);
        } else {
            float progress = animationTimer / ANIMATION_DURATION;
            Color originalColor = font.getColor().cpy();
            if (animationState == AnimationState.SLIDING_LEFT) {
                float currentX = Interpolation.pow2Out.apply(textX + 100f, textX, progress);
                float currentAlpha = Interpolation.fade.apply(0f, 1f, progress);
                font.setColor(originalColor.r, originalColor.g, originalColor.b, currentAlpha);
                font.draw(batch, currentTitle, currentX, titleY);
                int previousIndex = (currentCategoryIndex - 1 + categories.size) % categories.size;
                String previousTitle = categories.get(previousIndex).getDisplayName();
                float previousX = Interpolation.pow2Out.apply(textX, textX - 100f, progress);
                float previousAlpha = Interpolation.fade.apply(1f, 0f, progress);
                font.setColor(originalColor.r, originalColor.g, originalColor.b, previousAlpha);
                font.draw(batch, previousTitle, previousX, titleY);
            } else if (animationState == AnimationState.SLIDING_RIGHT) {
                float currentX = Interpolation.pow2Out.apply(textX - 100f, textX, progress);
                float currentAlpha = Interpolation.fade.apply(0f, 1f, progress);
                font.setColor(originalColor.r, originalColor.g, originalColor.b, currentAlpha);
                font.draw(batch, currentTitle, currentX, titleY);
                int nextIndex = (currentCategoryIndex + 1) % categories.size;
                String nextTitle = categories.get(nextIndex).getDisplayName();
                float nextX = Interpolation.pow2Out.apply(textX, textX + 100f, progress);
                float nextAlpha = Interpolation.fade.apply(1f, 0f, progress);
                font.setColor(originalColor.r, originalColor.g, originalColor.b, nextAlpha);
                font.draw(batch, nextTitle, nextX, titleY);
            }
            font.setColor(originalColor);
        }
    }

    private void drawCategoryArrows() {
        if (animationState == AnimationState.IDLE) {
            float xOffset = (float) Math.sin(arrowAnimTimer * 8f) * 2f;
            batch.draw(leftArrowRegion, leftArrowX - xOffset, arrowsY);
            batch.draw(rightArrowRegion, rightArrowX + xOffset, arrowsY);
        }
    }

    private void drawItemList() {
        float itemBoxX = 110f;
        float itemBoxY = 140f;
        float itemBoxHeight = 5f;
        float verticalPadding = 5f;
        float itemSpacing = 20f;
        for (int i = 0; i < itemsToDisplay.size; i++) {
            font.draw(batch, itemsToDisplay.get(i).toString(), itemBoxX + 15f, itemBoxY + itemBoxHeight - verticalPadding - (i * itemSpacing));
        }
        String closeText = "CLOSE BAG";
        font.draw(batch, closeText, itemBoxX + 15f, itemBoxY + itemBoxHeight - verticalPadding - (itemsToDisplay.size * itemSpacing));
        if (bagState == BagState.BROWSE) {
            float arrowSize = 7f;
            float arrowX = itemBoxX + 5f;
            float arrowY;
            if (selectedItemIndex == itemsToDisplay.size) {
                layout.setText(font, closeText);
                arrowY = itemBoxY + itemBoxHeight - verticalPadding - (itemsToDisplay.size * itemSpacing) - (layout.height / 2f) - (arrowSize / 2f);
            } else {
                layout.setText(font, itemsToDisplay.get(selectedItemIndex).toString());
                arrowY = itemBoxY + itemBoxHeight - verticalPadding - (selectedItemIndex * itemSpacing) - (layout.height / 2) - (arrowSize / 2);
            }
            batch.draw(arrow, arrowX, arrowY, arrowSize, arrowSize);
        }
    }

    private void drawItemDescription() {
        if (selectedItemIndex >= itemsToDisplay.size) return;
        Item selectedItem = itemsToDisplay.get(selectedItemIndex);
        TextureRegion itemSprite = itemAtlas.findRegion(selectedItem.getSpriteName());
        if (itemSprite != null) {
            batch.draw(itemSprite, 4f, 60f, 32, 32);
        }
        if (bagState == BagState.TOSS_QUANTITY) {
            String qtyMessage = "Toss how many?";
            layout.setText(font, qtyMessage, new Color(0.815f, 0.815f, 0.784f, 1f), 105f, Align.left, true);
            font.draw(batch, layout, 4f, 17f + layout.height + 5);
        } else if (bagState == BagState.TOSS_CONFIRM) {
            String tossMessage = "Is it okay to throw away " + tossQuantity + " " + selectedItem.getName() + "(s)?";
            layout.setText(font, tossMessage, new Color(0.815f, 0.815f, 0.784f, 1f), 105f, Align.left, true);
            font.draw(batch, layout, 4f, 17f + layout.height + 5);
        } else {
            layout.setText(font, selectedItem.getDescription(), new Color(0.815f, 0.815f, 0.784f, 1f), 105f, Align.left, true);
            font.draw(batch, layout, 4f, 17f + layout.height + 5);
        }
    }

    private void drawActionMenu() {
        float boxWidth = 80f;
        float boxHeight = 95f;
        float boxX = viewport.getWorldWidth() - boxWidth - 5f;
        float boxY = 5f;
        menuBoxPatch.draw(batch, boxX, boxY, boxWidth, boxHeight);
        float verticalPadding = 10f;
        float itemSpacing = 20f;
        for (int i = 0; i < actionMenuOptions.size; i++) {
            font.draw(batch, actionMenuOptions.get(i), boxX + 15f, boxY + boxHeight - verticalPadding - (i * itemSpacing));
        }
        layout.setText(font, actionMenuOptions.get(selectedActionIndex));
        float arrowY = boxY + boxHeight - verticalPadding - (selectedActionIndex * itemSpacing) - (layout.height / 2f) - (7f / 2f);
        batch.draw(arrow, boxX + 5f, arrowY, 7f, 7f);
    }

    private void drawTossQuantitySelector() {
        float boxWidth = 80f;
        float boxHeight = 40f;
        float boxX = viewport.getWorldWidth() - boxWidth - 5f;
        float boxY = 5f;
        menuBoxPatch.draw(batch, boxX, boxY, boxWidth, boxHeight);
        String quantityStr = "x" + String.format("%02d", tossQuantity);
        layout.setText(font, quantityStr);
        float textX = boxX + boxWidth - layout.width - 10f;
        float textY = boxY + (boxHeight + layout.height) / 2f;
        font.draw(batch, quantityStr, textX, textY);
    }

    private void drawTossConfirmation() {
        float boxWidth = 60f;
        float boxHeight = 50f;
        float boxX = viewport.getWorldWidth() - boxWidth - 5f;
        float boxY = 5f;
        menuBoxPatch.draw(batch, boxX, boxY, boxWidth, boxHeight);
        float verticalPadding = 10f;
        float itemSpacing = 20f;
        for (int i = 0; i < confirmMenuOptions.size; i++) {
            font.draw(batch, confirmMenuOptions.get(i), boxX + 20f, boxY + boxHeight - verticalPadding - (i * itemSpacing));
        }
        layout.setText(font, confirmMenuOptions.get(selectedConfirmIndex));
        float arrowY = boxY + boxHeight - verticalPadding - (selectedConfirmIndex * itemSpacing) - (layout.height / 2f) - (7f / 2f);
        batch.draw(arrow, boxX + 10f, arrowY, 7f, 7f);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(gameScreen.getInputHandler());
    }

    @Override
    public void dispose() {}
}