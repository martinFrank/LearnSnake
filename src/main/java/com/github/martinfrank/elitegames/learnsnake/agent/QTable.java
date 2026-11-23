package com.github.martinfrank.elitegames.learnsnake.agent;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Das "Gedächtnis" des Q-Learning-Agenten: eine Tabelle mit einer Zeile pro Zustand und einer Spalte pro Aktion.
 * <pre>
 *            Aktion 0   Aktion 1   Aktion 2
 * Zustand 0     0.00       0.00       0.00
 * Zustand 1     3.12      -9.80       5.47     ← in Zustand 1 ist Aktion 2 am besten
 * ...
 * </pre>
 * Jeder Eintrag Q(s, a) ist die Schätzung: "Wie viel Belohnung bekomme ich insgesamt noch,
 * wenn ich in Zustand s die Aktion a wähle und danach gut weiterspiele?"
 * <p>
 * Zusätzlich wird gezählt, wie oft jede Kombination gelernt wurde. Das gehört nicht zum Algorithmus,
 * hilft aber beim Verstehen: Ein Wert, der erst zweimal aktualisiert wurde, ist noch wenig verlässlich.
 */
public final class QTable {

    private static final String HEADER_PREFIX = "# Q-Table";

    private final int stateCount;
    private final int actionCount;
    private final double[][] values;
    private final long[][] updates;

    public QTable(int stateCount, int actionCount) {
        if (stateCount < 1 || actionCount < 1) {
            throw new IllegalArgumentException("stateCount und actionCount müssen mindestens 1 sein");
        }
        this.stateCount = stateCount;
        this.actionCount = actionCount;
        this.values = new double[stateCount][actionCount];
        this.updates = new long[stateCount][actionCount];
    }

    public int stateCount() {
        return stateCount;
    }

    public int actionCount() {
        return actionCount;
    }

    public double get(int state, int action) {
        return values[state][action];
    }

    /** Setzt einen neuen Wert und zählt die Aktualisierung. */
    public void update(int state, int action, double value) {
        values[state][action] = value;
        updates[state][action]++;
    }

    public long updates(int state, int action) {
        return updates[state][action];
    }

    public long updates(int state) {
        long sum = 0;
        for (long count : updates[state]) {
            sum += count;
        }
        return sum;
    }

    /** Der Wert der besten Aktion in diesem Zustand: max über a von Q(s, a). */
    public double maxValue(int state) {
        return values[state][bestAction(state)];
    }

    /** Die Aktion mit dem höchsten Q-Wert. Bei Gleichstand gewinnt die kleinere Aktionsnummer. */
    public int bestAction(int state) {
        double[] row = values[state];
        int best = 0;
        for (int action = 1; action < actionCount; action++) {
            if (row[action] > row[best]) {
                best = action;
            }
        }
        return best;
    }

    /** Zustände, die mindestens einmal gelernt wurden – alle anderen stehen noch auf 0. */
    public List<Integer> visitedStates() {
        List<Integer> visited = new ArrayList<>();
        for (int state = 0; state < stateCount; state++) {
            if (updates(state) > 0) {
                visited.add(state);
            }
        }
        return visited;
    }

    /**
     * Speichert die Tabelle als lesbare CSV-Datei. Nur besuchte Zustände werden geschrieben.
     * <pre>
     * # Q-Table states=2048 actions=3
     * state,q0,q1,q2,updates0,updates1,updates2
     * 265,3.1200,-9.8000,5.4700,120,4,388
     * </pre>
     */
    public void save(Path file) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER_PREFIX + " states=" + stateCount + " actions=" + actionCount);

        StringBuilder columns = new StringBuilder("state");
        for (int action = 0; action < actionCount; action++) {
            columns.append(",q").append(action);
        }
        for (int action = 0; action < actionCount; action++) {
            columns.append(",updates").append(action);
        }
        lines.add(columns.toString());

        for (int state : visitedStates()) {
            StringBuilder line = new StringBuilder().append(state);
            for (double value : values[state]) {
                line.append(',').append(String.format(Locale.ROOT, "%.6f", value));
            }
            for (long count : updates[state]) {
                line.append(',').append(count);
            }
            lines.add(line.toString());
        }

        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Q-Table konnte nicht gespeichert werden: " + file, e);
        }
    }

    public static QTable load(Path file) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Q-Table konnte nicht gelesen werden: " + file, e);
        }
        if (lines.size() < 2 || !lines.getFirst().startsWith(HEADER_PREFIX)) {
            throw new IllegalArgumentException("Keine Q-Table-Datei: " + file);
        }

        QTable table = new QTable(headerValue(lines.getFirst(), "states"), headerValue(lines.getFirst(), "actions"));
        int actions = table.actionCount;
        for (String line : lines.subList(2, lines.size())) {
            if (line.isBlank()) {
                continue;
            }
            String[] parts = line.split(",");
            if (parts.length != 1 + 2 * actions) {
                throw new IllegalArgumentException("Ungültige Zeile in " + file + ": " + line);
            }
            int state = Integer.parseInt(parts[0]);
            for (int action = 0; action < actions; action++) {
                table.values[state][action] = Double.parseDouble(parts[1 + action]);
                table.updates[state][action] = Long.parseLong(parts[1 + actions + action]);
            }
        }
        return table;
    }

    private static int headerValue(String header, String key) {
        for (String token : header.split(" ")) {
            if (token.startsWith(key + "=")) {
                return Integer.parseInt(token.substring(key.length() + 1));
            }
        }
        throw new IllegalArgumentException("Kopfzeile enthält kein '" + key + "': " + header);
    }
}
