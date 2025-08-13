package br.com.yago.emerald.view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Queue;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import br.com.yago.emerald.EmeraldGame;
import br.com.yago.emerald.assets.AssetsLoader;
import br.com.yago.emerald.config.GameConfig;
import br.com.yago.emerald.controller.PlayerInputHandler;
import br.com.yago.emerald.database.GameDatabase;
import br.com.yago.emerald.model.Character;
import br.com.yago.emerald.model.Direction;
import br.com.yago.emerald.model.Door;
import br.com.yago.emerald.model.EncounterData;
import br.com.yago.emerald.model.Entity;
import br.com.yago.emerald.model.NPC;
import br.com.yago.emerald.model.Player;
import br.com.yago.emerald.model.Pokemon;
import br.com.yago.emerald.model.Trigger;
import br.com.yago.emerald.model.Warp;
import br.com.yago.emerald.model.pathfinding.Pathfinder;
import br.com.yago.emerald.script.FadeOutCommand;
import br.com.yago.emerald.script.MoveToCommand;
import br.com.yago.emerald.script.ScriptCommand;
import br.com.yago.emerald.script.ScriptRunner;
import br.com.yago.emerald.script.WaitCommand;
import br.com.yago.emerald.ui.DialogueBox;
import br.com.yago.emerald.ui.MapNameBox;
import br.com.yago.emerald.ui.MenuBox;
import br.com.yago.emerald.world.NpcLoader;
import br.com.yago.emerald.world.TriggerLoader;
import br.com.yago.emerald.world.WarpLoader;

public class GameScreen extends ScreenAdapter {

	private final EmeraldGame game;
	private final SpriteBatch batch;
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
	private final GameDatabase db;
	private final Array<EncounterData> wildEncounters = new Array<>();

	private final String mapName;
	private final boolean isInterior;
	private TiledMapTileLayer grassLayer;
    private boolean inBattle = false;

	public GameScreen(EmeraldGame game, AssetsLoader assetLoader, SpriteBatch batch, String mapFileName, Player player, int playerStartX, int playerStartY, Direction playerFacing, boolean isWarpIn) {
		this.game = game;
		this.assetLoader = assetLoader;
		this.batch = batch;
        this.player = player;
		this.fadeManager = new FadeManager();
        this.db = game.db;

		BitmapFont uiFont = assetLoader.manager.get(assetLoader.FONT_NAME + ".otf", BitmapFont.class);
		BitmapFont dialogueFont = assetLoader.manager.get(assetLoader.DIALOGUE_FONT_NAME + ".otf", BitmapFont.class);

		Texture menuBoxTexture = assetLoader.manager.get(assetLoader.BOX, Texture.class);
		Texture dialogueArrowTexture = assetLoader.manager.get(assetLoader.DIALOGUE_ARROW, Texture.class);
		Texture menuArrowTexture = assetLoader.manager.get(assetLoader.HORIZONTAL_ARROW, Texture.class);

		gameCamera = new OrthographicCamera();
		gameViewport = new ExtendViewport(240 , 160, gameCamera);
		uiCamera = new OrthographicCamera();
		uiViewport = new ExtendViewport(240, 160, uiCamera);

		NinePatch mapNameBoxNinePatch = new NinePatch(menuBoxTexture, 8, 8, 8, 8);

		dialogueBox = new DialogueBox(dialogueFont, menuBoxTexture, dialogueArrowTexture);
		mapNameBox = new MapNameBox(mapNameBoxNinePatch, uiFont);
		menuBox = new MenuBox(uiFont, menuBoxTexture, menuArrowTexture);

		scriptRunner = new ScriptRunner(this);

		map = new TmxMapLoader().load(mapFileName);
		this.grassLayer = (TiledMapTileLayer) map.getLayers().get("grass");
        loadEncounterData();

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
        player.setCollisionLayer(collisionLayer);
        player.setAllEntities(entities);
        player.getWorldPosition().set(playerStartX * GameConfig.TILE_SIZE, playerStartY * GameConfig.TILE_SIZE);
        player.getStartPosition().set(player.getWorldPosition());
        player.getTargetPosition().set(player.getWorldPosition());
        player.setFacingDirection(playerFacing);
        player.setVisible(true);
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

    private void loadEncounterData() {
        String encounterFilePath = map.getProperties().get("encounterFile", String.class);
        if (encounterFilePath == null || encounterFilePath.isEmpty()) {
            Gdx.app.log("GameScreen", "Mapa não possui propriedade 'encounterFile'. Nenhum encontro selvagem será carregado.");
            return;
        }
        
        if (Gdx.files.internal(encounterFilePath).exists()) {
            Json json = new Json();
            Array<EncounterData> encounters = json.fromJson(Array.class, EncounterData.class, Gdx.files.internal(encounterFilePath));
            wildEncounters.addAll(encounters);
            Gdx.app.log("GameScreen", wildEncounters.size + " encontros carregados de: " + encounterFilePath);
        } else {
            Gdx.app.error("GameScreen", "Arquivo de encontro definido no mapa não foi encontrado: " + encounterFilePath);
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
        
        if (player.isMovementJustFinished()) {
            checkForBattle();
            player.clearMovementJustFinished();
        }

		dialogueBox.update(delta);
		mapNameBox.update(delta, uiViewport);
		gameCamera.position.set(player.getWorldPosition().x + GameConfig.PLAYER_WIDTH / 2f, player.getWorldPosition().y + GameConfig.PLAYER_HEIGHT / 2f, 0f);
		gameCamera.update();
		mapRenderer.setView(gameCamera);
	}
    
    private void checkForBattle() {
        if (inBattle || grassLayer == null) return;
        int playerX = player.getGridX();
        int playerY = player.getGridY();
        if (grassLayer.getCell(playerX, playerY) != null) {
            if (MathUtils.random(1, 100) <= 15) { // 15% chance
                startBattle();
            }
        }
    }
    
    private void startBattle() {
        playerInputHandler.clearPressedKeys();
        final Pokemon wildPokemon = selectRandomEncounter();

        if (wildPokemon == null) {
            Gdx.app.log("GameScreen", "Nenhum encontro selvagem para iniciar batalha.");
            return;
        }

        Array<ScriptCommand> preBattleSequence = new Array<>();
        WaitCommand wait = new WaitCommand();
        JsonValue waitData = new JsonValue(JsonValue.ValueType.object);
        waitData.addChild("duration", new JsonValue(0.4f));
        wait.initialize(waitData);
        preBattleSequence.add(wait);

        FadeOutCommand fade = new FadeOutCommand();
        JsonValue fadeData = new JsonValue(JsonValue.ValueType.object);
        fadeData.addChild("duration", new JsonValue(0.4f));
        fade.initialize(fadeData);
        preBattleSequence.add(fade);
        
        scriptRunner.runScript(preBattleSequence, player, () -> {
            game.openBattleScreen(wildPokemon);
        });
    }

    private Pokemon selectRandomEncounter() {
        if (wildEncounters.size == 0) return null;

        int totalChance = 0;
        for (EncounterData data : wildEncounters) {
            totalChance += data.chance;
        }

        if (totalChance == 0) return null;

        int randomRoll = MathUtils.random(1, totalChance);
        EncounterData selectedEncounter = null;
        int cumulativeChance = 0;
        for (EncounterData data : wildEncounters) {
            cumulativeChance += data.chance;
            if (randomRoll <= cumulativeChance) {
                selectedEncounter = data;
                break;
            }
        }

        if (selectedEncounter == null) selectedEncounter = wildEncounters.first();
        int level = MathUtils.random(selectedEncounter.minLevel, selectedEncounter.maxLevel);
        
        return db.createPokemon(selectedEncounter.speciesName, level, null, "");
    }
    
    public void onBattleEnd() {
        Array<ScriptCommand> postBattleSequence = new Array<>();
        WaitCommand wait = new WaitCommand();
        JsonValue waitData = new JsonValue(JsonValue.ValueType.object);
        waitData.addChild("duration", new JsonValue(1.0f));
        wait.initialize(waitData);
        postBattleSequence.add(wait);
        scriptRunner.run(player, postBattleSequence);
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
    
    public boolean isInBattle() {
        return inBattle;
    }

    public void setInBattle(boolean inBattle) {
        this.inBattle = inBattle;
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
	public PlayerInputHandler getInputHandler() { return playerInputHandler; }

	@Override
	public void dispose() {
		map.dispose();
		mapRenderer.dispose();
		fadeManager.dispose();
	}
}