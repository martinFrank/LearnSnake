package com.github.martinfrank.elitegames.learnsnake.training;

import com.github.martinfrank.elitegames.learnsnake.agent.Agent;
import com.github.martinfrank.elitegames.learnsnake.agent.RandomAgent;
import com.github.martinfrank.elitegames.learnsnake.environment.StepResult;
import com.github.martinfrank.elitegames.learnsnake.game.snake.SnakeEnvironment;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainerTest {

    @Test
    void runsRequestedNumberOfEpisodes() {
        Trainer trainer = randomTrainer(1L);
        List<Integer> seen = new ArrayList<>();

        TrainingStatistics stats = trainer.run(50, r -> seen.add(r.episode()));

        assertEquals(50, stats.episodes());
        assertEquals(1, seen.getFirst());
        assertEquals(50, seen.getLast());
        assertTrue(stats.results().stream().allMatch(r -> r.steps() > 0));
    }

    @Test
    void agentLearnsFromEveryStep() {
        SnakeEnvironment env = new SnakeEnvironment(10, 10, 1L);
        CountingAgent agent = new CountingAgent(new RandomAgent(env.actionCount(), 1L));

        EpisodeResult result = new Trainer(env, agent).runEpisode();

        assertEquals(result.steps(), agent.chooseCalls);
        assertEquals(result.steps(), agent.learnCalls);
        assertTrue(agent.lastResult.done(), "letzter Schritt beendet die Episode");
    }

    @Test
    void sameSeedsGiveSameStatistics() {
        assertEquals(randomTrainer(7L).run(200).summary(), randomTrainer(7L).run(200).summary());
    }

    @Test
    void statisticsWindow() {
        TrainingStatistics stats = new TrainingStatistics();
        stats.add(new EpisodeResult(1, 0, 10, -10, false));
        stats.add(new EpisodeResult(2, 4, 30, 30, true));
        stats.add(new EpisodeResult(3, 2, 20, 10, false));

        assertEquals(2.0, stats.averageScore());
        assertEquals(4, stats.maxScore());
        assertEquals(20.0, stats.averageSteps());
        assertEquals(1.0 / 3, stats.truncatedRatio(), 1e-9);

        TrainingStatistics lastTwo = stats.last(2);
        assertEquals(2, lastTwo.episodes());
        assertEquals(3.0, lastTwo.averageScore());
    }

    private static Trainer randomTrainer(long seed) {
        SnakeEnvironment env = new SnakeEnvironment(10, 10, seed);
        return new Trainer(env, new RandomAgent(env.actionCount(), seed));
    }

    private static final class CountingAgent implements Agent {
        private final Agent delegate;
        int chooseCalls;
        int learnCalls;
        StepResult lastResult;

        CountingAgent(Agent delegate) {
            this.delegate = delegate;
        }

        @Override
        public int chooseAction(double[] observation) {
            chooseCalls++;
            return delegate.chooseAction(observation);
        }

        @Override
        public void learn(double[] observation, int action, StepResult result) {
            learnCalls++;
            lastResult = result;
        }
    }
}
