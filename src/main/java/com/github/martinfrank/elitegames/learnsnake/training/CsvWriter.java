package com.github.martinfrank.elitegames.learnsnake.training;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Schreibt Messwerte zeilenweise in eine CSV-Datei – lesbar mit Excel, LibreOffice, Python/pandas
 * oder {@link CsvTable}.
 * <p>
 * Alle Werte werden als Zahlen geschrieben (Wahrheitswerte als 1/0), mit Punkt als Dezimaltrenner.
 */
public final class CsvWriter implements AutoCloseable {

    private final Path file;
    private final List<String> columns;
    private final BufferedWriter writer;

    public CsvWriter(Path file, List<String> columns) {
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("Mindestens eine Spalte nötig");
        }
        this.file = file;
        this.columns = List.copyOf(columns);
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8);
            writer.write(String.join(",", columns));
            writer.newLine();
        } catch (IOException e) {
            throw new UncheckedIOException("CSV-Datei konnte nicht angelegt werden: " + file, e);
        }
    }

    public void writeRow(Object... values) {
        if (values.length != columns.size()) {
            throw new IllegalArgumentException("Erwartet " + columns.size() + " Werte " + columns
                    + ", erhalten " + values.length);
        }
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                line.append(',');
            }
            line.append(format(values[i]));
        }
        try {
            writer.write(line.toString());
            writer.newLine();
        } catch (IOException e) {
            throw new UncheckedIOException("Schreiben fehlgeschlagen: " + file, e);
        }
    }

    private static String format(Object value) {
        return switch (value) {
            case Double d -> String.format(Locale.ROOT, "%.4f", d);
            case Float f -> String.format(Locale.ROOT, "%.4f", f);
            case Boolean b -> b ? "1" : "0";
            case Number n -> n.toString();
            default -> throw new IllegalArgumentException("Nur Zahlen und Wahrheitswerte erlaubt: " + value);
        };
    }

    @Override
    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            throw new UncheckedIOException("Schließen fehlgeschlagen: " + file, e);
        }
    }
}
