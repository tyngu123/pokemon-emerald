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
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class BagScreen extends ScreenAdapter {

    private final EmeraldGame game;
    private final GameScreen gameScreen;
    private final SpriteBatch batch;
    private final AssetsLoader assetLoader;
    private final Texture background;
    private final Texture arrow; // ## SETA DE SELEÇÃO DE ITEM ##
    private final Viewport viewport;
    private final OrthographicCamera camera;

    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    // Lógica das categorias
    private final Array<Item.ItemCategory> categories = new Array<>();
    private int currentCategoryIndex = 0;

    // ## LÓGICA DOS ITENS ##
    private final Inventory inventory;
    private Array<String> itemsToDisplay = new Array<>();
    private int selectedItemIndex = 0;

    // Animação de transição de categoria
    private enum AnimationState { IDLE, SLIDING_LEFT, SLIDING_RIGHT }
    private AnimationState animationState = AnimationState.IDLE;
    private float animationTimer = 0f;
    private static final float ANIMATION_DURATION = 0.2f;
    
    private final float titleX = 35f;
    private final float titleY = 150f;

    public BagScreen(EmeraldGame game, GameScreen gameScreen, SpriteBatch batch, AssetsLoader assetLoader) {
        this.game = game;
        this.gameScreen = gameScreen;
        this.batch = batch;
        this.assetLoader = assetLoader;
        this.inventory = gameScreen.getPlayer().getInventory(); // Pega o inventário do jogador

        this.camera = new OrthographicCamera();
        this.viewport = new ExtendViewport(240, 160, camera);

        this.background = assetLoader.manager.get(assetLoader.BAG_UI, Texture.class);
        this.arrow = assetLoader.manager.get(assetLoader.HORIZONTAL_ARROW, Texture.class);
        this.font = assetLoader.manager.get(assetLoader.BAG_FONT_NAME + ".otf", BitmapFont.class);
        
        // Adiciona as categorias do Enum
        for(Item.ItemCategory cat : Item.ItemCategory.values()) {
            categories.add(cat);
        }

        updateItemList(); // Carrega a lista de itens da primeira categoria
    }
    
    private void updateItemList() {
        itemsToDisplay.clear();
        Item.ItemCategory currentCategory = categories.get(currentCategoryIndex);
        Array<Item> items = inventory.getItemsByCategory(currentCategory);
        
        for (Item item : items) {
            itemsToDisplay.add(item.toString());
        }
        itemsToDisplay.add("CLOSE BAG"); // Adiciona a opção de fechar
        selectedItemIndex = 0; // Reseta a seleção
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (animationState != AnimationState.IDLE) return true; // Ignora input durante animação

                // Navegação entre categorias
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

                // Navegação na lista de itens
                if (keycode == Input.Keys.DOWN) {
                    selectedItemIndex = (selectedItemIndex + 1) % itemsToDisplay.size;
                    return true;
                }
                if (keycode == Input.Keys.UP) {
                    selectedItemIndex = (selectedItemIndex - 1 + itemsToDisplay.size) % itemsToDisplay.size;
                    return true;
                }
                
                // Ação (Selecionar item ou fechar)
                if (keycode == Input.Keys.Z) {
                    if (itemsToDisplay.get(selectedItemIndex).equals("CLOSE BAG")) {
                        game.closeBagScreen();
                    } else {
                        // Lógica para usar um item pode ser adicionada aqui no futuro
                        Gdx.app.log("BagScreen", "Usou o item: " + itemsToDisplay.get(selectedItemIndex));
                    }
                    return true;
                }

                // Fechar a tela
                if (keycode == Input.Keys.X || keycode == Input.Keys.ESCAPE) {
                    game.closeBagScreen();
                    return true;
                }
                return false;
            }
        });
    }
    
    private void update(float delta) {
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
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(background, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        
        drawCategoryTitle();
        drawItemList(); // ## NOVO MÉTODO DE RENDERIZAÇÃO ##
        
        batch.end();
    }
    
    private void drawCategoryTitle() {
        String currentTitle = categories.get(currentCategoryIndex).getDisplayName();
        
        float slideDistance = 100f; 

        if (animationState == AnimationState.IDLE) {
            layout.setText(font, currentTitle);
            font.draw(batch, layout, titleX, titleY);
        } else {
            float progress = animationTimer / ANIMATION_DURATION;
            
            if (animationState == AnimationState.SLIDING_LEFT) {
                float currentX = Interpolation.pow2Out.apply(titleX + slideDistance, titleX, progress);
                font.draw(batch, currentTitle, currentX, titleY);
                
                int previousIndex = (currentCategoryIndex - 1 + categories.size) % categories.size;
                String previousTitle = categories.get(previousIndex).getDisplayName();
                float previousX = Interpolation.pow2Out.apply(titleX, titleX - slideDistance, progress);
                font.draw(batch, previousTitle, previousX, titleY);
            } 
            else if (animationState == AnimationState.SLIDING_RIGHT) {
                float currentX = Interpolation.pow2Out.apply(titleX - slideDistance, titleX, progress);
                font.draw(batch, currentTitle, currentX, titleY);
                
                int nextIndex = (currentCategoryIndex + 1) % categories.size;
                String nextTitle = categories.get(nextIndex).getDisplayName();
                float nextX = Interpolation.pow2Out.apply(titleX, titleX + slideDistance, progress);
                font.draw(batch, nextTitle, nextX, titleY);
            }
        }
    }
    
    private void drawItemList() {
        float itemBoxX = 110f;
        float itemBoxY = 140f;
        float itemBoxWidth = 15f;
        float itemBoxHeight = 5f;
        
        float verticalPadding = 5f;
        float itemSpacing = 20f;

        for (int i = 0; i < itemsToDisplay.size; i++) {
            String itemText = itemsToDisplay.get(i);
            layout.setText(font, itemText);
            
            float textY = itemBoxY + itemBoxHeight - verticalPadding - (i * itemSpacing);
            float textX = itemBoxX + 15f;
            
            font.draw(batch, layout, textX, textY);
        }
        
        // Desenha a seta de seleção
        if (itemsToDisplay.size > 0) {
            float arrowSize = 7f;
            float arrowX = itemBoxX + 5f;
            
            layout.setText(font, itemsToDisplay.get(selectedItemIndex));
            
            float arrowY = itemBoxY + itemBoxHeight - verticalPadding - (selectedItemIndex * itemSpacing) - (layout.height / 2) - (arrowSize / 2);

            batch.draw(arrow, arrowX, arrowY, arrowSize, arrowSize);
        }
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
        // Nada a descartar.
    }
}