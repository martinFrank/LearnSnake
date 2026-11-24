package com.github.martinfrank.elitegames.learnsnake.training;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

/**
 * Schreibt jede Episode als Zeile in eine CSV-Datei (Spalten siehe {@link Metrics#EPISODE_COLUMNS}).
 * <p>
 * Passt direkt in {@link Trainer#run(int, Consumer)}:
 * <pre>
 * try (EpisodeLog log = new EpisodeLog(Path.of("results", "random.csv"))) {
 *     trainer.run(1000, log);
 * }
 * </pre>
 * Zusätzliche Spalten (z.B. ε eines lernenden Agenten) werden nach jeder Episode abgefragt.
 */
public final class EpisodeLog implements Consumer<EpisodeResult>, AutoCloseable {

    private final CsvWriter writer;
    private final List<DoubleSupplier> extraValues;
    private long totalSteps;

    public EpisodeLog(Path file) {
        this(file, Map.of());
    }

    /** @param extraColumns Spaltenname → Wert, der nach jeder Episode abgefragt wird */
    public EpisodeLog(Path file, Map<String, DoubleSupplier> extraColumns) {
        Map<String, DoubleSupplier> ordered = new LinkedHashMap<>(extraColumns);
        List<String> columns = new ArrayList<>(Metrics.EPISODE_COLUMNS);
        columns.addAll(ordered.keySet());
        this.writer = new CsvWriter(file, columns);
        this.extraValues = List.copyOf(ordered.values());
    }

    @Override
    public void accept(EpisodeResult result) {
        totalSteps += result.steps();
        List<Object> values = new ArrayList<>(List.of(result.episode(), totalSteps, result.totalReward(),
                result.steps(), result.score(), result.truncated()));
        extraValues.forEach(supplier -> values.add(supplier.getAsDouble()));
        writer.writeRow(values.toArray());
    }

    /** Alle Schritte aller bisher protokollierten Episoden. */
    public long totalSteps() {
        return totalSteps;
    }

    @Override
    public void close() {
        writer.close();
    }
}
