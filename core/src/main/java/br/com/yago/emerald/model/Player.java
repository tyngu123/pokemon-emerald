package br.com.yago.emerald.model;

import br.com.yago.emerald.EmeraldGame;
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
    private final EmeraldGame game;
    private Follower follower;

	public Player(String name, TextureAtlas atlas, TiledMapTileLayer collisionLayer, Array<Entity> allEntities,
			float startX, float startY, EmeraldGame game) {
		super(atlas, collisionLayer, allEntities, startX, startY);
		this.name = name;
		this.inventory = new Inventory();
		this.party = new Party();
        this.game = game;
		addDefaultItems();
		addDefaultPokemon();
	}
    
    public void setFollower(Follower follower) {
        this.follower = follower;
    }

	private void addDefaultItems() {
		inventory.addItem(new Item("Potion", "Heals a Pokémon by 20 HP.", "potion", Item.ItemCategory.ITEMS, 5));
		inventory.addItem(new Item("Poke Ball", "A device for catching wild Pokémon.", "pokeball", Item.ItemCategory.POKEBALLS, 20));
	}

    // Agora usa o GameDatabase para criar os Pokémon iniciais com XP específico
	private void addDefaultPokemon() {
	    // Pikachu com XP para estar um pouco acima do nível 5
	    party.addPokemon(game.db.createPokemon("LUGIA", "Ratinho", this.name, 30975)); 
	    
	    // Charizard com XP para estar um pouco acima do nível 7
	    party.addPokemon(game.db.createPokemon("CHARIZARD", "CHARIZARD", this.name, 300));
        
        // Eevee no nível 100
        party.addPokemon(game.db.createPokemon("CHARMANDER", "CHARMANDER", this.name, 300));
        
        // Blastoise com XP para estar um pouco acima do nível 36
        party.addPokemon(game.db.createPokemon("GENGAR", "BLASTOISE", this.name, 300));
        
        party.addPokemon(game.db.createPokemon("VENUSAUR", "VENUSAUR", this.name, 300));
        
        party.addPokemon(game.db.createPokemon("NIDOKING", "NIDOKING", this.name, 300));
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

	/**
     * ## MÉTODO SOBRESCRITO ##
     * Verifica a colisão com outras entidades, mas ignora o Pokémon seguidor.
     */
    @Override
    public boolean isOccupied(int gridX, int gridY) {
        if (allEntities == null) return false;
        for (int i = 0; i < allEntities.size; i++) {
            Entity other = allEntities.get(i);
            if (other == this || other instanceof Follower) { // Ignora a si mesmo e o seguidor
                continue;
            }
            if (other instanceof Character) {
                Character c = (Character) other;
                if (c.getGridX() == gridX && c.getGridY() == gridY) {
                    return true;
                }
            }
        }
        return false;
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
    
    @Override
    protected void onMovementComplete() {
        if (follower != null) {
            follower.addPathNode(getGridX(), getGridY(), facingDirection);
        }
    }
}