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
    void turnTowards() {
        assertEquals(Turn.LEFT, Direction.RIGHT.turnTowards(Direction.UP));
        assertEquals(Turn.RIGHT, Direction.RIGHT.turnTowards(Direction.DOWN));
        assertEquals(Turn.STRAIGHT, Direction.RIGHT.turnTowards(Direction.RIGHT));
        assertEquals(Turn.STRAIGHT, Direction.RIGHT.turnTowards(Direction.LEFT), "Umdrehen ist nicht erlaubt");
    }

    @Test
    void turnTowardsAndApplyFitTogether() {
        for (Direction from : Direction.values()) {
            assertEquals(from.turnLeft(), from.apply(from.turnTowards(from.turnLeft())));
            assertEquals(from.turnRight(), from.apply(from.turnTowards(from.turnRight())));
        }
    }

    @Test
    void upMeansDecreasingY() {
        assertEquals(new Position(3, 2), new Position(3, 3).move(Direction.UP));
        assertEquals(new Position(3, 4), new Position(3, 3).move(Direction.DOWN));
    }
}
