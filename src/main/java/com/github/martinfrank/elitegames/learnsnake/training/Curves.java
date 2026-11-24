package com.github.martinfrank.elitegames.learnsnake.training;

import java.util.ArrayList;
import java.util.List;

/**
 * Hilfsfunktionen, um aus Episodenwerten eine lesbare Lernkurve zu machen.
 */
public final class Curves {

    private Curves() {
    }

    /**
     * Gleitender Durchschnitt über die letzten {@code window} Werte. Einzelne Episoden schwanken stark –
     * erst der Durchschnitt zeigt den Trend. Am Anfang wird über die vorhandenen Werte gemittelt.
     */
    public static double[] movingAverage(double[] values, int window) {
        if (window < 1) {
            throw new IllegalArgumentException("window muss mindestens 1 sein");
        }
        double[] result = new double[values.length];
        double sum = 0;
        for (int i = 0; i < values.length; i++) {
            sum += values[i];
            if (i >= window) {
                sum -= values[i - window];
            }
            result[i] = sum / Math.min(i + 1, window);
        }
        return result;
    }

    /**
     * Wählt höchstens {@code maxPoints} gleichmäßig verteilte Indizes aus, der letzte ist immer dabei.
     * Ein Diagramm mit 10'000 Punkten pro Linie wäre langsam und nicht lesbarer.
     */
    public static List<Integer> sampleIndices(int size, int maxPoints) {
        if (maxPoints < 2) {
            throw new IllegalArgumentException("maxPoints muss mindestens 2 sein");
        }
        List<Integer> indices = new ArrayList<>();
        if (size == 0) {
            return indices;
        }
        int step = Math.max(1, (int) Math.ceil((double) (size - 1) / (maxPoints - 1)));
        for (int i = 0; i < size - 1; i += step) {
            indices.add(i);
        }
        indices.add(size - 1);
        return indices;
    }
}
