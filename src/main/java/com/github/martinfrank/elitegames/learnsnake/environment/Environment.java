package com.github.martinfrank.elitegames.learnsnake.environment;

/**
 * Spielunabhängige Schnittstelle zwischen einem Spiel und einem Agenten (angelehnt an Gymnasium).
 * <p>
 * Observations sind {@code double[]} fester Länge, Aktionen sind Zahlen von {@code 0} bis
 * {@code actionCount() - 1}. Was die einzelnen Werte bedeuten, weiß nur das jeweilige Environment –
 * dadurch funktionieren Agenten mit jedem Spiel.
 */
public interface Environment {

    /** Startet eine neue Episode und liefert die erste Observation. */
    double[] reset();

    /**
     * Führt eine Aktion aus.
     *
     * @throws IllegalStateException    wenn die Episode bereits beendet ist
     * @throws IllegalArgumentException wenn die Aktion außerhalb des gültigen Bereichs liegt
     */
    StepResult step(int action);

    /** Länge jeder Observation. */
    int observationSize();

    /** Anzahl möglicher Aktionen. */
    int actionCount();

    /** Spielspezifische Punktzahl der laufenden Episode (z.B. gefressenes Essen bei Snake). */
    int score();
}
