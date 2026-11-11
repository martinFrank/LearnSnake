package com.github.martinfrank.elitegames.learnsnake.game.snake;

import com.github.martinfrank.elitegames.learnsnake.environment.StepResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.martinfrank.elitegames.learnsnake.game.snake.SnakeEnvironment.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnakeEnvironmentTest {

    private SnakeEnvironment env;

    @BeforeEach
    void setUp() {
        env = new SnakeEnvironment(10, 10, 42L);
        env.reset();
        env.game().placeFoodAt(new Position(0, 0));
    }

    @Test
    void sizes() {
        assertEquals(11, env.observationSize());
        assertEquals(3, env.actionCount());
        assertEquals(11, env.reset().length);
    }

    @Test
    void observationEncodesDirectionAndFood() {
        double[] obs = env.step(ACTION_STRAIGHT).observation();
        // Kopf (6,5), Blick rechts, Essen (0,0) liegt oben links, keine Gefahr
        assertArrayEquals(new double[]{
                0, 0, 0,        // Gefahr geradeaus, links, rechts
                0, 1, 0, 0,     // Richtung oben, rechts, unten, links
                1, 0, 0, 1      // Essen oben, rechts, unten, links
        }, obs);
    }

    @Test
    void observationFollowsTurns() {
        double[] obs = env.step(ACTION_LEFT).observation();
        assertEquals(1.0, obs[DIRECTION_UP]);
        assertEquals(0.0, obs[DIRECTION_RIGHT]);
    }

    @Test
    void observationDetectsWallAhead() {
        StepResult result = null;
        for (int i = 0; i < 4; i++) {
            result = env.step(ACTION_STRAIGHT);
        }
        // Kopf (9,5) an der rechten Wand
        double[] obs = result.observation();
        assertEquals(1.0, obs[DANGER_STRAIGHT]);
        assertEquals(0.0, obs[DANGER_LEFT]);
        assertEquals(0.0, obs[DANGER_RIGHT]);
    }

    @Test
    void observationDetectsBodyToTheRight() {
        growTo(5);
        env.step(ACTION_RIGHT);
        double[] obs = env.step(ACTION_RIGHT).observation();
        assertEquals(1.0, obs[DANGER_RIGHT], "eigener Körper");
    }

    @Test
    void rewardForFood() {
        env.game().placeFoodAt(new Position(6, 5));
        StepResult result = env.step(ACTION_STRAIGHT);
        assertEquals(SnakeRewards.DEFAULT.food(), result.reward());
        assertFalse(result.done());
        assertEquals(1, env.score());
    }

    @Test
    void rewardForNormalStep() {
        assertEquals(SnakeRewards.DEFAULT.step(), env.step(ACTION_STRAIGHT).reward());
    }

    @Test
    void collisionTerminates() {
        for (int i = 0; i < 4; i++) {
            env.step(ACTION_STRAIGHT);
        }
        StepResult result = env.step(ACTION_STRAIGHT);
        assertEquals(SnakeRewards.DEFAULT.death(), result.reward());
        assertTrue(result.terminated());
        assertFalse(result.truncated());
    }

    @Test
    void starvationTruncates() {
        // Immer rechts abbiegen: Die Schlange (Länge 3) kreist endlos in einem 2x2-Quadrat
        int limit = SnakeEnvironment.STARVATION_FACTOR * 3;
        for (int i = 1; i < limit; i++) {
            assertFalse(env.step(ACTION_RIGHT).done(), "Schritt " + i);
        }
        StepResult result = env.step(ACTION_RIGHT);
        assertTrue(result.truncated());
        assertFalse(result.terminated());
    }

    @Test
    void stepAfterDoneThrows() {
        for (int i = 0; i < 5; i++) {
            env.step(ACTION_STRAIGHT);
        }
        assertThrows(IllegalStateException.class, () -> env.step(ACTION_STRAIGHT));
    }

    @Test
    void resetAllowsNewEpisode() {
        for (int i = 0; i < 5; i++) {
            env.step(ACTION_STRAIGHT);
        }
        env.reset();
        assertFalse(env.step(ACTION_STRAIGHT).done());
        assertEquals(0, env.score());
    }

    @Test
    void invalidActionThrows() {
        assertThrows(IllegalArgumentException.class, () -> env.step(-1));
        assertThrows(IllegalArgumentException.class, () -> env.step(3));
    }

    @Test
    void customRewards() {
        SnakeEnvironment custom = new SnakeEnvironment(10, 10, 1L, new SnakeRewards(1, -5, -0.1));
        custom.reset();
        custom.game().placeFoodAt(new Position(0, 0));
        assertEquals(-0.1, custom.step(ACTION_STRAIGHT).reward());
    }

    private void growTo(int length) {
        SnakeGame game = env.game();
        while (game.length() < length) {
            game.placeFoodAt(game.head().move(Direction.RIGHT));
            env.step(ACTION_STRAIGHT);
        }
        game.placeFoodAt(new Position(0, 0));
    }
}
