package com.github.martinfrank.elitegames.learnsnake.game.snake;

/**
 * Belohnungen im {@link SnakeEnvironment}. Als eigener Record, damit man leicht mit
 * unterschiedlichen Reward-Systemen experimentieren kann.
 *
 * @param food  Essen gefressen
 * @param death Kollision mit Wand oder eigenem Körper
 * @param step  jeder andere Schritt – bewusst nicht positiv, sonst lernt die Schlange im Kreis zu laufen
 */
public record SnakeRewards(double food, double death, double step) {

    public static final SnakeRewards DEFAULT = new SnakeRewards(10, -10, 0);
}
