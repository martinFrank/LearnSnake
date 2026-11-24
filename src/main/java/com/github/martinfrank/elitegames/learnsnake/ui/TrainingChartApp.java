package com.github.martinfrank.elitegames.learnsnake.ui;

import com.github.martinfrank.elitegames.learnsnake.Main;
import com.github.martinfrank.elitegames.learnsnake.training.CsvTable;
import com.github.martinfrank.elitegames.learnsnake.training.Curves;
import com.github.martinfrank.elitegames.learnsnake.training.Metrics;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Zeigt die von {@link Main} geschriebenen CSV-Dateien als Lernkurven.
 * <p>
 * Dateien mit einer Zeile pro Episode werden als gleitender Durchschnitt gezeichnet,
 * Auswertungsdateien (bereits Mittelwerte) direkt.
 */
public final class TrainingChartApp extends Application {

    private static final int MAX_POINTS_PER_SERIES = 400;

    /** Was auf der y-Achse steht – und wie die Spalte in Episoden- bzw. Auswertungsdateien heißt. */
    private enum Measure {
        SCORE("Score", Metrics.SCORE, Metrics.MEAN_SCORE),
        RETURN("Return (Summe der Rewards)", Metrics.RETURN, Metrics.MEAN_RETURN),
        LENGTH("Episodenlänge (Schritte)", Metrics.LENGTH, Metrics.MEAN_LENGTH),
        EPSILON("ε (Explorationsrate)", Metrics.EPSILON, null);

        private final String label;
        private final String episodeColumn;
        private final String evaluationColumn;

        Measure(String label, String episodeColumn, String evaluationColumn) {
            this.label = label;
            this.episodeColumn = episodeColumn;
            this.evaluationColumn = evaluationColumn;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private enum XAxis {
        EPISODE("Episode", Metrics.EPISODE),
        TOTAL_STEPS("Schritte gesamt (Timesteps)", Metrics.TOTAL_STEPS);

        private final String label;
        private final String column;

        XAxis(String label, String column) {
            this.label = label;
            this.column = column;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final ComboBox<Measure> measureChoice =
            new ComboBox<>(FXCollections.observableArrayList(Measure.values()));
    private final ComboBox<XAxis> xAxisChoice = new ComboBox<>(FXCollections.observableArrayList(XAxis.values()));
    private final Slider window = new Slider(1, 1000, 100);
    private final VBox fileList = new VBox(6);
    private final Label info = new Label();
    private final NumberAxis xAxis = new NumberAxis();
    private final NumberAxis yAxis = new NumberAxis();
    private final LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
    private final Map<String, CsvTable> tables = new LinkedHashMap<>();
    private final Map<String, CheckBox> visible = new LinkedHashMap<>();

    @Override
    public void start(Stage stage) {
        measureChoice.setValue(Measure.SCORE);
        xAxisChoice.setValue(XAxis.EPISODE);
        measureChoice.setOnAction(_ -> redraw());
        xAxisChoice.setOnAction(_ -> redraw());
        window.valueProperty().addListener((_, _, _) -> redraw());

        Label windowLabel = new Label();
        windowLabel.textProperty().bind(window.valueProperty().asString("Gleitender Durchschnitt: %.0f Episoden"));
        Button reload = new Button("Neu laden");
        reload.setOnAction(_ -> load());

        HBox controls = new HBox(12, new Label("Messgröße:"), measureChoice, new Label("x-Achse:"), xAxisChoice,
                windowLabel, window, reload);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(8));

        VBox side = new VBox(8, new Label("Dateien in " + Main.RESULTS_DIR + "/"), fileList);
        side.setPadding(new Insets(8));

        chart.setCreateSymbols(false);
        chart.setAnimated(false);
        yAxis.setForceZeroInRange(true);

        info.setPadding(new Insets(8));
        info.setWrapText(true);

        BorderPane root = new BorderPane(chart);
        root.setTop(controls);
        root.setLeft(side);
        root.setBottom(info);

        stage.setTitle("LearnSnake – Lernkurven");
        stage.setScene(new Scene(root, 1200, 700));
        stage.show();
        load();
    }

    private void load() {
        tables.clear();
        fileList.getChildren().clear();
        if (Files.isDirectory(Main.RESULTS_DIR)) {
            try (Stream<Path> files = Files.list(Main.RESULTS_DIR)) {
                files.filter(file -> file.getFileName().toString().endsWith(".csv"))
                        .sorted()
                        .forEach(file -> tables.put(nameOf(file), CsvTable.read(file)));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        // Neue Dateien sind sichtbar, bei bekannten bleibt die Auswahl beim Neuladen erhalten
        for (String name : tables.keySet()) {
            fileList.getChildren().add(visible.computeIfAbsent(name, _ -> {
                CheckBox box = new CheckBox(name);
                box.setSelected(true);
                box.setOnAction(_ -> redraw());
                return box;
            }));
        }
        redraw();
    }

    private static String nameOf(Path file) {
        String fileName = file.getFileName().toString();
        return fileName.substring(0, fileName.length() - ".csv".length());
    }

    private void redraw() {
        Measure measure = measureChoice.getValue();
        XAxis x = xAxisChoice.getValue();
        int windowSize = (int) Math.round(window.getValue());

        List<XYChart.Series<Number, Number>> allSeries = new ArrayList<>();
        for (Map.Entry<String, CsvTable> entry : tables.entrySet()) {
            if (!visible.get(entry.getKey()).isSelected()) {
                continue;
            }
            CsvTable table = entry.getValue();
            if (!table.has(x.column)) {
                continue;
            }
            if (table.has(measure.episodeColumn)) {
                double[] smoothed = Curves.movingAverage(table.column(measure.episodeColumn), windowSize);
                allSeries.add(series(entry.getKey(), table.column(x.column), smoothed));
            } else if (measure.evaluationColumn != null && table.has(measure.evaluationColumn)) {
                allSeries.add(series(entry.getKey() + " (Mittelwert je Auswertung)",
                        table.column(x.column), table.column(measure.evaluationColumn)));
            }
        }
        chart.getData().setAll(allSeries);
        xAxis.setLabel(x.label);
        yAxis.setLabel(measure.label);

        info.setText(tables.isEmpty()
                ? "Keine Messwerte gefunden. Zuerst Main ausführen – es schreibt die CSV-Dateien nach "
                + Main.RESULTS_DIR.toAbsolutePath()
                : "Einzelne Episoden schwanken stark, deshalb wird über die letzten " + windowSize
                + " Episoden gemittelt. Auswertungsdateien enthalten bereits Mittelwerte und werden nicht"
                + " zusätzlich geglättet.");
    }

    private static XYChart.Series<Number, Number> series(String name, double[] xs, double[] ys) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(name);
        for (int i : Curves.sampleIndices(xs.length, MAX_POINTS_PER_SERIES)) {
            series.getData().add(new XYChart.Data<>(xs[i], ys[i]));
        }
        return series;
    }
}
