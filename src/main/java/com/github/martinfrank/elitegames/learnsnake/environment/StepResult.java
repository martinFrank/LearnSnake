package com.github.martinfrank.elitegames.learnsnake.environment;

/**
 * Ergebnis eines Schritts im Environment.
 *
 * @param observation neuer Spielzustand
 * @param reward      Belohnung für die ausgeführte Aktion
 * @param terminated  Episode regulär beendet (z.B. Kollision) – es gibt keinen Folgezustand
 * @param truncated   Episode von außen abgebrochen (z.B. Schrittlimit) – das Spiel wäre eigentlich weitergegangen
 */
public record StepResult(double[] observation, double reward, boolean terminated, boolean truncated) {

    public boolean done() {
        return terminated || truncated;
    }
}
