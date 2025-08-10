package br.com.yago.emerald.model;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Player extends Character {

	private final String name;
	private Direction nextDirection = null;
	private boolean interactionRequested = false;
	private final Inventory inventory;
	private final Party party;

	public Player(String name, TextureAtlas atlas, TiledMapTileLayer collisionLayer, Array<Entity> allEntities,
			float startX, float startY) {
		super(atlas, collisionLayer, allEntities, startX, startY);
		this.name = name;
		this.inventory = new Inventory();
		this.party = new Party();
		addDefaultItems();
		addDefaultPokemon();
	}

	private void addDefaultItems() {
		inventory.addItem(new Item("Potion", "Heals a Pokémon by 20 HP.", "potion", Item.ItemCategory.ITEMS, 5));
		inventory.addItem(
				new Item("Super Potion", "Heals a Pokémon by 50 HP.", "super-potion", Item.ItemCategory.ITEMS, 2));
		inventory.addItem(new Item("Antidote", "Cures a poisoned Pokémon.", "antidote", Item.ItemCategory.ITEMS, 10));
		inventory.addItem(new Item("Poke Ball", "A device for catching wild Pokémon.", "pokeball",
				Item.ItemCategory.POKEBALLS, 20));
		inventory.addItem(
				new Item("Great Ball", "A good, high-performance Ball.", "greatball", Item.ItemCategory.POKEBALLS, 5));
		inventory.addItem(new Item("HM01 Cut", "A machine for teaching Cut.", "hm", Item.ItemCategory.TMS_HMS, 1));
	}

	private void addDefaultPokemon() {
	    Ability torrent = new Ability("Torrent", "Powers up Water-type moves.");
	    Item potion = new Item("Potion", "Heals a Pokémon by 20 HP.", "potion", Item.ItemCategory.ITEMS, 1);

	    Map<String, Integer> mudkipBaseStats = new HashMap<>();
	    mudkipBaseStats.put("HP", 50);
	    mudkipBaseStats.put("ATTACK", 70);
	    mudkipBaseStats.put("DEFENSE", 50);
	    mudkipBaseStats.put("SPECIAL_ATTACK", 50);
	    mudkipBaseStats.put("SPECIAL_DEFENSE", 50);
	    mudkipBaseStats.put("SPEED", 40);

	    Pokemon mudkip = new Pokemon("MUDKIP", "Mudkip", Pokemon.Gender.MALE, true, 5, mudkipBaseStats,
	            PokemonType.WATER, null, PokemonNature.HARDY, 5, "Littleroot Town", Pokeball.POKEBALL, this.name,
	            torrent, potion, ExperienceGroup.FAST);

	    Ability intimidate = new Ability("Intimidate", "Lowers the foe's Attack.");
	    Map<String, Integer> salamenceBaseStats = new HashMap<>();
	    salamenceBaseStats.put("HP", 95);
	    salamenceBaseStats.put("ATTACK", 135);
	    salamenceBaseStats.put("DEFENSE", 80);
	    salamenceBaseStats.put("SPECIAL_ATTACK", 110);
	    salamenceBaseStats.put("SPECIAL_DEFENSE", 80);
	    salamenceBaseStats.put("SPEED", 100);

	    Pokemon salamence = new Pokemon("SALAMENCE", "drago", Pokemon.Gender.FEMALE, true, 5, salamenceBaseStats,
	            PokemonType.DRAGON, PokemonType.FLYING, PokemonNature.NAUGHTY, 50, "Meteor Falls", Pokeball.GREATBALL,
	            this.name, intimidate, null, ExperienceGroup.SLOW);

	    // ---------- NOVOS (base stats oficiais) ----------
	    // PIKACHU — 35 / 55 / 40 / 50 / 50 / 90
	    Ability staticAbility = new Ability("Static", "Contact may cause paralysis.");
	    Map<String, Integer> pikachuBaseStats = new HashMap<>();
	    pikachuBaseStats.put("HP", 35);
	    pikachuBaseStats.put("ATTACK", 55);
	    pikachuBaseStats.put("DEFENSE", 40);
	    pikachuBaseStats.put("SPECIAL_ATTACK", 50);
	    pikachuBaseStats.put("SPECIAL_DEFENSE", 50);
	    pikachuBaseStats.put("SPEED", 90);

	    Pokemon pikachu = new Pokemon("PIKACHU", "fofuxo", Pokemon.Gender.MALE, false, 100, pikachuBaseStats,
	            PokemonType.ELECTRIC, null, PokemonNature.JOLLY, 5, "Viridian Forest", Pokeball.POKEBALL, this.name,
	            staticAbility, null, ExperienceGroup.FAST);

	    // JIGGLYPUFF — 115 / 45 / 20 / 45 / 25 / 20
	    Ability cuteCharm = new Ability("Cute Charm", "May cause infatuation on contact.");
	    Map<String, Integer> jigglypuffBaseStats = new HashMap<>();
	    jigglypuffBaseStats.put("HP", 255);
	    jigglypuffBaseStats.put("ATTACK", 45);
	    jigglypuffBaseStats.put("DEFENSE", 20);
	    jigglypuffBaseStats.put("SPECIAL_ATTACK", 45);
	    jigglypuffBaseStats.put("SPECIAL_DEFENSE", 25);
	    jigglypuffBaseStats.put("SPEED", 20);

	    Pokemon jigglypuff = new Pokemon("EEVEE", "lalica", Pokemon.Gender.FEMALE, true, 100, jigglypuffBaseStats,
	            PokemonType.NORMAL, PokemonType.FAIRY, PokemonNature.MODEST, 5, "Route 115", Pokeball.POKEBALL, this.name,
	            cuteCharm, null, ExperienceGroup.FAST);

	    // DIALGA — 100 / 120 / 120 / 150 / 100 / 90
	    Ability pressure = new Ability("Pressure", "Raises the foe's PP usage.");
	    Map<String, Integer> dialgaBaseStats = new HashMap<>();
	    dialgaBaseStats.put("HP", 100);
	    dialgaBaseStats.put("ATTACK", 120);
	    dialgaBaseStats.put("DEFENSE", 120);
	    dialgaBaseStats.put("SPECIAL_ATTACK", 150);
	    dialgaBaseStats.put("SPECIAL_DEFENSE", 100);
	    dialgaBaseStats.put("SPEED", 90);

	    Pokemon dialga = new Pokemon("DIALGA", "Dialga", Pokemon.Gender.GENDERLESS, false, 5, dialgaBaseStats,
	            PokemonType.STEEL, PokemonType.DRAGON, PokemonNature.MODEST, 5, "Spear Pillar", Pokeball.POKEBALL,
	            this.name, pressure, null, ExperienceGroup.SLOW);

	    // adicionar ao party
	    party.addPokemon(mudkip);
	    party.addPokemon(salamence);
	    party.addPokemon(pikachu);
	    party.addPokemon(jigglypuff);
	    party.addPokemon(dialga);
	}

	public String getName() {
		return name;
	}

	public Inventory getInventory() {
		return inventory;
	}

	public Party getParty() {
		return party;
	}

	@Override
	protected Direction getDesiredDirection() {
		Direction pathDirection = super.getDesiredDirection();
		if (pathDirection != null) {
			return pathDirection;
		}
		return this.nextDirection;
	}

	public void checkForWarp(Array<Warp> warps) {
		if (currentState == State.IDLE && nextDirection != null) {
			int targetX = getGridX() + nextDirection.getDx();
			int targetY = getGridY() + nextDirection.getDy();
			Rectangle targetBounds = new Rectangle(targetX * getWidth(), targetY * getHeight(), getWidth(),
					getHeight());
			for (Warp warp : warps) {
				if (warp.getBounds().overlaps(targetBounds)) {
				}
			}
		}
	}

	public void setNextDirection(Direction direction) {
		this.nextDirection = direction;
	}

	public void requestInteraction() {
		if (currentState == State.IDLE) {
			this.interactionRequested = true;
		}
	}

	public boolean isInteractionRequested() {
		return this.interactionRequested;
	}

	public void clearInteractionRequest() {
		this.interactionRequested = false;
	}

	public int getFacingGridX() {
		return getGridX() + facingDirection.getDx();
	}

	public int getFacingGridY() {
		return getGridY() + facingDirection.getDy();
	}
}