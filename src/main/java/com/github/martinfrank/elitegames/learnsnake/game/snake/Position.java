package com.github.martinfrank.elitegames.learnsnake.game.snake;

/**
 * Eine Zelle auf dem Spielfeld.
 */
public record Position(int x, int y) {

    public Position move(Direction direction) {
        return new Position(x + direction.dx(), y + direction.dy());
    }
}
