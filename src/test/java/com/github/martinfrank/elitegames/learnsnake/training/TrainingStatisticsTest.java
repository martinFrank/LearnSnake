package com.github.martinfrank.elitegames.learnsnake.training;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrainingStatisticsTest {

    @Test
    void standardDeviationOfScoreAndReward() {
        TrainingStatistics stats = new TrainingStatistics();
        stats.add(new EpisodeResult(1, 2, 10, 10.0, false));
        stats.add(new EpisodeResult(2, 4, 10, 30.0, false));

        // Mittel 3, Abweichungen ±1 → Standardabweichung 1; Reward: Mittel 20, ±10
        assertEquals(1.0, stats.scoreStandardDeviation(), 1e-9);
        assertEquals(10.0, stats.rewardStandardDeviation(), 1e-9);
    }

    @Test
    void standardDeviationWithoutEpisodesIsZero() {
        assertEquals(0.0, new TrainingStatistics().scoreStandardDeviation());
    }
}
