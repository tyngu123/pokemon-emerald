package br.com.yago.emerald.view;

import br.com.yago.emerald.EmeraldGame;
import br.com.yago.emerald.assets.AssetsLoader;
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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class BagScreen extends ScreenAdapter {

    private final EmeraldGame game;
    private final GameScreen gameScreen;
    private final SpriteBatch batch;
    private final AssetsLoader assetLoader;
    private final Texture background;
    private final Viewport viewport;
    private final OrthographicCamera camera;
    
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();
    private final Array<String> categories = new Array<>();
    private int currentCategoryIndex = 0;

    private enum AnimationState { IDLE, SLIDING_LEFT, SLIDING_RIGHT }
    private AnimationState animationState = AnimationState.IDLE;
    private float animationTimer = 0f;
    private static final float ANIMATION_DURATION = 0.2f;
    
    private final float titleX = 40f;
    private final float titleY = 220f;

    public BagScreen(EmeraldGame game, GameScreen gameScreen, SpriteBatch batch, AssetsLoader assetLoader) {
        this.game = game;
        this.gameScreen = gameScreen;
        this.batch = batch;
        this.assetLoader = assetLoader;

        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(320, 240, camera);

        this.background = assetLoader.manager.get(assetLoader.BAG_UI, Texture.class);
        
        // ## ALTERAÇÃO AQUI: Carrega a nova fonte de tamanho menor ##
        this.font = assetLoader.manager.get(assetLoader.BAG_FONT_NAME + ".otf", BitmapFont.class);
        
        categories.add("ITEMS");
        categories.add("POKé BALLS");
        categories.add("TMs & HMs");
        categories.add("BERRIES");
        categories.add("KEY ITEMS");
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (animationState == AnimationState.IDLE) {
                    if (keycode == Input.Keys.RIGHT) {
                        currentCategoryIndex = (currentCategoryIndex + 1) % categories.size;
                        animationState = AnimationState.SLIDING_LEFT;
                        animationTimer = 0;
                        return true;
                    }
                    if (keycode == Input.Keys.LEFT) {
                        currentCategoryIndex = (currentCategoryIndex - 1 + categories.size) % categories.size;
                        animationState = AnimationState.SLIDING_RIGHT;
                        animationTimer = 0;
                        return true;
                    }
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
        
        batch.end();
    }
    
    private void drawCategoryTitle() {
        String currentTitle = categories.get(currentCategoryIndex);
        
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
                String previousTitle = categories.get(previousIndex);
                float previousX = Interpolation.pow2Out.apply(titleX, titleX - slideDistance, progress);
                font.draw(batch, previousTitle, previousX, titleY);
            } 
            else if (animationState == AnimationState.SLIDING_RIGHT) {
                float currentX = Interpolation.pow2Out.apply(titleX - slideDistance, titleX, progress);
                font.draw(batch, currentTitle, currentX, titleY);
                
                int nextIndex = (currentCategoryIndex + 1) % categories.size;
                String nextTitle = categories.get(nextIndex);
                float nextX = Interpolation.pow2Out.apply(titleX, titleX + slideDistance, progress);
                font.draw(batch, nextTitle, nextX, titleY);
            }
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