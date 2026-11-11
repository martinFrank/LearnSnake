package com.github.martinfrank.elitegames.learnsnake.game.snake;

/**
 * Absolute Bewegungsrichtung auf dem Spielfeld.
 * <p>
 * Koordinatensystem: x wächst nach rechts, y wächst nach unten (wie bei Bildschirmkoordinaten).
 * Die Reihenfolge der Konstanten ist im Uhrzeigersinn – darauf beruhen {@link #turnLeft()} und {@link #turnRight()}.
 */
public enum Direction {
    UP(0, -1),
    RIGHT(1, 0),
    DOWN(0, 1),
    LEFT(-1, 0);

    private final int dx;
    private final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public int dx() {
        return dx;
    }

    public int dy() {
        return dy;
    }

    public Direction turnLeft() {
        return values()[(ordinal() + 3) % 4];
    }

    public Direction turnRight() {
        return values()[(ordinal() + 1) % 4];
    }

    public Direction apply(Turn turn) {
        return switch (turn) {
            case STRAIGHT -> this;
            case LEFT -> turnLeft();
            case RIGHT -> turnRight();
        };
    }
}
