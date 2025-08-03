package br.com.yago.emerald.view;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import br.com.yago.emerald.assets.AssetsLoader;
import br.com.yago.emerald.config.GameConfig;
import br.com.yago.emerald.controller.PlayerInputHandler;
import br.com.yago.emerald.model.Character;
import br.com.yago.emerald.model.Direction;
import br.com.yago.emerald.model.Entity;
import br.com.yago.emerald.model.NPC;
import br.com.yago.emerald.model.Player;
import br.com.yago.emerald.model.Trigger;
import br.com.yago.emerald.model.pathfinding.Pathfinder;
import br.com.yago.emerald.script.ScriptRunner;
import br.com.yago.emerald.ui.DialogueBox;
import br.com.yago.emerald.ui.MapNameBox; // ## IMPORTA A NOVA CLASSE ##
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
	private final MapNameBox mapNameBox; // ## NOVA INSTÂNCIA DA UI ##
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
	private final Pathfinder pathfinder;
	
	private final String mapName; // ## ARMAZENA O NOME DO MAPA ##
	private final boolean isInterior; // ## ARMAZENA SE O MAPA É INTERIOR ##

	public GameScreen() {
		assetLoader = new AssetsLoader();
		assetLoader.load();
		assetLoader.manager.finishLoading();

		uiFont = assetLoader.manager.get(assetLoader.FONT_NAME + ".otf", BitmapFont.class);
		Texture box9 = assetLoader.manager.get(assetLoader.DIALOGUE_BOX, Texture.class);
		Texture arr = assetLoader.manager.get(assetLoader.DIALOGUE_ARROW, Texture.class);
		Texture townBoxTexture = assetLoader.manager.get(assetLoader.TOWN_BOX, Texture.class); // ## CARREGA A TEXTURA DA CAIXA ##

		gameCamera = new OrthographicCamera();
		gameViewport = new FitViewport(320, 240, gameCamera);
		uiCamera = new OrthographicCamera();
		uiViewport = new ScreenViewport(uiCamera);

		dialogueBox = new DialogueBox(uiFont, box9, arr);
		mapNameBox = new MapNameBox(townBoxTexture, uiFont); // ## INICIALIZA A CAIXA ##
		scriptRunner = new ScriptRunner(this);
		batch = new SpriteBatch();

		map = new TmxMapLoader().load("maps/alpha_map.tmx");
		
		// ## LÊ AS PROPRIEDADES DO MAPA ##
		this.mapName = map.getProperties().get("name", "Área Desconhecida", String.class);
		this.isInterior = map.getProperties().get("isInterior", false, Boolean.class);

		mapRenderer = new OrthogonalTiledMapRenderer(map, 1f, batch);
		mapBackgroundLayers = new int[] { map.getLayers().getIndex("floor"), map.getLayers().getIndex("buildings"), map.getLayers().getIndex("trees") };
		mapForegroundLayers = new int[] { map.getLayers().getIndex("foreground") };

		TiledMapTileLayer collisionLayer = (TiledMapTileLayer) map.getLayers().get("collision");
		
		this.pathfinder = new Pathfinder(collisionLayer);
		
		entities = new Array<>();
		player = new Player(assetLoader.manager.get(assetLoader.PLAYER_ATLAS), collisionLayer, entities, 10 * GameConfig.TILE_SIZE, 8 * GameConfig.TILE_SIZE);
		entities.add(player);

		NpcLoader.loadNpcs(map, collisionLayer, assetLoader, entities);
        for (Entity e : entities) {
            if (e instanceof NPC) npcs.add((NPC)e);
        }
		
        TriggerLoader.loadTriggers(map, triggers);

		Gdx.input.setInputProcessor(new PlayerInputHandler(player, this));

		// ## ATIVA A CAIXA SE O MAPA FOR EXTERIOR ##
		if (!this.isInterior) {
			mapNameBox.show(this.mapName);
		}
	}
	
	@Override
	public void resize(int width, int height) {
		gameViewport.update(width, height, true);
		uiViewport.update(width, height, true);
	}

	private void update(float delta) {
		for (Entity entity : entities) {
			entity.update(delta);
		}
        dialogueBox.update(delta);
		mapNameBox.update(delta, uiViewport); // ## ATUALIZA A CAIXA ##

		for (Trigger trigger : triggers) {
			trigger.updateState(player);
		}

        if (scriptRunner.isActive()) {
            scriptRunner.update(delta);
        } else {
			handleInteraction();
			
			for (Trigger trigger : triggers) {
				trigger.attemptToFire(player, scriptRunner);
			}
        }

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
		
		// ## RENDERIZA A CAIXA DE NOME DO MAPA ##
		batch.begin();
		mapNameBox.render(batch);
		batch.end();

		dialogueBox.render(batch, uiViewport.getWorldWidth());
	}
	
    public NPC getNpcById(String npcId) {
        if (npcId == null || npcId.trim().isEmpty()) return null;
        for (NPC npc : npcs) {
            if (npcId.equals(npc.getId())) {
                return npc;
            }
        }
        return null;
    }

    public Queue<Direction> findPathFor(Character from, Character to) {
        Vector2 startTile = new Vector2(from.getGridX(), from.getGridY());
        int targetX = to.getGridX();
        int targetY = to.getGridY();

        Set<Vector2> occupiedTiles = new HashSet<>();
        for (Entity entity : this.entities) {
            if (entity == from) {
                continue;
            }
            if (entity instanceof Character) {
                occupiedTiles.add(new Vector2(((Character) entity).getGridX(), ((Character) entity).getGridY()));
            }
        }

        Vector2 adjacentTarget = pathfinder.findBestAdjacent(from.getGridX(), from.getGridY(), targetX, targetY, occupiedTiles);

        if (adjacentTarget == null) {
            Gdx.app.log("GameScreen", "Nenhum tile adjacente caminhável encontrado para o alvo.");
            return new Queue<>();
        }

        occupiedTiles.add(new Vector2(targetX, targetY));
        return pathfinder.findPath(startTile, adjacentTarget, occupiedTiles);
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