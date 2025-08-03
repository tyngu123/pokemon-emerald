package br.com.yago.emerald.model;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
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
		Direction pathDirection = super.getDesiredDirection();
		if (pathDirection != null) {
			return pathDirection;
		}
		return this.nextDirection;
	}

	/**
	 * ## NOVO MÉTODO ##
	 * Chamado a cada frame para checar se o jogador está tentando entrar em um warp.
	 * Esta lógica foi movida de GameScreen para cá para manter o código organizado.
	 */
	public void checkForWarp(Array<Warp> warps) {
		if (currentState == State.IDLE && nextDirection != null) {
			int targetX = getGridX() + nextDirection.getDx();
			int targetY = getGridY() + nextDirection.getDy();
			
			// Cria uma "caixa de colisão" para o ponto de destino
			Rectangle targetBounds = new Rectangle(targetX * getWidth(), targetY * getHeight(), getWidth(), getHeight());
			
			for (Warp warp : warps) {
				if (warp.getBounds().overlaps(targetBounds)) {
					// Encontrou um warp! A lógica de teletransporte será chamada pela GameScreen
					// através de um callback ou método direto.
					// Por enquanto, apenas logamos. A chamada real será feita na GameScreen.
					// Esta parte é um placeholder, a GameScreen vai iniciar o warp.
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