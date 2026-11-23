package com.github.martinfrank.elitegames.learnsnake.agent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QTableTest {

    @Test
    void startsWithZeros() {
        QTable table = new QTable(4, 3);
        assertEquals(0.0, table.get(2, 1));
        assertEquals(0, table.updates(2));
        assertEquals(List.of(), table.visitedStates());
    }

    @Test
    void bestActionIsHighestValue() {
        QTable table = new QTable(1, 3);
        table.update(0, 0, -1.0);
        table.update(0, 1, 5.0);
        table.update(0, 2, 2.0);

        assertEquals(1, table.bestAction(0));
        assertEquals(5.0, table.maxValue(0));
    }

    @Test
    void tieGoesToLowestAction() {
        QTable table = new QTable(1, 3);
        assertEquals(0, table.bestAction(0));

        table.update(0, 1, 3.0);
        table.update(0, 2, 3.0);
        assertEquals(1, table.bestAction(0));
    }

    @Test
    void countsUpdates() {
        QTable table = new QTable(3, 2);
        table.update(1, 0, 1.0);
        table.update(1, 0, 2.0);
        table.update(1, 1, 1.0);

        assertEquals(2, table.updates(1, 0));
        assertEquals(3, table.updates(1));
        assertEquals(List.of(1), table.visitedStates());
    }

    @Test
    void saveAndLoadRoundTrip(@TempDir Path dir) {
        QTable table = new QTable(8, 3);
        table.update(5, 0, 1.25);
        table.update(5, 2, -9.5);
        table.update(7, 1, 0.125);

        Path file = dir.resolve("sub").resolve("q.csv");
        table.save(file);
        QTable loaded = QTable.load(file);

        assertEquals(8, loaded.stateCount());
        assertEquals(3, loaded.actionCount());
        for (int state = 0; state < 8; state++) {
            for (int action = 0; action < 3; action++) {
                assertEquals(table.get(state, action), loaded.get(state, action), 1e-9);
                assertEquals(table.updates(state, action), loaded.updates(state, action));
            }
        }
    }

    @Test
    void savedFileIsReadable(@TempDir Path dir) throws IOException {
        QTable table = new QTable(4, 2);
        table.update(3, 1, 2.5);

        Path file = dir.resolve("q.csv");
        table.save(file);

        assertEquals(List.of(
                "# Q-Table states=4 actions=2",
                "state,q0,q1,updates0,updates1",
                "3,0.000000,2.500000,0,1"), Files.readAllLines(file));
    }

    @Test
    void rejectsForeignFile(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("other.csv");
        Files.writeString(file, "a,b,c\n1,2,3\n");
        assertThrows(IllegalArgumentException.class, () -> QTable.load(file));
    }
}
