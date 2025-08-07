package br.com.yago.emerald.view;

import br.com.yago.emerald.EmeraldGame;
import br.com.yago.emerald.assets.AssetsLoader;
import br.com.yago.emerald.model.Inventory;
import br.com.yago.emerald.model.Item;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
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
    // ## NOVO: Atlas com os sprites dos itens ##
    private final TextureAtlas itemAtlas;

    // Lógica das categorias
    private final Array<Item.ItemCategory> categories = new Array<>();
    private int currentCategoryIndex = 0;

    // Lógica dos itens
    private final Inventory inventory;
    // ## MUDANÇA: Agora guardamos os objetos Item, não apenas Strings ##
    private final Array<Item> itemsToDisplay = new Array<>();
    private int selectedItemIndex = 0;

    // Animação de transição de categoria
    private enum AnimationState { IDLE, SLIDING_LEFT, SLIDING_RIGHT }
    private AnimationState animationState = AnimationState.IDLE;
    private float animationTimer = 0f;
    private static final float ANIMATION_DURATION = 0.2f;

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
        
        // ## NOVO: Carregando o atlas de itens ##
        this.itemAtlas = assetLoader.manager.get(assetLoader.ITEM_ATLAS, TextureAtlas.class);

        for(Item.ItemCategory cat : Item.ItemCategory.values()) {
            categories.add(cat);
        }

        updateItemList();
    }

    private void updateItemList() {
        itemsToDisplay.clear();
        Item.ItemCategory currentCategory = categories.get(currentCategoryIndex);
        // ## MUDANÇA: Adiciona diretamente os objetos Item ##
        itemsToDisplay.addAll(inventory.getItemsByCategory(currentCategory));
        selectedItemIndex = 0;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
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

                // ## MUDANÇA: O tamanho da lista agora é itemsToDisplay.size + 1 (por causa do "CLOSE BAG") ##
                if (keycode == Input.Keys.DOWN) {
                    selectedItemIndex = (selectedItemIndex + 1) % (itemsToDisplay.size + 1);
                    return true;
                }
                if (keycode == Input.Keys.UP) {
                    selectedItemIndex = (selectedItemIndex - 1 + (itemsToDisplay.size + 1)) % (itemsToDisplay.size + 1);
                    return true;
                }

                if (keycode == Input.Keys.Z) {
                    // Se o item selecionado for o último da lista, é o "CLOSE BAG"
                    if (selectedItemIndex == itemsToDisplay.size) {
                        game.closeBagScreen();
                    } else {
                        Gdx.app.log("BagScreen", "Usou o item: " + itemsToDisplay.get(selectedItemIndex).getName());
                    }
                    return true;
                }

                if (keycode == Input.Keys.X || keycode == Input.Keys.ESCAPE) {
                    game.closeBagScreen();
                    return true;
                }
                return false;
            }
        });
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
        // ## NOVO: Desenha a caixa de descrição ##
        drawItemDescription();

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
            if (icon != null) {
                batch.draw(icon, iconX, iconY);
            }
        }
    }

    private void drawCategoryTitle() {
        String currentTitle = categories.get(currentCategoryIndex).getDisplayName();
        
        layout.setText(font, currentTitle);
        float textX = titleX + ( (rightArrowX - leftArrowX) / 2 ) - (layout.width / 2) - 5;
        
        float slideDistance = 100f; 

        if (animationState == AnimationState.IDLE) {
            font.draw(batch, layout, textX, titleY);
        } else {
            float progress = animationTimer / ANIMATION_DURATION;
            
            if (animationState == AnimationState.SLIDING_LEFT) {
                float currentX = Interpolation.pow2Out.apply(textX + slideDistance, textX, progress);
                font.draw(batch, currentTitle, currentX, titleY);
                
                int previousIndex = (currentCategoryIndex - 1 + categories.size) % categories.size;
                String previousTitle = categories.get(previousIndex).getDisplayName();
                float previousX = Interpolation.pow2Out.apply(textX, textX - slideDistance, progress);
                font.draw(batch, previousTitle, previousX, titleY);
            } 
            else if (animationState == AnimationState.SLIDING_RIGHT) {
                float currentX = Interpolation.pow2Out.apply(textX - slideDistance, textX, progress);
                font.draw(batch, currentTitle, currentX, titleY);
                
                int nextIndex = (currentCategoryIndex + 1) % categories.size;
                String nextTitle = categories.get(nextIndex).getDisplayName();
                float nextX = Interpolation.pow2Out.apply(textX, textX + slideDistance, progress);
                font.draw(batch, nextTitle, nextX, titleY);
            }
        }
    }

    private void drawCategoryArrows() {
        if (animationState == AnimationState.IDLE) {
            float speed = 8f;
            float amplitude = 2f;
            float xOffset = (float) Math.sin(arrowAnimTimer * speed) * amplitude;
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

        // Desenha a lista de itens
        for (int i = 0; i < itemsToDisplay.size; i++) {
            Item currentItem = itemsToDisplay.get(i);
            String itemText = currentItem.toString(); // Ex: "POTION X5"
            layout.setText(font, itemText);
            
            float textY = itemBoxY + itemBoxHeight - verticalPadding - (i * itemSpacing);
            float textX = itemBoxX + 15f;
            
            font.draw(batch, layout, textX, textY);
        }
        
        // Desenha a opção "CLOSE BAG"
        String closeText = "CLOSE BAG";
        layout.setText(font, closeText);
        float closeY = itemBoxY + itemBoxHeight - verticalPadding - (itemsToDisplay.size * itemSpacing);
        float closeX = itemBoxX + 15f;
        font.draw(batch, layout, closeX, closeY);

        // Desenha a seta de seleção
        float arrowSize = 7f;
        float arrowX = itemBoxX + 5f;
        float arrowY;
        
        if (selectedItemIndex == itemsToDisplay.size) { // Seta está no "CLOSE BAG"
            layout.setText(font, closeText);
            arrowY = closeY - (layout.height / 2f) - (arrowSize / 2f);
        } else { // Seta está em um item
             layout.setText(font, itemsToDisplay.get(selectedItemIndex).toString());
             arrowY = itemBoxY + itemBoxHeight - verticalPadding - (selectedItemIndex * itemSpacing) - (layout.height / 2) - (arrowSize / 2);
        }
        batch.draw(arrow, arrowX, arrowY, arrowSize, arrowSize);
    }
    
    // ## NOVO MÉTODO PARA DESENHAR A DESCRIÇÃO E O SPRITE DO ITEM ##
    private void drawItemDescription() {
        // A caixa de descrição só aparece se um item estiver selecionado (e não a opção "CLOSE BAG")
        if (selectedItemIndex >= itemsToDisplay.size) {
            return;
        }

        Item selectedItem = itemsToDisplay.get(selectedItemIndex);
        
        // Desenha o sprite do item
        TextureRegion itemSprite = itemAtlas.findRegion(selectedItem.getSpriteName());
        if (itemSprite != null) {
            float spriteX = 4f;
            float spriteY = 60f;
            batch.draw(itemSprite, spriteX, spriteY, 32, 32); // Tamanho fixo para o sprite
        }
        
        // Desenha a descrição do item
        float descBoxX = 4f;
        float descBoxY = 17f;
        float descBoxWidth = 105f;
        
        layout.setText(font, selectedItem.getDescription(), font.getColor(), descBoxWidth, Align.left, true);
        font.draw(batch, layout, descBoxX, descBoxY + layout.height + 5);
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
    public void dispose() {
        // Nada a descartar
    }
}