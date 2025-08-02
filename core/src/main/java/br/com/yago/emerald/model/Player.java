package br.com.yago.emerald.model;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.utils.Array;

public class Player extends Character {

	private Direction nextDirection = null;
	private boolean interactionRequested = false;

	public Player(TextureAtlas atlas, TiledMapTileLayer collisionLayer, Array<Entity> allEntities, float startX,
			float startY) {
		super(atlas, collisionLayer, allEntities, startX, startY);
	}

	@Override
	protected Direction getDesiredDirection() {
		// Primeiro, checa se há um caminho a seguir (pathfinding).
		Direction pathDirection = super.getDesiredDirection();
		if (pathDirection != null) {
			return pathDirection;
		}
		// Se não, usa o input do jogador.
		return this.nextDirection;
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