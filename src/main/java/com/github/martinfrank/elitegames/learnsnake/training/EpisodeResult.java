package com.github.martinfrank.elitegames.learnsnake.training;

/**
 * Ergebnis einer gespielten Episode.
 *
 * @param episode     laufende Nummer, beginnend bei 1
 * @param score       spielspezifische Punktzahl
 * @param steps       Anzahl Schritte
 * @param totalReward Summe aller Rewards
 * @param truncated   {@code true}, wenn die Episode abgebrochen statt regulär beendet wurde
 */
public record EpisodeResult(int episode, int score, int steps, double totalReward, boolean truncated) {
}
