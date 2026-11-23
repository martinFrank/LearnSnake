package com.github.martinfrank.elitegames.learnsnake.agent;

/**
 * Die Stellschrauben (Hyperparameter) des Q-Learning-Agenten.
 *
 * @param learningRate   α (0..1]: Wie stark ein neues Erlebnis den alten Wert verschiebt.
 *                       0.1 heißt: 10 % neu, 90 % bisheriges Wissen.
 * @param discountFactor γ [0..1]: Wie viel spätere Belohnungen zählen. 0 = nur die sofortige Belohnung,
 *                       nahe 1 = weit vorausdenken.
 * @param epsilonStart   ε zu Beginn: Wahrscheinlichkeit, eine zufällige statt der besten Aktion zu wählen.
 * @param epsilonMin     Untergrenze für ε – ein wenig Neugier bleibt immer.
 * @param epsilonDecay   Faktor, mit dem ε nach jeder Episode multipliziert wird.
 */
public record QLearningParameters(double learningRate, double discountFactor,
                                  double epsilonStart, double epsilonMin, double epsilonDecay) {

    /** Für langes Training ohne Oberfläche: ε erreicht nach rund 2300 Episoden das Minimum. */
    public static final QLearningParameters DEFAULT = new QLearningParameters(0.1, 0.9, 1.0, 0.01, 0.998);

    public QLearningParameters {
        requireInRange("learningRate", learningRate, 0, 1, false);
        requireInRange("discountFactor", discountFactor, 0, 1, true);
        requireInRange("epsilonStart", epsilonStart, 0, 1, true);
        requireInRange("epsilonMin", epsilonMin, 0, 1, true);
        requireInRange("epsilonDecay", epsilonDecay, 0, 1, true);
        if (epsilonMin > epsilonStart) {
            throw new IllegalArgumentException("epsilonMin darf nicht größer als epsilonStart sein");
        }
    }

    private static void requireInRange(String name, double value, double min, double max, boolean minInclusive) {
        boolean aboveMin = minInclusive ? value >= min : value > min;
        if (!aboveMin || value > max) {
            throw new IllegalArgumentException(name + " muss zwischen " + min + " und " + max + " liegen: " + value);
        }
    }
}
