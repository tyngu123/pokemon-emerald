package br.com.yago.emerald.model;

/**
 * Enumeração para representar as quatro direções de movimento possíveis.
 * Isso evita o uso de "magic numbers" ou strings, tornando o código mais limpo e seguro.
 * Cada direção tem um deslocamento correspondente em X e Y no grid.
 */
public enum Direction {
    UP(0, 1),
    DOWN(0, -1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    private final int dx;
    private final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }
}
