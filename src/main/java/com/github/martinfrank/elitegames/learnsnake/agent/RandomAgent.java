package com.github.martinfrank.elitegames.learnsnake.agent;

import java.util.Random;

/**
 * Wählt jede Aktion gleich wahrscheinlich. Dient als untere Vergleichslinie:
 * Ein lernender Agent, der nicht besser ist als dieser, hat nichts gelernt.
 */
public final class RandomAgent implements Agent {

    private final int actionCount;
    private final Random random;

    public RandomAgent(int actionCount, long seed) {
        if (actionCount < 1) {
            throw new IllegalArgumentException("actionCount muss mindestens 1 sein");
        }
        this.actionCount = actionCount;
        this.random = new Random(seed);
    }

    @Override
    public int chooseAction(double[] observation) {
        return random.nextInt(actionCount);
    }
}
