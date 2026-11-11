package com.github.martinfrank.elitegames.learnsnake.game.snake;

/**
 * Was bei einem Spielschritt passiert ist.
 * <p>
 * Das Spiel selbst kennt keine Rewards – die Übersetzung in Belohnungen
 * ist Aufgabe des Environments (Schritt 2).
 */
public enum StepOutcome {
    /** Normaler Schritt, nichts besonderes passiert. */
    MOVED(false),
    /** Essen gefressen, die Schlange ist gewachsen. */
    ATE_FOOD(false),
    /** In die Wand gelaufen – Spielende. */
    HIT_WALL(true),
    /** In den eigenen Körper gelaufen – Spielende. */
    HIT_SELF(true),
    /** Die Schlange füllt das ganze Feld aus – Spiel gewonnen. */
    BOARD_FULL(true);

    private final boolean terminal;

    StepOutcome(boolean terminal) {
        this.terminal = terminal;
    }

    public boolean isTerminal() {
        return terminal;
    }
}
