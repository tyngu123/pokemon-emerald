package br.com.yago.emerald.model;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Player extends Character {

	private Direction nextDirection = null;
	private boolean interactionRequested = false;
	private final Inventory inventory;

	public Player(TextureAtlas atlas, TiledMapTileLayer collisionLayer, Array<Entity> allEntities, float startX,
			float startY) {
		super(atlas, collisionLayer, allEntities, startX, startY);
		this.inventory = new Inventory();
		addDefaultItems();
	}

	// ## MÉTODO ATUALIZADO PARA INCLUIR DESCRIÇÃO E NOME DO SPRITE ##
	private void addDefaultItems() {
		inventory.addItem(new Item("Potion", "Heals a Pokémon by 20 HP.", "potion", Item.ItemCategory.ITEMS, 5));
		inventory.addItem(new Item("Super Potion", "Heals a Pokémon by 50 HP.", "super-potion", Item.ItemCategory.ITEMS, 2));
		inventory.addItem(new Item("Antidote", "Cures a poisoned Pokémon.", "antidote", Item.ItemCategory.ITEMS, 10));
		inventory.addItem(new Item("Poke Ball", "A device for catching wild Pokémon.", "pokeball", Item.ItemCategory.POKEBALLS, 20));
		inventory.addItem(new Item("Great Ball", "A good, high-performance Ball.", "greatball", Item.ItemCategory.POKEBALLS, 5));
		inventory.addItem(new Item("HM01 Cut", "A machine for teaching Cut.", "hm", Item.ItemCategory.TMS_HMS, 1));
	}

	public Inventory getInventory() {
		return inventory;
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
			
			Rectangle targetBounds = new Rectangle(targetX * getWidth(), targetY * getHeight(), getWidth(), getHeight());
			
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