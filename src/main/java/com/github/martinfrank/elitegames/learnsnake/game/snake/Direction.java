package com.github.martinfrank.elitegames.learnsnake.game.snake;

/**
 * Absolute Bewegungsrichtung auf dem Spielfeld.
 * <p>
 * Koordinatensystem: x wächst nach rechts, y wächst nach unten (wie bei Bildschirmkoordinaten).
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
        return switch (this) {
            case UP -> LEFT;
            case LEFT -> DOWN;
            case DOWN -> RIGHT;
            case RIGHT -> UP;
        };
    }

    public Direction turnRight() {
        return switch (this) {
            case UP -> RIGHT;
            case RIGHT -> DOWN;
            case DOWN -> LEFT;
            case LEFT -> UP;
        };
    }

    /**
     * Welche relative Drehung führt von dieser Richtung zu {@code target}?
     * Für die gleiche oder die entgegengesetzte Richtung gibt es keine Drehung – dann {@link Turn#STRAIGHT}.
     * (Umdrehen ist in Snake nicht erlaubt.)
     */
    public Turn turnTowards(Direction target) {
        if (target == turnLeft()) {
            return Turn.LEFT;
        }
        if (target == turnRight()) {
            return Turn.RIGHT;
        }
        return Turn.STRAIGHT;
    }

    public Direction apply(Turn turn) {
        return switch (turn) {
            case STRAIGHT -> this;
            case LEFT -> turnLeft();
            case RIGHT -> turnRight();
        };
    }
}
