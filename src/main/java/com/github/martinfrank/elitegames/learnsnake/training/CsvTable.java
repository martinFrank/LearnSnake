package com.github.martinfrank.elitegames.learnsnake.training;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Eine eingelesene CSV-Datei, wie sie {@link CsvWriter} schreibt: Kopfzeile mit Spaltennamen, darunter Zahlen.
 */
public record CsvTable(List<String> columns, List<double[]> rows) {

    public CsvTable {
        columns = List.copyOf(columns);
        rows = List.copyOf(rows);
    }

    public static CsvTable read(Path file) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("CSV-Datei konnte nicht gelesen werden: " + file, e);
        }
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("Leere CSV-Datei: " + file);
        }
        List<String> columns = Arrays.asList(lines.getFirst().split(","));
        List<double[]> rows = new ArrayList<>();
        for (String line : lines.subList(1, lines.size())) {
            if (line.isBlank()) {
                continue;
            }
            String[] parts = line.split(",");
            if (parts.length != columns.size()) {
                throw new IllegalArgumentException("Falsche Spaltenzahl in " + file + ": " + line);
            }
            double[] row = new double[parts.length];
            for (int i = 0; i < parts.length; i++) {
                row[i] = Double.parseDouble(parts[i]);
            }
            rows.add(row);
        }
        return new CsvTable(columns, rows);
    }

    public boolean has(String column) {
        return columns.contains(column);
    }

    public double[] column(String column) {
        int index = columns.indexOf(column);
        if (index < 0) {
            throw new IllegalArgumentException("Spalte '" + column + "' gibt es nicht, nur " + columns);
        }
        return rows.stream().mapToDouble(row -> row[index]).toArray();
    }
}
