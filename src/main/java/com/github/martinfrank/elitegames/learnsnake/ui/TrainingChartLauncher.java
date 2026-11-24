package com.github.martinfrank.elitegames.learnsnake.ui;

import javafx.application.Application;

/**
 * Startet die Lernkurven-Ansicht, z.B. direkt aus IntelliJ (Grund für die eigene Klasse: siehe
 * {@link SnakeAppLauncher}). Alternativ: {@code mvn javafx:run@chart} bzw. {@code startChart.bat}.
 */
public final class TrainingChartLauncher {

    static void main(String[] args) {
        Application.launch(TrainingChartApp.class, args);
    }
}
