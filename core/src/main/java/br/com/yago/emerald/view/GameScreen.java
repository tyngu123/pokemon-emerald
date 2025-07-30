package br.com.yago.emerald.view;

import br.com.yago.emerald.assets.AssetsLoader;
import br.com.yago.emerald.config.GameConfig;
import br.com.yago.emerald.controller.PlayerInputHandler;
import br.com.yago.emerald.model.Character;
import br.com.yago.emerald.model.Entity;
import br.com.yago.emerald.model.NPC;
import br.com.yago.emerald.model.Player;
import br.com.yago.emerald.script.ScriptCommand;
import br.com.yago.emerald.script.ScriptLoader;
import br.com.yago.emerald.script.ScriptRunner;
import br.com.yago.emerald.ui.DialogueBox;
import br.com.yago.emerald.world.NpcLoader;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Comparator;

public class GameScreen extends ScreenAdapter {

    private final SpriteBatch batch;
    private final BitmapFont uiFont;
    private final OrthographicCamera gameCamera;
    private final Viewport gameViewport;

    // UI
    private final OrthographicCamera uiCamera;
    private final Viewport uiViewport;
    private final ShapeRenderer shapeRenderer;
    private final DialogueBox dialogueBox;

    // Mundo
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final int[] mapBackgroundLayers, mapForegroundLayers;
    private final AssetsLoader assetLoader;
    private final Player player;
    private final Array<Entity> entities;
    private final Array<NPC> npcs = new Array<>();

    // Scripts
    private final ScriptLoader scriptLoader;
    private final ScriptRunner scriptRunner;

    private final Comparator<Entity> yComparator =
        (e1, e2) -> Float.compare(e2.getWorldPosition().y, e1.getWorldPosition().y);

    public GameScreen() {
        assetLoader = new AssetsLoader();
        assetLoader.load();
        assetLoader.manager.finishLoading();

        // Fonte UI
        uiFont = assetLoader.manager.get(assetLoader.FONT_NAME + ".otf", BitmapFont.class);

        // Texturas UI
        Texture box9 = assetLoader.manager.get(assetLoader.DIALOGUE_BOX, Texture.class);
        Texture arr  = assetLoader.manager.get(assetLoader.DIALOGUE_ARROW, Texture.class);

        // Cameras
        gameCamera  = new OrthographicCamera();
        gameViewport= new FitViewport(320, 240, gameCamera);
        uiCamera    = new OrthographicCamera();
        uiViewport  = new ScreenViewport(uiCamera);

        // DialogueBox
        shapeRenderer = new ShapeRenderer();
        dialogueBox   = new DialogueBox(uiFont, box9, arr);

        // Scripts
        scriptLoader = new ScriptLoader();
        scriptRunner = new ScriptRunner(this);

        // Batch
        batch = new SpriteBatch();

        // Carrega mapa
        map = new TmxMapLoader().load("maps/alpha_map.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1f, batch);
        mapBackgroundLayers = new int[]{
            map.getLayers().getIndex("floor"),
            map.getLayers().getIndex("buildings"),
            map.getLayers().getIndex("trees")
        };
        mapForegroundLayers = new int[]{
            map.getLayers().getIndex("foreground")
        };

        // Entidades
        TiledMapTileLayer collisionLayer =
            (TiledMapTileLayer) map.getLayers().get("collision");
        entities = new Array<>();
        player = new Player(
            assetLoader.manager.get(assetLoader.PLAYER_ATLAS),
            collisionLayer, entities,
            10 * GameConfig.TILE_SIZE,
            8  * GameConfig.TILE_SIZE
        );
        entities.add(player);

        // NPCs
        NpcLoader.loadNpcs(map, collisionLayer, assetLoader, entities);
        for (Entity e : entities) {
            if (e instanceof NPC) npcs.add((NPC)e);
        }

        // Input
        Gdx.input.setInputProcessor(new PlayerInputHandler(player, this));
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, true);
        uiViewport.update(width, height, true);
    }

    /**
     * Aqui está o pulo de update das entidades corrigido:
     * — primeiro sempre atualizamos todas as entidades,
     * — depois rodamos o script (se ativo) ou a lógica normal de interação.
     */
    private void update(float delta) {
        // 1) UI de diálogo
        dialogueBox.update(delta);

        // 2) SEMPRE atualizar entidades (player, npc, etc),
        //    para que qualquer WALKING progrida.
        for (int i = 0; i < entities.size; i++) {
            entities.get(i).update(delta);
        }

        // 3) Se há script ativo, atualiza o runner;
        //    senão processa tentativa de interação.
        if (scriptRunner.isActive()) {
            scriptRunner.update(delta);
        } else {
            handleInteraction();
        }

        // 4) Atualiza câmera do jogo
        gameCamera.position.set(
            player.getWorldPosition().x + GameConfig.PLAYER_WIDTH / 2f,
            player.getWorldPosition().y + GameConfig.PLAYER_HEIGHT / 2f,
            0f
        );
        gameCamera.update();
        mapRenderer.setView(gameCamera);
    }

    private void handleInteraction() {
        if (player.isInteractionRequested()) {
            player.clearInteractionRequest();
            int tx = player.getFacingGridX();
            int ty = player.getFacingGridY();
            for (NPC npc : npcs) {
                if (npc.getGridX() == tx
                    && npc.getGridY() == ty
                    && npc.getCurrentState() == Character.State.IDLE) {
                    Array<ScriptCommand> cmds = scriptLoader.loadScript(npc.getScriptFile());
                    scriptRunner.runScript(cmds, npc, null);
                    break;
                }
            }
        }
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render do mundo
        mapRenderer.render(mapBackgroundLayers);
        batch.setProjectionMatrix(gameCamera.combined);
        batch.begin();
        entities.sort(yComparator);
        for (Entity e : entities) {
            TextureRegion frame = e.getCurrentFrame();
            if (frame == null) continue;      // pula NPCs invisíveis ou sem atlas
            batch.draw(
                frame,
                e.getWorldPosition().x,
                e.getWorldPosition().y,
                e.getWidth(),
                e.getHeight()
            );
        }
        batch.end();
        mapRenderer.render(mapForegroundLayers);

        // Render da UI
        uiViewport.apply();
        batch.setProjectionMatrix(uiCamera.combined);
        dialogueBox.render(batch, uiViewport.getWorldWidth());
    }

    public DialogueBox getDialogueBox() { return dialogueBox; }
    public ScriptRunner getScriptRunner() { return scriptRunner; }
    public Player getPlayer() { return player; }

    @Override
    public void dispose() {
        batch.dispose();
        assetLoader.dispose();
        map.dispose();
        mapRenderer.dispose();
        shapeRenderer.dispose();
    }
}
