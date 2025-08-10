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
        // ## ADICIONANDO MOVIMENTOS AO MUDKIP ##
        mudkip.learnMove(new Move("Tackle", "A physical attack.", PokemonType.NORMAL, MoveCategory.PHYSICAL, 40, 100, 35));
        mudkip.learnMove(new Move("Growl", "Lowers the foe's Attack.", PokemonType.NORMAL, MoveCategory.STATUS, 0, 100, 40));
        mudkip.learnMove(new Move("Water Gun", "Squirts water to attack.", PokemonType.WATER, MoveCategory.SPECIAL, 40, 100, 25));


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
        // ## ADICIONANDO MOVIMENTOS AO SALAMENCE ##
        salamence.learnMove(new Move("Dragon Breath", "Breathes a powerful blast.", PokemonType.DRAGON, MoveCategory.SPECIAL, 60, 100, 20));
        salamence.learnMove(new Move("Fly", "Flies up high, then strikes.", PokemonType.FLYING, MoveCategory.PHYSICAL, 90, 95, 15));
        salamence.learnMove(new Move("Crunch", "Crunches with sharp fangs.", PokemonType.DARK, MoveCategory.PHYSICAL, 80, 100, 15));
        salamence.learnMove(new Move("Flamethrower", "A powerful fire attack.", PokemonType.FIRE, MoveCategory.SPECIAL, 90, 100, 15));

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
        // ## ADICIONANDO MOVIMENTOS AO PIKACHU ##
        pikachu.learnMove(new Move("Thunder Shock", "An electric shock attack.", PokemonType.ELECTRIC, MoveCategory.SPECIAL, 40, 100, 30));
        pikachu.learnMove(new Move("Quick Attack", "An attack that always goes first.", PokemonType.NORMAL, MoveCategory.PHYSICAL, 40, 100, 30));
        pikachu.learnMove(new Move("Iron Tail", "Slams with a steel-hard tail.", PokemonType.STEEL, MoveCategory.PHYSICAL, 100, 75, 15));
        pikachu.learnMove(new Move("Agility", "Sharply raises user's Speed.", PokemonType.PSYCHIC, MoveCategory.STATUS, 0, 0, 30));


	    // JIGGLYPUFF — 115 / 45 / 20 / 45 / 25 / 20
	    Ability cuteCharm = new Ability("Cute Charm", "May cause infatuation on contact.");
	    Map<String, Integer> jigglypuffBaseStats = new HashMap<>();
	    jigglypuffBaseStats.put("HP", 255);
	    jigglypuffBaseStats.put("ATTACK", 45);
	    jigglypuffBaseStats.put("DEFENSE", 20);
	    jigglypuffBaseStats.put("SPECIAL_ATTACK", 45);
	    jigglypuffBaseStats.put("SPECIAL_DEFENSE", 25);
	    jigglypuffBaseStats.put("SPEED", 20);

	    Pokemon jigglypuff = new Pokemon("DRAGONITE", "lalica", Pokemon.Gender.FEMALE, true, 100, jigglypuffBaseStats,
	            PokemonType.NORMAL, PokemonType.FAIRY, PokemonNature.MODEST, 5, "Route 115", Pokeball.POKEBALL, this.name,
	            cuteCharm, null, ExperienceGroup.FAST);
        // ## ADICIONANDO MOVIMENTOS AO EEVEE (Jigglypuff no código) ##
        jigglypuff.learnMove(new Move("Sing", "A soothing lullaby that may cause sleep.", PokemonType.NORMAL, MoveCategory.STATUS, 0, 55, 15));
        jigglypuff.learnMove(new Move("Body Slam", "A crushing body slam.", PokemonType.NORMAL, MoveCategory.PHYSICAL, 85, 100, 15));


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
        // ## ADICIONANDO MOVIMENTOS AO DIALGA ##
        dialga.learnMove(new Move("Roar of Time", "The user blasts the target with power that distorts even time.", PokemonType.DRAGON, MoveCategory.SPECIAL, 150, 90, 5));
        dialga.learnMove(new Move("Flash Cannon", "The user gathers all its light energy and releases it at once.", PokemonType.STEEL, MoveCategory.SPECIAL, 80, 100, 10));
        dialga.learnMove(new Move("Aura Sphere", "The user lets loose a blast of aura power from deep within its body.", PokemonType.FIGHTING, MoveCategory.SPECIAL, 80, 0, 20));
        dialga.learnMove(new Move("Metal Claw", "The target is raked with steel claws.", PokemonType.STEEL, MoveCategory.PHYSICAL, 50, 95, 35));


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