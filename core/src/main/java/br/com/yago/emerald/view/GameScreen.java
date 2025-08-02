package br.com.yago.emerald.view;

import java.util.Comparator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import br.com.yago.emerald.assets.AssetsLoader;
import br.com.yago.emerald.config.GameConfig;
import br.com.yago.emerald.controller.PlayerInputHandler;
import br.com.yago.emerald.model.Character;
import br.com.yago.emerald.model.Entity;
import br.com.yago.emerald.model.NPC;
import br.com.yago.emerald.model.Player;
import br.com.yago.emerald.model.Trigger;
import br.com.yago.emerald.script.ScriptRunner;
import br.com.yago.emerald.ui.DialogueBox;
import br.com.yago.emerald.world.NpcLoader;
import br.com.yago.emerald.world.TriggerLoader;

public class GameScreen extends ScreenAdapter {

	private final SpriteBatch batch;
	private final BitmapFont uiFont;
	private final OrthographicCamera gameCamera;
	private final Viewport gameViewport;
	private final OrthographicCamera uiCamera;
	private final Viewport uiViewport;
	private final DialogueBox dialogueBox;
	private final TiledMap map;
	private final OrthogonalTiledMapRenderer mapRenderer;
	private final int[] mapBackgroundLayers, mapForegroundLayers;
	private final AssetsLoader assetLoader;
	private final Player player;
	private final Array<Entity> entities;
	private final Array<NPC> npcs = new Array<>();
	private final Array<Trigger> triggers = new Array<>();
	private final ScriptRunner scriptRunner;
	private final Comparator<Entity> yComparator = (e1, e2) -> Float.compare(e2.getWorldPosition().y, e1.getWorldPosition().y);

	public GameScreen() {
		assetLoader = new AssetsLoader();
		assetLoader.load();
		assetLoader.manager.finishLoading();

		uiFont = assetLoader.manager.get(assetLoader.FONT_NAME + ".otf", BitmapFont.class);
		Texture box9 = assetLoader.manager.get(assetLoader.DIALOGUE_BOX, Texture.class);
		Texture arr = assetLoader.manager.get(assetLoader.DIALOGUE_ARROW, Texture.class);

		gameCamera = new OrthographicCamera();
		gameViewport = new FitViewport(320, 240, gameCamera);
		uiCamera = new OrthographicCamera();
		uiViewport = new ScreenViewport(uiCamera);

		dialogueBox = new DialogueBox(uiFont, box9, arr);
		scriptRunner = new ScriptRunner(this);
		batch = new SpriteBatch();

		map = new TmxMapLoader().load("maps/alpha_map.tmx");
		mapRenderer = new OrthogonalTiledMapRenderer(map, 1f, batch);
		mapBackgroundLayers = new int[] { map.getLayers().getIndex("floor"), map.getLayers().getIndex("buildings"), map.getLayers().getIndex("trees") };
		mapForegroundLayers = new int[] { map.getLayers().getIndex("foreground") };

		TiledMapTileLayer collisionLayer = (TiledMapTileLayer) map.getLayers().get("collision");
		entities = new Array<>();
		player = new Player(assetLoader.manager.get(assetLoader.PLAYER_ATLAS), collisionLayer, entities, 10 * GameConfig.TILE_SIZE, 8 * GameConfig.TILE_SIZE);
		entities.add(player);

		NpcLoader.loadNpcs(map, collisionLayer, assetLoader, entities);
        for (Entity e : entities) {
            if (e instanceof NPC) npcs.add((NPC)e);
        }
		
        TriggerLoader.loadTriggers(map, triggers);

		Gdx.input.setInputProcessor(new PlayerInputHandler(player, this));
	}
	
	@Override
	public void resize(int width, int height) {
		gameViewport.update(width, height, true);
		uiViewport.update(width, height, true);
	}

	/**
	 * ## MÉTODO UPDATE TOTALMENTE REESTRUTURADO ##
	 * Esta é a correção principal que garante a ordem correta das operações.
	 */
	private void update(float delta) {
        // 1. Atualiza a lógica de TODAS as entidades (Jogador, NPCs).
        // Isso atualiza suas posições, animações, etc.
		for (Entity entity : entities) {
			entity.update(delta);
		}
        dialogueBox.update(delta); // Atualiza a caixa de diálogo (ex: efeito de digitação)

		// 2. ATUALIZA O ESTADO INTERNO DE TODOS OS GATILHOS.
		// Esta etapa agora acontece INCONDICIONALMENTE, a cada frame.
		// Isso garante que o jogo sempre saiba se o jogador entrou ou saiu de uma área,
		// mesmo que um script esteja rodando.
		for (Trigger trigger : triggers) {
			trigger.updateState(player);
		}

        // 3. Atualiza o script em execução OU checa por novos eventos.
        if (scriptRunner.isActive()) {
			// Se um script está ativo, apenas o atualizamos para que ele possa continuar.
            scriptRunner.update(delta);
        } else {
            // Se NENHUM script estiver ativo, o jogo está "livre" para iniciar novas ações.
            // a. Checa se o jogador tentou interagir com um NPC.
			handleInteraction();
			
			// b. Pede para cada gatilho TENTAR disparar. A lógica interna do gatilho decidirá se pode.
			for (Trigger trigger : triggers) {
				trigger.attemptToFire(player, scriptRunner);
			}
        }

        // 4. Atualiza a câmera para seguir o jogador.
        gameCamera.position.set(player.getWorldPosition().x + GameConfig.PLAYER_WIDTH / 2f, player.getWorldPosition().y + GameConfig.PLAYER_HEIGHT / 2f, 0f);
        gameCamera.update();
        mapRenderer.setView(gameCamera);
    }

	private void handleInteraction() {
		if (player.isInteractionRequested()) {
			player.clearInteractionRequest();
			int tx = player.getFacingGridX();
			int ty = player.getFacingGridY();
			for (NPC npc : npcs) {
				if (npc.getGridX() == tx && npc.getGridY() == ty && npc.getCurrentState() == Character.State.IDLE) {
					scriptRunner.triggerNpcScript(npc);
					break;
				}
			}
		}
	}

	@Override
	public void render(float delta) {
		update(delta);

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		mapRenderer.render(mapBackgroundLayers);
		batch.setProjectionMatrix(gameCamera.combined);
		batch.begin();
		entities.sort(yComparator);
		for (Entity e : entities) {
			TextureRegion frame = e.getCurrentFrame();
			if (frame == null) continue;
			batch.draw(frame, e.getWorldPosition().x, e.getWorldPosition().y, e.getWidth(), e.getHeight());
		}
		batch.end();
		mapRenderer.render(mapForegroundLayers);

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
	}
}