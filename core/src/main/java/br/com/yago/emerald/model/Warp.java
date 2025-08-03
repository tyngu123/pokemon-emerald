package br.com.yago.emerald.model;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Warp extends Entity {

    private final String targetMap;
    private final int targetX;
    private final int targetY;
    private final Direction targetFacing;

    public Warp(float x, float y, float width, float height, String targetMap, int targetX, int targetY, String targetFacingStr) {
        super(x, y, width, height);
        this.targetMap = targetMap;
        this.targetX = targetX;
        this.targetY = targetY;
        
        // ## CORREÇÃO ##
        // A conversão da string para o enum é feita de forma segura aqui.
        Direction tempFacing;
        try {
            tempFacing = Direction.valueOf(targetFacingStr.toUpperCase());
        } catch (Exception e) {
            tempFacing = Direction.DOWN; // Padrão seguro
        }
        this.targetFacing = tempFacing; // Atribuição única ao campo final.
    }

    // Getters para as propriedades do warp
    public String getTargetMap() { return targetMap; }
    public int getTargetX() { return targetX; }
    public int getTargetY() { return targetY; }
    public Direction getTargetFacing() { return targetFacing; }

    @Override
    public void update(float delta) {
        // Warps são estáticos, não precisam de lógica de update.
    }

    @Override
    public TextureRegion getCurrentFrame() {
        // Warps são invisíveis.
        return null;
    }
}