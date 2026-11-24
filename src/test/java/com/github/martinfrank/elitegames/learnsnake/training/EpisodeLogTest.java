package com.github.martinfrank.elitegames.learnsnake.training;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EpisodeLogTest {

    @Test
    void writesOneRowPerEpisodeWithTotalSteps(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("sub").resolve("log.csv");
        double[] epsilon = {0.5};

        try (EpisodeLog log = new EpisodeLog(file, Map.of(Metrics.EPSILON, () -> epsilon[0]))) {
            log.accept(new EpisodeResult(1, 2, 30, 15.0, false));
            epsilon[0] = 0.25;
            log.accept(new EpisodeResult(2, 0, 12, -10.0, true));
            assertEquals(42, log.totalSteps());
        }

        assertEquals(List.of(
                "episode,total_steps,return,length,score,truncated,epsilon",
                "1,30,15.0000,30,2,0,0.5000",
                "2,42,-10.0000,12,0,1,0.2500"), Files.readAllLines(file));
    }

    @Test
    void csvTableReadsWhatWasWritten(@TempDir Path dir) {
        Path file = dir.resolve("eval.csv");
        try (CsvWriter writer = new CsvWriter(file, List.of("a", "b"))) {
            writer.writeRow(1, 2.5);
            writer.writeRow(3L, true);
        }

        CsvTable table = CsvTable.read(file);
        assertEquals(List.of("a", "b"), table.columns());
        assertTrue(table.has("b"));
        assertArrayEquals(new double[]{1, 3}, table.column("a"), 1e-9);
        assertArrayEquals(new double[]{2.5, 1}, table.column("b"), 1e-9);
        assertThrows(IllegalArgumentException.class, () -> table.column("c"));
    }

    @Test
    void writerRejectsWrongNumberOfValues(@TempDir Path dir) {
        try (CsvWriter writer = new CsvWriter(dir.resolve("x.csv"), List.of("a", "b"))) {
            assertThrows(IllegalArgumentException.class, () -> writer.writeRow(1));
            assertThrows(IllegalArgumentException.class, () -> writer.writeRow(1, "text"));
        }
    }
}
