package com.github.martinfrank.elitegames.learnsnake.environment;

/**
 * Übersetzt eine Observation in eine Zustandsnummer von {@code 0} bis {@code stateCount() - 1}.
 * <p>
 * Tabellarische Verfahren wie Q-Learning brauchen das: Sie haben eine Tabellenzeile pro Zustand
 * und können mit einem {@code double[]} direkt nichts anfangen.
 * <p>
 * Wie das am besten geht, hängt vom Spiel ab – deshalb legt das jeweilige Environment fest, welcher
 * Encoder zu seiner Observation passt:
 * <ul>
 *     <li>Snake liefert nur Ja/Nein-Merkmale → jedes Merkmal wird ein Bit ({@link BinaryStateEncoder}).</li>
 *     <li>Ein Spiel mit kontinuierlichen Werten (z.B. Flappy Bird) müsste die Werte erst in Bereiche
 *         einteilen (diskretisieren).</li>
 * </ul>
 */
public interface StateEncoder {

    /** Anzahl möglicher Zustände – die Anzahl Zeilen einer Q-Table. */
    int stateCount();

    /** @return Zustandsnummer von {@code 0} bis {@code stateCount() - 1} */
    int encode(double[] observation);
}
