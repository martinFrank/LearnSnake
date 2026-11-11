package com.github.martinfrank.elitegames.learnsnake.ui;

import javafx.application.Application;

/**
 * Startet die Oberfläche, z.B. direkt aus IntelliJ.
 * <p>
 * Warum eine eigene Klasse? Startet man eine Klasse, die von {@link Application} erbt, direkt vom Classpath,
 * bricht Java mit "JavaFX runtime components are missing" ab. Über diesen Umweg funktioniert es.
 * Alternativ: {@code mvn javafx:run}.
 */
public final class SnakeAppLauncher {

    static void main(String[] args) {
        Application.launch(SnakeApp.class, args);
    }
}
