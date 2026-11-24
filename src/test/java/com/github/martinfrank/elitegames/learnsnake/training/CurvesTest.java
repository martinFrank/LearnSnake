package com.github.martinfrank.elitegames.learnsnake.training;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CurvesTest {

    @Test
    void movingAverageUsesLastWindowValues() {
        double[] values = {2, 4, 6, 8, 10};
        // Anfang: Mittel über die vorhandenen Werte, danach immer über die letzten 3
        assertArrayEquals(new double[]{2, 3, 4, 6, 8}, Curves.movingAverage(values, 3), 1e-9);
    }

    @Test
    void windowOfOneChangesNothing() {
        double[] values = {5, 1, 7};
        assertArrayEquals(values, Curves.movingAverage(values, 1), 1e-9);
    }

    @Test
    void rejectsInvalidWindow() {
        assertThrows(IllegalArgumentException.class, () -> Curves.movingAverage(new double[]{1}, 0));
    }

    @Test
    void sampleKeepsAllIndicesWhenFewValues() {
        assertEquals(List.of(0, 1, 2, 3), Curves.sampleIndices(4, 10));
    }

    @Test
    void sampleThinsOutAndKeepsLast() {
        List<Integer> indices = Curves.sampleIndices(10_000, 400);
        assertEquals(0, indices.getFirst());
        assertEquals(9_999, indices.getLast());
        assertEquals(true, indices.size() <= 400, "Anzahl " + indices.size());
    }

    @Test
    void sampleOfNothingIsEmpty() {
        assertEquals(List.of(), Curves.sampleIndices(0, 10));
    }
}
