package com.github.martinfrank.elitegames.learnsnake.training;

import java.util.List;

/**
 * Spaltennamen der CSV-Dateien. Sie folgen den in Reinforcement Learning üblichen Messgrößen
 * (vgl. Gymnasium {@code RecordEpisodeStatistics} und Stable-Baselines3 {@code Monitor}/{@code EvalCallback}).
 *
 * <h2>Pro Episode ({@link #EPISODE_COLUMNS})</h2>
 * <ul>
 *     <li>{@link #RETURN}: Summe aller Rewards der Episode – das, was der Agent maximieren will</li>
 *     <li>{@link #LENGTH}: Anzahl Schritte der Episode</li>
 *     <li>{@link #TOTAL_STEPS}: Schritte seit Beginn ("Timesteps") – in der Forschung die übliche x-Achse,
 *         weil späte Episoden viel länger dauern als frühe</li>
 *     <li>{@link #SCORE}: spielspezifische Punktzahl (bei Snake: gefressenes Essen)</li>
 *     <li>{@link #EPSILON}: Explorationsrate, nur bei lernenden Agenten mit ε-greedy</li>
 * </ul>
 *
 * <h2>Regelmäßige Auswertung ({@link #EVALUATION_COLUMNS})</h2>
 * Während des Trainings wird immer wieder ohne Zufall und ohne Lernen auf denselben Spielen gemessen.
 * Das ist die aussagekräftige Lernkurve: Die Werte aus dem Training selbst sind durch die zufälligen
 * Züge (ε) verfälscht. Angegeben werden Mittelwert und Standardabweichung über die Auswertungsepisoden.
 */
public final class Metrics {

    public static final String EPISODE = "episode";
    public static final String TOTAL_STEPS = "total_steps";
    public static final String RETURN = "return";
    public static final String LENGTH = "length";
    public static final String SCORE = "score";
    public static final String TRUNCATED = "truncated";
    public static final String EPSILON = "epsilon";

    public static final String MEAN_RETURN = "mean_return";
    public static final String STD_RETURN = "std_return";
    public static final String MEAN_SCORE = "mean_score";
    public static final String STD_SCORE = "std_score";
    public static final String MEAN_LENGTH = "mean_length";
    public static final String MAX_SCORE = "max_score";

    public static final List<String> EPISODE_COLUMNS = List.of(EPISODE, TOTAL_STEPS, RETURN, LENGTH, SCORE, TRUNCATED);

    public static final List<String> EVALUATION_COLUMNS = List.of(EPISODE, TOTAL_STEPS,
            MEAN_RETURN, STD_RETURN, MEAN_SCORE, STD_SCORE, MEAN_LENGTH, MAX_SCORE);

    private Metrics() {
    }
}
