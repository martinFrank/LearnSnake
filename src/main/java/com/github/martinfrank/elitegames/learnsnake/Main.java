package com.github.martinfrank.elitegames.learnsnake;

import com.github.martinfrank.elitegames.learnsnake.agent.Agent;
import com.github.martinfrank.elitegames.learnsnake.agent.RandomAgent;
import com.github.martinfrank.elitegames.learnsnake.agent.RuleBasedSnakeAgent;
import com.github.martinfrank.elitegames.learnsnake.game.snake.SnakeEnvironment;
import com.github.martinfrank.elitegames.learnsnake.training.Trainer;
import com.github.martinfrank.elitegames.learnsnake.training.TrainingStatistics;

/**
 * Lässt die Vergleichs-Agenten Snake spielen und gibt ihre Statistik aus.
 */
public final class Main {

    private static final int WIDTH = 10;
    private static final int HEIGHT = 10;
    private static final int EPISODES = 10_000;
    private static final long SEED = 42L;

    static void main() {
        evaluate("Random Agent", new RandomAgent(SnakeEnvironment.ACTION_COUNT, SEED));
        evaluate("Rule-Based Agent", new RuleBasedSnakeAgent());
    }

    private static void evaluate(String name, Agent agent) {
        SnakeEnvironment environment = new SnakeEnvironment(WIDTH, HEIGHT, SEED);
        Trainer trainer = new Trainer(environment, agent);

        long start = System.nanoTime();
        TrainingStatistics statistics = trainer.run(EPISODES);
        long millis = (System.nanoTime() - start) / 1_000_000;

        System.out.printf("%s auf %dx%d:%n", name, WIDTH, HEIGHT);
        System.out.println(statistics.summary());
        System.out.printf("Dauer: %d ms (%.0f Schritte/s)%n%n",
                millis, statistics.averageSteps() * EPISODES / Math.max(1, millis) * 1000);
    }
}
