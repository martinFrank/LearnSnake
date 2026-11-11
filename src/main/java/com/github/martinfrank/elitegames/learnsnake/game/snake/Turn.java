package com.github.martinfrank.elitegames.learnsnake.game.snake;

/**
 * Relative Aktion der Schlange, bezogen auf ihre aktuelle Bewegungsrichtung.
 * <p>
 * Weil es kein "Umdrehen" gibt, kann die Schlange nie direkt in sich selbst zurücklaufen.
 */
public enum Turn {
    STRAIGHT,
    LEFT,
    RIGHT
}
