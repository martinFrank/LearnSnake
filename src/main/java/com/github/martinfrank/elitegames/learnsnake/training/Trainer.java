package com.github.martinfrank.elitegames.learnsnake.training;

import com.github.martinfrank.elitegames.learnsnake.agent.Agent;
import com.github.martinfrank.elitegames.learnsnake.environment.Environment;
import com.github.martinfrank.elitegames.learnsnake.environment.StepResult;

import java.util.function.Consumer;

/**
 * Die Spielschleife: Lässt einen Agenten Episode für Episode in einem Environment spielen.
 * <pre>
 * observation = reset()
 * solange nicht fertig:
 *     action = agent.chooseAction(observation)
 *     result = env.step(action)
 *     agent.learn(observation, action, result)
 *     observation = result.observation
 * </pre>
 */
public final class Trainer {

    private final Environment environment;
    private final Agent agent;
    private int episodeCounter;

    public Trainer(Environment environment, Agent agent) {
        this.environment = environment;
        this.agent = agent;
    }

    public EpisodeResult runEpisode() {
        double[] observation = environment.reset();
        int steps = 0;
        double totalReward = 0;
        StepResult result;
        do {
            int action = agent.chooseAction(observation);
            result = environment.step(action);
            agent.learn(observation, action, result);
            observation = result.observation();
            steps++;
            totalReward += result.reward();
        } while (!result.done());

        episodeCounter++;
        return new EpisodeResult(episodeCounter, environment.score(), steps, totalReward, result.truncated());
    }

    public TrainingStatistics run(int episodes) {
        return run(episodes, _ -> { });
    }

    /** @param afterEpisode wird nach jeder Episode aufgerufen, z.B. für Fortschrittsausgaben */
    public TrainingStatistics run(int episodes, Consumer<EpisodeResult> afterEpisode) {
        TrainingStatistics statistics = new TrainingStatistics();
        for (int i = 0; i < episodes; i++) {
            EpisodeResult result = runEpisode();
            statistics.add(result);
            afterEpisode.accept(result);
        }
        return statistics;
    }
}
