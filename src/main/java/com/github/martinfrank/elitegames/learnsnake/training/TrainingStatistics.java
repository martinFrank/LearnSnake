package com.github.martinfrank.elitegames.learnsnake.training;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Sammelt Episodenergebnisse und wertet sie aus.
 */
public final class TrainingStatistics {

    private final List<EpisodeResult> results = new ArrayList<>();

    public void add(EpisodeResult result) {
        results.add(result);
    }

    public List<EpisodeResult> results() {
        return Collections.unmodifiableList(results);
    }

    public int episodes() {
        return results.size();
    }

    public double averageScore() {
        return results.stream().mapToInt(EpisodeResult::score).average().orElse(0);
    }

    public int maxScore() {
        return results.stream().mapToInt(EpisodeResult::score).max().orElse(0);
    }

    public double averageSteps() {
        return results.stream().mapToInt(EpisodeResult::steps).average().orElse(0);
    }

    public double averageReward() {
        return results.stream().mapToDouble(EpisodeResult::totalReward).average().orElse(0);
    }

    /** Standardabweichung des Scores: Wie stark schwanken die einzelnen Episoden um den Durchschnitt? */
    public double scoreStandardDeviation() {
        return standardDeviation(results.stream().mapToDouble(EpisodeResult::score).toArray());
    }

    public double rewardStandardDeviation() {
        return standardDeviation(results.stream().mapToDouble(EpisodeResult::totalReward).toArray());
    }

    private static double standardDeviation(double[] values) {
        if (values.length == 0) {
            return 0;
        }
        double mean = Arrays.stream(values).average().orElse(0);
        double sumOfSquares = Arrays.stream(values).map(v -> (v - mean) * (v - mean)).sum();
        return Math.sqrt(sumOfSquares / values.length);
    }

    /** Anteil der Episoden, die durch das Schrittlimit abgebrochen wurden (0..1). */
    public double truncatedRatio() {
        return results.stream().filter(EpisodeResult::truncated).count() / (double) Math.max(1, results.size());
    }

    /** Statistik über die letzten {@code n} Episoden – für den Verlauf während des Trainings. */
    public TrainingStatistics last(int n) {
        TrainingStatistics window = new TrainingStatistics();
        results.subList(Math.max(0, results.size() - n), results.size()).forEach(window::add);
        return window;
    }

    public String summary() {
        return String.format(Locale.ROOT,
                "Episoden: %d | Score avg %.2f, max %d | Schritte avg %.1f | Reward avg %.2f | abgebrochen %.1f%%",
                episodes(), averageScore(), maxScore(), averageSteps(), averageReward(), truncatedRatio() * 100);
    }
}
