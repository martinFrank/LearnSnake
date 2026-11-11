package com.github.martinfrank.elitegames.learnsnake.game.snake;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnakeGameTest {

    private static final long SEED = 42L;

    /** Essen in eine Ecke legen, damit es den Testablauf nicht stört. */
    private static SnakeGame newGame() {
        SnakeGame game = new SnakeGame(10, 10, SEED);
        game.placeFoodAt(new Position(0, 0));
        return game;
    }

    @Test
    void initialState() {
        SnakeGame game = new SnakeGame(10, 10, SEED);

        assertEquals(List.of(new Position(5, 5), new Position(4, 5), new Position(3, 5)), game.body());
        assertEquals(Direction.RIGHT, game.direction());
        assertEquals(0, game.score());
        assertEquals(0, game.steps());
        assertFalse(game.isGameOver());

        Position food = game.food().orElseThrow();
        assertTrue(game.isInside(food));
        assertFalse(game.isOccupied(food));
    }

    @Test
    void rejectsTooSmallBoard() {
        assertThrows(IllegalArgumentException.class, () -> new SnakeGame(4, 10, SEED));
        assertThrows(IllegalArgumentException.class, () -> new SnakeGame(10, 4, SEED));
    }

    @Test
    void straightMovesHeadAndTail() {
        SnakeGame game = newGame();

        assertEquals(StepOutcome.MOVED, game.step(Turn.STRAIGHT));

        assertEquals(List.of(new Position(6, 5), new Position(5, 5), new Position(4, 5)), game.body());
        assertFalse(game.isOccupied(new Position(3, 5)));
        assertEquals(1, game.steps());
    }

    @Test
    void turnsAreRelativeToCurrentDirection() {
        SnakeGame game = newGame();

        game.step(Turn.LEFT);
        assertEquals(Direction.UP, game.direction());
        assertEquals(new Position(5, 4), game.head());

        game.step(Turn.LEFT);
        assertEquals(Direction.LEFT, game.direction());
        assertEquals(new Position(4, 4), game.head());

        game.step(Turn.RIGHT);
        assertEquals(Direction.UP, game.direction());
        assertEquals(new Position(4, 3), game.head());
    }

    @Test
    void eatingGrowsSnakeAndPlacesNewFood() {
        SnakeGame game = newGame();
        game.placeFoodAt(new Position(6, 5));

        assertEquals(StepOutcome.ATE_FOOD, game.step(Turn.STRAIGHT));

        assertEquals(4, game.length());
        assertEquals(1, game.score());
        assertEquals(0, game.stepsSinceLastFood());
        assertEquals(List.of(new Position(6, 5), new Position(5, 5), new Position(4, 5), new Position(3, 5)),
                game.body());
        Position newFood = game.food().orElseThrow();
        assertFalse(game.isOccupied(newFood));
    }

    @Test
    void stepsSinceLastFoodCountsUp() {
        SnakeGame game = newGame();
        game.step(Turn.STRAIGHT);
        game.step(Turn.STRAIGHT);
        assertEquals(2, game.stepsSinceLastFood());
    }

    @Test
    void hittingWallEndsGame() {
        SnakeGame game = newGame();
        // Kopf auf (5,5), Blick nach rechts: 4 Schritte bis x=9, der 5. geht hinaus
        for (int i = 0; i < 4; i++) {
            assertEquals(StepOutcome.MOVED, game.step(Turn.STRAIGHT));
        }
        assertEquals(StepOutcome.HIT_WALL, game.step(Turn.STRAIGHT));
        assertTrue(game.isGameOver());
    }

    @Test
    void stepAfterGameOverThrows() {
        SnakeGame game = newGame();
        while (!game.isGameOver()) {
            game.step(Turn.STRAIGHT);
        }
        assertThrows(IllegalStateException.class, () -> game.step(Turn.STRAIGHT));
    }

    @Test
    void hittingOwnBodyEndsGame() {
        SnakeGame game = growTo(5);
        // Kopf (7,5), Körper nach links. Dreimal rechts: runter, links, hoch -> in den Körper
        assertEquals(StepOutcome.MOVED, game.step(Turn.RIGHT));
        assertEquals(StepOutcome.MOVED, game.step(Turn.RIGHT));
        assertEquals(StepOutcome.HIT_SELF, game.step(Turn.RIGHT));
        assertTrue(game.isGameOver());
    }

    @Test
    void movingIntoVacatedTailCellIsAllowed() {
        SnakeGame game = growTo(4);
        // Bei Länge 4 landet der Kopf nach drei Rechtsdrehungen genau auf dem alten Schwanzende,
        // das im selben Schritt weiterwandert.
        game.step(Turn.RIGHT);
        game.step(Turn.RIGHT);
        assertEquals(StepOutcome.MOVED, game.step(Turn.RIGHT));
        assertFalse(game.isGameOver());
        assertEquals(4, game.length());
    }

    @Test
    void wouldCollideDetectsWall() {
        SnakeGame game = newGame();
        for (int i = 0; i < 4; i++) {
            game.step(Turn.STRAIGHT);
        }
        // Kopf auf (9,5), Blick nach rechts
        assertTrue(game.wouldCollide(Turn.STRAIGHT));
        assertFalse(game.wouldCollide(Turn.LEFT));
        assertFalse(game.wouldCollide(Turn.RIGHT));
    }

    @Test
    void wouldCollideDetectsBody() {
        SnakeGame game = growTo(5);
        game.step(Turn.RIGHT);
        game.step(Turn.RIGHT);
        // Kopf (6,6), Blick nach links; rechts abbiegen führt nach (6,5) in den Körper
        assertTrue(game.wouldCollide(Turn.RIGHT));
        assertFalse(game.wouldCollide(Turn.STRAIGHT));
        assertFalse(game.wouldCollide(Turn.LEFT));
    }

    @Test
    void wouldCollideIgnoresVacatingTail() {
        SnakeGame game = growTo(4);
        game.step(Turn.RIGHT);
        game.step(Turn.RIGHT);
        assertFalse(game.wouldCollide(Turn.RIGHT));
    }

    @Test
    void wouldCollideDoesNotChangeState() {
        SnakeGame game = newGame();
        List<Position> before = game.body();
        game.wouldCollide(Turn.LEFT);
        assertEquals(before, game.body());
        assertEquals(Direction.RIGHT, game.direction());
        assertEquals(0, game.steps());
    }

    @Test
    void resetRestoresInitialState() {
        SnakeGame game = growTo(5);
        game.step(Turn.LEFT);

        game.reset();

        assertEquals(List.of(new Position(5, 5), new Position(4, 5), new Position(3, 5)), game.body());
        assertEquals(Direction.RIGHT, game.direction());
        assertEquals(0, game.score());
        assertEquals(0, game.steps());
        assertFalse(game.isGameOver());
    }

    @Test
    void sameSeedGivesSameGame() {
        assertEquals(playRandomly(123L), playRandomly(123L));
    }

    @Test
    void invariantsHoldDuringRandomPlay() {
        SnakeGame game = new SnakeGame(8, 8, SEED);
        Random random = new Random(7);
        for (int i = 0; i < 20_000; i++) {
            if (game.isGameOver()) {
                game.reset();
            }
            game.step(Turn.values()[random.nextInt(3)]);
            if (game.isGameOver()) {
                continue;
            }
            List<Position> body = game.body();
            assertEquals(SnakeGame.INITIAL_LENGTH + game.score(), body.size());
            assertEquals(body.size(), new HashSet<>(body).size(), "Körper darf sich nicht überlappen");
            assertTrue(body.stream().allMatch(game::isInside));
            game.food().ifPresent(food -> assertFalse(game.isOccupied(food), "Essen liegt auf der Schlange"));
        }
    }

    /** Lässt die Schlange geradeaus fressen, bis sie die gewünschte Länge hat. Kopf endet auf (länge+2, 5). */
    private static SnakeGame growTo(int length) {
        SnakeGame game = newGame();
        while (game.length() < length) {
            game.placeFoodAt(game.head().move(Direction.RIGHT));
            assertEquals(StepOutcome.ATE_FOOD, game.step(Turn.STRAIGHT));
        }
        game.placeFoodAt(new Position(0, 0));
        return game;
    }

    /** Spielt einige Episoden zufällig und protokolliert Köpfe und Essen. */
    private static List<Object> playRandomly(long seed) {
        SnakeGame game = new SnakeGame(10, 10, seed);
        Random random = new Random(seed);
        List<Object> trace = new ArrayList<>();
        for (int i = 0; i < 2_000; i++) {
            if (game.isGameOver()) {
                game.reset();
            }
            trace.add(game.step(Turn.values()[random.nextInt(3)]));
            trace.add(game.head());
            trace.add(game.food());
        }
        return trace;
    }
}
