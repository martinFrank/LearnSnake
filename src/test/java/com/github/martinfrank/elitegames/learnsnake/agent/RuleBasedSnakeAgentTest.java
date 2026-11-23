package com.github.martinfrank.elitegames.learnsnake.agent;

import com.github.martinfrank.elitegames.learnsnake.game.snake.SnakeEnvironment;
import com.github.martinfrank.elitegames.learnsnake.training.Trainer;
import com.github.martinfrank.elitegames.learnsnake.training.TrainingStatistics;
import org.junit.jupiter.api.Test;

import static com.github.martinfrank.elitegames.learnsnake.game.snake.SnakeEnvironment.ACTION_LEFT;
import static com.github.martinfrank.elitegames.learnsnake.game.snake.SnakeEnvironment.ACTION_RIGHT;
import static com.github.martinfrank.elitegames.learnsnake.game.snake.SnakeEnvironment.ACTION_STRAIGHT;
import static com.github.martinfrank.elitegames.learnsnake.game.snake.SnakeEnvironment.DANGER_LEFT;
import static com.github.martinfrank.elitegames.learnsnake.game.snake.SnakeEnvironment.DANGER_RIGHT;
import static com.github.martinfrank.elitegames.learnsnake.game.snake.SnakeEnvironment.DANGER_STRAIGHT;
import static com.github.martinfrank.elitegames.learnsnake.game.snake.SnakeEnvironment.DIRECTION_DOWN;
import static com.github.martinfrank.elitegames.learnsnake.game.snake.SnakeEnvironment.DIRECTION_LEFT;
import static com.github.martinfrank.elitegames.learnsnake.game.snake.SnakeEnvironment.DIRECTION_RIGHT;
import static com.github.martinfrank.elitegames.learnsnake.game.snake.SnakeEnvironment.DIRECTION_UP;
import static com.github.martinfrank.elitegames.learnsnake.game.snake.SnakeEnvironment.FOOD_DOWN;
import static com.github.martinfrank.elitegames.learnsnake.game.snake.SnakeEnvironment.FOOD_LEFT;
import static com.github.martinfrank.elitegames.learnsnake.game.snake.SnakeEnvironment.FOOD_RIGHT;
import static com.github.martinfrank.elitegames.learnsnake.game.snake.SnakeEnvironment.FOOD_UP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleBasedSnakeAgentTest {

    private final RuleBasedSnakeAgent agent = new RuleBasedSnakeAgent();

    /** Baut eine Observation, in der genau die angegebenen Merkmale gesetzt sind. */
    private static double[] observation(int... setFeatures) {
        double[] obs = new double[SnakeEnvironment.OBSERVATION_SIZE];
        for (int feature : setFeatures) {
            obs[feature] = 1.0;
        }
        return obs;
    }

    @Test
    void goesStraightWhenFoodIsAhead() {
        assertEquals(ACTION_STRAIGHT, agent.chooseAction(observation(DIRECTION_UP, FOOD_UP)));
    }

    @Test
    void turnsLeftTowardsFood() {
        // nach oben unterwegs, links ist Westen
        assertEquals(ACTION_LEFT, agent.chooseAction(observation(DIRECTION_UP, FOOD_LEFT)));
    }

    @Test
    void turnsRightTowardsFood() {
        // nach rechts unterwegs, rechts ist Süden
        assertEquals(ACTION_RIGHT, agent.chooseAction(observation(DIRECTION_RIGHT, FOOD_DOWN)));
    }

    @Test
    void translatesAbsoluteFoodDirectionForEveryHeading() {
        assertEquals(ACTION_LEFT, agent.chooseAction(observation(DIRECTION_RIGHT, FOOD_UP)));
        assertEquals(ACTION_LEFT, agent.chooseAction(observation(DIRECTION_DOWN, FOOD_RIGHT)));
        assertEquals(ACTION_RIGHT, agent.chooseAction(observation(DIRECTION_DOWN, FOOD_LEFT)));
        assertEquals(ACTION_RIGHT, agent.chooseAction(observation(DIRECTION_LEFT, FOOD_UP)));
    }

    @Test
    void prefersStraightWhenFoodIsDiagonallyAhead() {
        assertEquals(ACTION_STRAIGHT, agent.chooseAction(observation(DIRECTION_UP, FOOD_UP, FOOD_RIGHT)));
    }

    @Test
    void avoidsDangerEvenIfFoodIsThere() {
        assertEquals(ACTION_RIGHT,
                agent.chooseAction(observation(DIRECTION_UP, FOOD_UP, FOOD_RIGHT, DANGER_STRAIGHT)));
    }

    @Test
    void goesStraightWhenFoodIsBehind() {
        // Essen liegt hinten: Keine Drehung führt direkt hin, also die erste sichere nehmen
        assertEquals(ACTION_STRAIGHT, agent.chooseAction(observation(DIRECTION_UP, FOOD_DOWN)));
    }

    @Test
    void takesFirstSafeTurnWhenNoSafeTurnLeadsToFood() {
        assertEquals(ACTION_LEFT,
                agent.chooseAction(observation(DIRECTION_UP, FOOD_RIGHT, DANGER_STRAIGHT, DANGER_RIGHT)));
    }

    @Test
    void goesStraightWhenEverythingIsDangerous() {
        assertEquals(ACTION_STRAIGHT,
                agent.chooseAction(observation(DIRECTION_UP, FOOD_UP, DANGER_STRAIGHT, DANGER_LEFT, DANGER_RIGHT)));
    }

    @Test
    void rejectsObservationWithoutDirection() {
        assertThrows(IllegalArgumentException.class, () -> agent.chooseAction(observation(FOOD_UP)));
    }

    @Test
    void rejectsObservationOfWrongSize() {
        assertThrows(IllegalArgumentException.class, () -> agent.chooseAction(new double[3]));
    }

    @Test
    void playsMuchBetterThanRandom() {
        SnakeEnvironment env = new SnakeEnvironment(10, 10, 7L);
        TrainingStatistics stats = new Trainer(env, agent).run(200);

        assertTrue(stats.averageScore() > 5, "Durchschnitt war " + stats.averageScore());
    }
}
