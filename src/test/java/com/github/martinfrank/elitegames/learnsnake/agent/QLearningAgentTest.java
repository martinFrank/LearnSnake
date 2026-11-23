package com.github.martinfrank.elitegames.learnsnake.agent;

import com.github.martinfrank.elitegames.learnsnake.environment.BinaryStateEncoder;
import com.github.martinfrank.elitegames.learnsnake.environment.StepResult;
import com.github.martinfrank.elitegames.learnsnake.game.snake.SnakeEnvironment;
import com.github.martinfrank.elitegames.learnsnake.training.Trainer;
import com.github.martinfrank.elitegames.learnsnake.training.TrainingStatistics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QLearningAgentTest {

    /** α = 0.5 und γ = 0.9 – runde Zahlen, damit man die Formel im Kopf nachrechnen kann. Kein Zufall. */
    private static final QLearningParameters GREEDY = new QLearningParameters(0.5, 0.9, 0.0, 0.0, 1.0);

    private static final BinaryStateEncoder ENCODER = new BinaryStateEncoder(2);

    private static final double[] STATE_0 = {0, 0};
    private static final double[] STATE_1 = {1, 0};
    private static final double[] STATE_2 = {0, 1};
    private static final int ACTION_A = 0;
    private static final int ACTION_B = 1;

    private static QLearningAgent agent(QLearningParameters parameters) {
        return new QLearningAgent(ENCODER, 2, parameters, 1L);
    }

    @Test
    void updateMovesValueTowardsTarget() {
        QLearningAgent agent = agent(GREEDY);
        QTable table = agent.table();
        table.update(ENCODER.encode(STATE_1), ACTION_B, 4.0);

        // Ziel = r + γ · max Q(s') = 1 + 0.9 · 4 = 4.6
        // Q neu = 0 + 0.5 · (4.6 − 0) = 2.3
        agent.learn(STATE_0, ACTION_A, new StepResult(STATE_1, 1.0, false, false));

        assertEquals(2.3, table.get(0, ACTION_A), 1e-9);
    }

    @Test
    void repeatedUpdatesConvergeToTarget() {
        QLearningAgent agent = agent(GREEDY);
        for (int i = 0; i < 50; i++) {
            agent.learn(STATE_0, ACTION_A, new StepResult(STATE_1, 1.0, false, false));
        }
        // Folgezustand hat überall 0, also Ziel = 1
        assertEquals(1.0, agent.table().get(0, ACTION_A), 1e-9);
    }

    @Test
    void terminatedIgnoresNextState() {
        QLearningAgent agent = agent(GREEDY);
        agent.table().update(ENCODER.encode(STATE_1), ACTION_B, 100.0);

        // Ziel = r = −10, der hohe Wert in STATE_1 zählt nicht
        agent.learn(STATE_0, ACTION_A, new StepResult(STATE_1, -10.0, true, false));

        assertEquals(-5.0, agent.table().get(0, ACTION_A), 1e-9);
    }

    @Test
    void truncatedStillUsesNextState() {
        QLearningAgent agent = agent(GREEDY);
        agent.table().update(ENCODER.encode(STATE_1), ACTION_B, 10.0);

        // Abbruch wegen Schrittlimit: Das Spiel wäre weitergegangen, Ziel = 0 + 0.9 · 10 = 9
        agent.learn(STATE_0, ACTION_A, new StepResult(STATE_1, 0.0, false, true));

        assertEquals(4.5, agent.table().get(0, ACTION_A), 1e-9);
    }

    @Test
    void choosesBestKnownActionWithoutExploration() {
        QLearningAgent agent = agent(GREEDY);
        agent.table().update(ENCODER.encode(STATE_2), ACTION_B, 1.0);

        assertEquals(ACTION_B, agent.chooseAction(STATE_2));
        assertEquals(ACTION_A, agent.chooseAction(STATE_0));
    }

    @Test
    void fullExplorationPicksAllActions() {
        QLearningAgent agent = agent(new QLearningParameters(0.1, 0.9, 1.0, 1.0, 1.0));
        agent.table().update(0, ACTION_A, 100.0);

        int chosenB = 0;
        for (int i = 0; i < 1000; i++) {
            if (agent.chooseAction(STATE_0) == ACTION_B) {
                chosenB++;
            }
        }
        assertTrue(chosenB > 400 && chosenB < 600, "Aktion B wurde " + chosenB + "-mal gewählt");
    }

    @Test
    void epsilonDecaysAfterEachEpisodeDownToMinimum() {
        QLearningAgent agent = agent(new QLearningParameters(0.1, 0.9, 1.0, 0.3, 0.5));
        StepResult move = new StepResult(STATE_1, 0.0, false, false);
        StepResult death = new StepResult(STATE_1, -1.0, true, false);

        agent.learn(STATE_0, ACTION_A, move);
        assertEquals(1.0, agent.epsilon());

        agent.learn(STATE_0, ACTION_A, death);
        assertEquals(0.5, agent.epsilon());

        agent.learn(STATE_0, ACTION_A, death);
        assertEquals(0.3, agent.epsilon());
        assertEquals(2, agent.trainedEpisodes());
    }

    @Test
    void evaluationModeNeitherExploresNorLearns() {
        QLearningAgent agent = agent(new QLearningParameters(0.5, 0.9, 1.0, 1.0, 1.0));
        agent.table().update(0, ACTION_B, 1.0);
        agent.setTraining(false);

        for (int i = 0; i < 100; i++) {
            assertEquals(ACTION_B, agent.chooseAction(STATE_0));
        }
        agent.learn(STATE_0, ACTION_A, new StepResult(STATE_1, 50.0, true, false));
        assertEquals(0.0, agent.table().get(0, ACTION_A));
        assertEquals(1.0, agent.epsilon());
    }

    @Test
    void rejectsObservationThatDoesNotFitTheEncoder() {
        QLearningAgent agent = agent(GREEDY);
        assertThrows(IllegalArgumentException.class, () -> agent.chooseAction(new double[]{0, 0, 1}));
    }

    @Test
    void rejectsTableThatDoesNotFitTheEncoder() {
        QTable tooSmall = new QTable(3, 2);
        assertThrows(IllegalArgumentException.class, () -> new QLearningAgent(ENCODER, tooSmall, GREEDY, 1L));
    }

    @Test
    void learnsToPlaySnakeMuchBetterThanRandom() {
        QLearningAgent agent = new QLearningAgent(SnakeEnvironment.STATE_ENCODER, SnakeEnvironment.ACTION_COUNT,
                QLearningParameters.DEFAULT, 3L);
        new Trainer(new SnakeEnvironment(10, 10, 3L), agent).run(3_000);

        agent.setTraining(false);
        TrainingStatistics stats = new Trainer(new SnakeEnvironment(10, 10, 99L), agent).run(500);

        assertTrue(stats.averageScore() > 10, "Durchschnitt war " + stats.averageScore());
    }
}
