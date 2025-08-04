package br.com.yago.emerald.model;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Warp extends Entity {

    private final String targetMap;
    private final int targetX;
    private final int targetY;
    private final Direction targetFacing;
    // ## NOVAS PROPRIEDADES PARA PORTAS ##
    private final boolean isDoor;
    private final String doorType;
    private final int doorX;
    private final int doorY;

    public Warp(float x, float y, float width, float height, String targetMap, int targetX, int targetY, String targetFacingStr,
                boolean isDoor, String doorType, int doorX, int doorY) {
        super(x, y, width, height);
        this.targetMap = targetMap;
        this.targetX = targetX;
        this.targetY = targetY;
        
        Direction tempFacing;
        try {
            tempFacing = Direction.valueOf(targetFacingStr.toUpperCase());
        } catch (Exception e) {
            tempFacing = Direction.DOWN;
        }
        this.targetFacing = tempFacing;
        
        this.isDoor = isDoor;
        this.doorType = doorType;
        this.doorX = doorX;
        this.doorY = doorY;
    }

    // Getters para as propriedades do warp
    public String getTargetMap() { return targetMap; }
    public int getTargetX() { return targetX; }
    public int getTargetY() { return targetY; }
    public Direction getTargetFacing() { return targetFacing; }
    
    // ## GETTERS PARA AS NOVAS PROPRIEDADES ##
    public boolean isDoor() { return isDoor; }
    public String getDoorType() { return doorType; }
    public int getDoorX() { return doorX; }
    public int getDoorY() { return doorY; }

    @Override
    public void update(float delta) { /* Warps são estáticos */ }

    @Override
    public TextureRegion getCurrentFrame() { /* Warps são invisíveis */ return null; }
}