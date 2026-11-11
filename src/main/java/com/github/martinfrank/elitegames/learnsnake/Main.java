package com.github.martinfrank.elitegames.learnsnake;

import com.github.martinfrank.elitegames.learnsnake.agent.RandomAgent;
import com.github.martinfrank.elitegames.learnsnake.game.snake.SnakeEnvironment;
import com.github.martinfrank.elitegames.learnsnake.training.Trainer;
import com.github.martinfrank.elitegames.learnsnake.training.TrainingStatistics;

/**
 * Lässt den Random Agent Snake spielen und gibt die Statistik aus.
 */
public final class Main {

    private static final int EPISODES = 10_000;
    private static final long SEED = 42L;

    static void main() {
        SnakeEnvironment environment = new SnakeEnvironment(10, 10, SEED);
        RandomAgent agent = new RandomAgent(environment.actionCount(), SEED);
        Trainer trainer = new Trainer(environment, agent);

        long start = System.nanoTime();
        TrainingStatistics statistics = trainer.run(EPISODES);
        long millis = (System.nanoTime() - start) / 1_000_000;

        System.out.println("Random Agent auf 10x10:");
        System.out.println(statistics.summary());
        System.out.printf("Dauer: %d ms (%.0f Schritte/s)%n",
                millis, statistics.averageSteps() * EPISODES / Math.max(1, millis) * 1000);
    }
}
