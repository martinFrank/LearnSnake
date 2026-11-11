package com.github.martinfrank.elitegames.learnsnake.agent;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomAgentTest {

    @Test
    void choosesEveryActionWithinRange() {
        RandomAgent agent = new RandomAgent(3, 1L);
        Set<Integer> seen = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            int action = agent.chooseAction(new double[0]);
            assertTrue(action >= 0 && action < 3);
            seen.add(action);
        }
        assertEquals(Set.of(0, 1, 2), seen);
    }

    @Test
    void rejectsInvalidActionCount() {
        assertThrows(IllegalArgumentException.class, () -> new RandomAgent(0, 1L));
    }
}
