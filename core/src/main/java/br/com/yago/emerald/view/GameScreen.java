package br.com.yago.emerald.view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.yago.emerald.EmeraldGame;
import br.com.yago.emerald.model.Door;
import br.com.yago.emerald.model.Warp;
import br.com.yago.emerald.script.MoveToCommand;
import br.com.yago.emerald.script.ScriptCommand;
import br.com.yago.emerald.world.WarpLoader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Queue;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
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
import br.com.yago.emerald.ui.MapNameBox;
import br.com.yago.emerald.ui.MenuBox;
import br.com.yago.emerald.world.NpcLoader;
import br.com.yago.emerald.world.TriggerLoader;

public class GameScreen extends ScreenAdapter {

	private final EmeraldGame game;
	private final SpriteBatch batch;
	private final BitmapFont uiFont;
	private final BitmapFont dialogueFont;
	private final OrthographicCamera gameCamera;
	private final Viewport gameViewport;
	private final OrthographicCamera uiCamera;
	private final Viewport uiViewport;
	private final DialogueBox dialogueBox;
	private final MapNameBox mapNameBox;
	private final MenuBox menuBox;
	private final TiledMap map;
	private final OrthogonalTiledMapRenderer mapRenderer;
	private final int[] mapBackgroundLayers, mapForegroundLayers;
	private final AssetsLoader assetLoader;
	private final Player player;
	private final PlayerInputHandler playerInputHandler;
	private final Array<Entity> entities;
	private final Array<NPC> npcs = new Array<>();
	private final Array<Trigger> triggers = new Array<>();
	private final Array<Warp> warps = new Array<>();
	private final Array<Door> doors = new Array<>();
	private final ScriptRunner scriptRunner;
	private final Comparator<Entity> yComparator = (e1, e2) -> Float.compare(e2.getWorldPosition().y, e1.getWorldPosition().y);
	private final Pathfinder pathfinder;
	private final FadeManager fadeManager;


	private final String mapName;
	private final boolean isInterior;

	public GameScreen(EmeraldGame game, AssetsLoader assetLoader, SpriteBatch batch, String mapFileName, int playerStartX, int playerStartY, Direction playerFacing, boolean isWarpIn) {
		this.game = game;
		this.assetLoader = assetLoader;
		this.batch = batch;
		this.fadeManager = new FadeManager();

		uiFont = assetLoader.manager.get(assetLoader.FONT_NAME + ".otf", BitmapFont.class);
		dialogueFont = assetLoader.manager.get(assetLoader.DIALOGUE_FONT_NAME + ".otf", BitmapFont.class);

		Texture menuBoxTexture = assetLoader.manager.get(assetLoader.BOX, Texture.class);
		Texture dialogueArrowTexture = assetLoader.manager.get(assetLoader.DIALOGUE_ARROW, Texture.class);
		Texture menuArrowTexture = assetLoader.manager.get(assetLoader.HORIZONTAL_ARROW, Texture.class);

		gameCamera = new OrthographicCamera();
		// ## MUDANÇA AQUI: Trocado FitViewport por ExtendViewport para eliminar as faixas pretas ##
		gameViewport = new ExtendViewport(240 , 160, gameCamera);
		uiCamera = new OrthographicCamera();
		uiViewport = new ExtendViewport(240, 160, uiCamera);

		NinePatch mapNameBoxNinePatch = new NinePatch(menuBoxTexture, 8, 8, 8, 8);

		dialogueBox = new DialogueBox(dialogueFont, menuBoxTexture, dialogueArrowTexture);
		mapNameBox = new MapNameBox(mapNameBoxNinePatch, uiFont);
		menuBox = new MenuBox(uiFont, menuBoxTexture, menuArrowTexture);

		scriptRunner = new ScriptRunner(this);

		map = new TmxMapLoader().load(mapFileName);

		this.mapName = map.getProperties().get("name", "Área Desconhecida", String.class);
		this.isInterior = map.getProperties().get("isInterior", false, Boolean.class);

		mapRenderer = new OrthogonalTiledMapRenderer(map, 1f, batch);

		List<Integer> backgroundIndexes = new ArrayList<>();
		List<Integer> foregroundIndexes = new ArrayList<>();
		String[] backgroundLayerNames = {"floor", "buildings", "trees"};
		String[] foregroundLayerNames = {"foreground"};

		for (MapLayer layer : map.getLayers()) {
			if (layer instanceof TiledMapTileLayer) {
				for (String name : backgroundLayerNames) {
					if (layer.getName().equalsIgnoreCase(name)) {
						backgroundIndexes.add(map.getLayers().getIndex(layer));
					}
				}
				for (String name : foregroundLayerNames) {
					if (layer.getName().equalsIgnoreCase(name)) {
						foregroundIndexes.add(map.getLayers().getIndex(layer));
					}
				}
			}
		}
		mapBackgroundLayers = backgroundIndexes.stream().mapToInt(i -> i).toArray();
		mapForegroundLayers = foregroundIndexes.stream().mapToInt(i -> i).toArray();

		TiledMapTileLayer collisionLayer = (TiledMapTileLayer) map.getLayers().get("collision");

		this.pathfinder = new Pathfinder(collisionLayer);

		entities = new Array<>();
		player = new Player(assetLoader.manager.get(assetLoader.PLAYER_ATLAS), collisionLayer, entities, playerStartX * GameConfig.TILE_SIZE, playerStartY * GameConfig.TILE_SIZE);
		player.setFacingDirection(playerFacing);
		entities.add(player);

		NpcLoader.loadNpcs(map, collisionLayer, assetLoader, entities);
		for (Entity e : entities) {
			if (e instanceof NPC) npcs.add((NPC)e);
		}

		TriggerLoader.loadTriggers(map, triggers);
		WarpLoader.loadWarps(map, warps);

		TextureAtlas doorAtlas = assetLoader.manager.get(assetLoader.DOOR_ATLAS, TextureAtlas.class);
		for (Warp warp : warps) {
			if (warp.isDoor()) {
				Door door = new Door(doorAtlas, warp.getDoorType(), warp.getDoorX(), warp.getDoorY());
				entities.add(door);
				doors.add(door);
			}
		}

		this.playerInputHandler = new PlayerInputHandler(player, this, game);
		Gdx.input.setInputProcessor(this.playerInputHandler);

		if (!this.isInterior) {
			mapNameBox.show(this.mapName);
		}

		if (isWarpIn) {
			runStepOutAnimation(playerFacing);
		}
	}

	private void runStepOutAnimation(Direction direction) {
		Array<ScriptCommand> stepOutSequence = new Array<>();
		MoveToCommand move = new MoveToCommand();
		move.initialize(new JsonValue(JsonValue.ValueType.object));
		move.setTarget("player");
		move.setDirection(direction);
		move.setDistance(1);
		stepOutSequence.add(move);
		scriptRunner.run(player, stepOutSequence);
	}

	@Override
	public void resize(int width, int height) {
		gameViewport.update(width, height, true);
		uiViewport.update(width, height, true);
	}

	private void update(float delta) {
		fadeManager.update(delta);
		playerInputHandler.update(delta);

		if (!menuBox.isVisible()) {
			for (Entity entity : entities) {
				entity.update(delta);
			}
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
		}

		dialogueBox.update(delta);
		mapNameBox.update(delta, uiViewport);

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
		batch.begin();
		mapNameBox.render(batch);
		dialogueBox.render(batch, uiViewport);
		menuBox.render(batch, uiViewport);
		fadeManager.render(batch, uiViewport.getWorldWidth(), uiViewport.getWorldHeight());
		batch.end();
	}

	public Warp getWarpAt(int gridX, int gridY) {
		Rectangle targetArea = new Rectangle(gridX * GameConfig.TILE_SIZE, gridY * GameConfig.TILE_SIZE, 1, 1);
		for (Warp warp : warps) {
			if (warp.getBounds().overlaps(targetArea)) {
				return warp;
			}
		}
		return null;
	}

	public Door getDoorAt(int gridX, int gridY) {
		for (Door door : doors) {
			if (door.getGridX() == gridX && door.getGridY() == gridY) {
				return door;
			}
		}
		return null;
	}

	public void initiateWarp(Warp warp) {
		game.warpTo(warp.getTargetMap(), warp.getTargetX(), warp.getTargetY(), warp.getTargetFacing());
	}

	public void startFadeIn() {
		fadeManager.fadeIn(0.5f);
	}

	public void startFadeOut(float duration, Runnable onComplete) {
		fadeManager.fadeOut(duration, onComplete);
	}

	public boolean isFading() {
		return fadeManager.isFading();
	}

	public void addEntity(Entity entity) {
		if (entity != null && !entities.contains(entity, true)) {
			entities.add(entity);
		}
	}

	public void removeEntity(Entity entity) {
		if (entity != null) {
			entities.removeValue(entity, true);
		}
	}

	public AssetsLoader getAssetLoader() {
		return assetLoader;
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
			if (entity == from) continue;
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
	public MenuBox getMenuBox() { return menuBox; }
	public ScriptRunner getScriptRunner() { return scriptRunner; }
	public Player getPlayer() { return player; }
	public PlayerInputHandler getInputHandler() {
		return playerInputHandler;
	}
	@Override
	public void dispose() {
		map.dispose();
		mapRenderer.dispose();
		fadeManager.dispose();
	}
}