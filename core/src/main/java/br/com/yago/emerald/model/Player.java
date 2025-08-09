package br.com.yago.emerald.model;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import br.com.yago.emerald.model.Pokemon.Gender;

public class Player extends Character {

	private final String name;
	private Direction nextDirection = null;
	private boolean interactionRequested = false;
	private final Inventory inventory;
	private final Party party;

	public Player(String name, TextureAtlas atlas, TiledMapTileLayer collisionLayer, Array<Entity> allEntities, float startX, float startY) {
		super(atlas, collisionLayer, allEntities, startX, startY);
		this.name = name;
		this.inventory = new Inventory();
		this.party = new Party();
		addDefaultItems();
		addDefaultPokemon();
	}

	private void addDefaultItems() {
		inventory.addItem(new Item("Potion", "Heals a Pokémon by 20 HP.", "potion", Item.ItemCategory.ITEMS, 5));
		inventory.addItem(new Item("Super Potion", "Heals a Pokémon by 50 HP.", "super-potion", Item.ItemCategory.ITEMS, 2));
		inventory.addItem(new Item("Antidote", "Cures a poisoned Pokémon.", "antidote", Item.ItemCategory.ITEMS, 10));
		inventory.addItem(new Item("Poke Ball", "A device for catching wild Pokémon.", "pokeball", Item.ItemCategory.POKEBALLS, 20));
		inventory.addItem(new Item("Great Ball", "A good, high-performance Ball.", "greatball", Item.ItemCategory.POKEBALLS, 5));
		inventory.addItem(new Item("HM01 Cut", "A machine for teaching Cut.", "hm", Item.ItemCategory.TMS_HMS, 1));
	}

	private void addDefaultPokemon() {
	    // ## HABILIDADES ADICIONADAS A CADA POKÉMON ##
		Ability torrent = new Ability("Torrent", "Powers up Water-type moves when the Pokémon's HP is low.");
		Pokemon mudkip = new Pokemon("MUDKIP", "Mudkip", Gender.MALE, true, 5, 25, 12, 10, 11, 10, 9, PokemonType.WATER, null, PokemonNature.HARDY, 5, "Littleroot Town", Pokeball.POKEBALL, this.name, torrent);

		Ability intimidate = new Ability("Intimidate", "Lowers the opposing Pokémon's Attack stat upon entering battle.");
		Pokemon salamence = new Pokemon("SALAMENCE", "drago", Gender.FEMALE, false, 50, 150, 135, 80, 110, 80, 100, PokemonType.DRAGON, PokemonType.FLYING, PokemonNature.NAUGHTY, 50, "Meteor Falls", Pokeball.GREATBALL, this.name, intimidate);

		Ability drought = new Ability("Drought", "Turns the sunlight harsh.");
		Pokemon groudon = new Pokemon("GROUDON", "Groudon", Gender.GENDERLESS, false, 100, 404, 438, 416, 328, 306, 306, PokemonType.GROUND, null, PokemonNature.ADAMANT, 70, "Cave of Origin", Pokeball.POKEBALL, this.name, drought);

		Ability cuteCharm = new Ability("Cute Charm", "May cause infatuation.");
		Pokemon jigglypuff = new Pokemon("JIGGLYPUFF", "lalinha", Gender.FEMALE, true, 100, 230, 10, 8, 12, 11, 12, PokemonType.NORMAL, PokemonType.FAIRY, PokemonNature.MODEST, 15, "Route 115", Pokeball.POKEBALL, this.name, cuteCharm);

		Ability pressure = new Ability("Pressure", "raises the foe's PP usage.");
		Pokemon articuno = new Pokemon("ARTICUNO", "Articuno", Gender.GENDERLESS, true, 90, 289, 185, 212, 203, 257, 185, PokemonType.ICE, PokemonType.FLYING, PokemonNature.TIMID, 50, "Seafoam Islands", Pokeball.GREATBALL, this.name, pressure);

		Ability staticAbility = new Ability("Static", "Contact cause paralysis.");
		Pokemon pikachu = new Pokemon("PIKACHU", "fofuxo", Gender.MALE, false, 100, 23, 10, 8, 12, 11, 12, PokemonType.ELECTRIC, null, PokemonNature.JOLLY, 100, "Viridian Forest", Pokeball.POKEBALL, this.name, staticAbility);
		
		party.addPokemon(mudkip);
		party.addPokemon(salamence);
		party.addPokemon(groudon);
		party.addPokemon(jigglypuff);
		party.addPokemon(articuno);
		party.addPokemon(pikachu);
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