package com.github.martinfrank.elitegames.learnsnake.agent;

import com.github.martinfrank.elitegames.learnsnake.environment.StepResult;

/**
 * Ein Spieler, der anhand einer Observation eine Aktion wählt.
 */
public interface Agent {

    /** Wählt eine Aktion zwischen {@code 0} und {@code actionCount - 1}. */
    int chooseAction(double[] observation);

    /**
     * Wird nach jedem Schritt aufgerufen: "In Zustand {@code observation} habe ich {@code action} gewählt,
     * daraufhin ist {@code result} passiert." Nicht-lernende Agenten ignorieren das.
     */
    default void learn(double[] observation, int action, StepResult result) {
    }
}
