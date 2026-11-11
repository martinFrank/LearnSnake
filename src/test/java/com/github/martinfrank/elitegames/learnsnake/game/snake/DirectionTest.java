package com.github.martinfrank.elitegames.learnsnake.game.snake;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectionTest {

    @Test
    void turnRightGoesClockwise() {
        assertEquals(Direction.RIGHT, Direction.UP.turnRight());
        assertEquals(Direction.DOWN, Direction.RIGHT.turnRight());
        assertEquals(Direction.LEFT, Direction.DOWN.turnRight());
        assertEquals(Direction.UP, Direction.LEFT.turnRight());
    }

    @Test
    void turnLeftGoesCounterClockwise() {
        assertEquals(Direction.LEFT, Direction.UP.turnLeft());
        assertEquals(Direction.UP, Direction.RIGHT.turnLeft());
        assertEquals(Direction.RIGHT, Direction.DOWN.turnLeft());
        assertEquals(Direction.DOWN, Direction.LEFT.turnLeft());
    }

    @Test
    void applyTurn() {
        assertEquals(Direction.RIGHT, Direction.RIGHT.apply(Turn.STRAIGHT));
        assertEquals(Direction.UP, Direction.RIGHT.apply(Turn.LEFT));
        assertEquals(Direction.DOWN, Direction.RIGHT.apply(Turn.RIGHT));
    }

    @Test
    void upMeansDecreasingY() {
        assertEquals(new Position(3, 2), new Position(3, 3).move(Direction.UP));
        assertEquals(new Position(3, 4), new Position(3, 3).move(Direction.DOWN));
    }
}
