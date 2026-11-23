package com.github.martinfrank.elitegames.learnsnake.ui;

import com.github.martinfrank.elitegames.learnsnake.Main;
import com.github.martinfrank.elitegames.learnsnake.agent.Agent;
import com.github.martinfrank.elitegames.learnsnake.agent.QLearningAgent;
import com.github.martinfrank.elitegames.learnsnake.agent.QLearningParameters;
import com.github.martinfrank.elitegames.learnsnake.agent.QTable;
import com.github.martinfrank.elitegames.learnsnake.agent.RandomAgent;
import com.github.martinfrank.elitegames.learnsnake.agent.RuleBasedSnakeAgent;
import com.github.martinfrank.elitegames.learnsnake.environment.StepResult;
import com.github.martinfrank.elitegames.learnsnake.game.snake.Direction;
import com.github.martinfrank.elitegames.learnsnake.game.snake.SnakeEnvironment;
import com.github.martinfrank.elitegames.learnsnake.training.Trainer;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.util.Locale;

/**
 * Oberfläche zum Selbstspielen und zum Zuschauen, wie ein Agent spielt.
 * <p>
 * Mensch und Agent laufen durch dieselbe Spielschleife wie im Training:
 * Observation → {@code agent.chooseAction()} → {@code environment.step()} → {@code agent.learn()}.
 * Die Oberfläche bestimmt nur das Tempo und zeichnet das Ergebnis.
 */
public final class SnakeApp extends Application {

    private static final int COLUMNS = 10;
    private static final int ROWS = 10;
    private static final long AGENT_RESTART_DELAY_NANOS = 1_000_000_000L;
    /**
     * Beim Zuschauen soll man den Lernfortschritt nach wenigen Dutzend Episoden sehen:
     * ε sinkt deshalb viel schneller als im Training ohne Oberfläche.
     */
    private static final QLearningParameters LIVE_PARAMETERS = new QLearningParameters(0.1, 0.9, 1.0, 0.01, 0.95);
    /** Falls noch keine gespeicherte Q-Table existiert, wird so lange ohne Oberfläche trainiert. */
    private static final int FALLBACK_TRAINING_EPISODES = 5_000;

    /** Wer spielt? Neue Agenten (Q-Learning, ...) werden hier ergänzt. */
    private enum Mode {
        HUMAN("Selbst spielen"),
        RANDOM("Random Agent"),
        RULE_BASED("Rule-Based Agent"),
        Q_LEARNING_LIVE("Q-Learning (lernt live)"),
        Q_LEARNING_TRAINED("Q-Learning (trainiert)");

        private final String label;

        Mode(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private enum State {
        /** Wartet auf den ersten Tastendruck. */
        READY,
        RUNNING,
        PAUSED,
        GAME_OVER
    }

    private final SnakeEnvironment environment = new SnakeEnvironment(COLUMNS, ROWS, System.nanoTime());
    private final KeyboardAgent keyboard = new KeyboardAgent(environment.game());
    private final SnakeView view = new SnakeView(COLUMNS, ROWS);
    private final Label status = new Label();
    private final Slider speed = new Slider(20, 400, 150);

    private Mode mode = Mode.HUMAN;
    private Agent agent = keyboard;
    private State state;
    private double[] observation;
    private StepResult lastResult;
    private long lastStepNanos;
    private long gameOverNanos;
    private int episodes;
    private int bestScore;

    @Override
    public void start(Stage stage) {
        ComboBox<Mode> modeChoice = new ComboBox<>(FXCollections.observableArrayList(Mode.values()));
        modeChoice.setValue(mode);
        modeChoice.setOnAction(_ -> switchMode(modeChoice.getValue()));
        modeChoice.setFocusTraversable(false);

        speed.setFocusTraversable(false);
        Label speedLabel = new Label();
        speedLabel.textProperty().bind(speed.valueProperty().asString("Tempo: %.0f ms/Schritt"));

        HBox controls = new HBox(12, new Label("Spieler:"), modeChoice, speedLabel, speed);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(8));

        Label help = new Label("Pfeiltasten / WASD: steuern    Leertaste: Pause    Enter: neues Spiel");
        VBox bottom = new VBox(4, status, help);
        bottom.setPadding(new Insets(8));

        BorderPane root = new BorderPane(view);
        root.setTop(controls);
        root.setBottom(bottom);

        Scene scene = new Scene(root);
        // Filter statt Handler: Die Tasten sollen beim Spiel ankommen, nicht bei ComboBox oder Slider
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::onKeyPressed);

        stage.setTitle("LearnSnake");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        newGame();
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                update(now);
                render();
            }
        }.start();
    }

    private void onKeyPressed(KeyEvent event) {
        switch (event.getCode()) {
            case UP, W -> steer(Direction.UP);
            case DOWN, S -> steer(Direction.DOWN);
            case LEFT, A -> steer(Direction.LEFT);
            case RIGHT, D -> steer(Direction.RIGHT);
            case SPACE -> togglePause();
            case ENTER, R -> newGame();
            default -> {
                return;
            }
        }
        event.consume();
    }

    private void steer(Direction direction) {
        if (mode != Mode.HUMAN) {
            return;
        }
        keyboard.press(direction);
        if (state == State.READY) {
            state = State.RUNNING;
            lastStepNanos = System.nanoTime();
        }
    }

    private void togglePause() {
        if (state == State.RUNNING) {
            state = State.PAUSED;
        } else if (state == State.PAUSED) {
            state = State.RUNNING;
        }
    }

    private void switchMode(Mode newMode) {
        mode = newMode;
        agent = switch (newMode) {
            case HUMAN -> keyboard;
            case RANDOM -> new RandomAgent(environment.actionCount(), System.nanoTime());
            case RULE_BASED -> new RuleBasedSnakeAgent();
            case Q_LEARNING_LIVE -> new QLearningAgent(SnakeEnvironment.STATE_ENCODER,
                    SnakeEnvironment.ACTION_COUNT, LIVE_PARAMETERS, System.nanoTime());
            case Q_LEARNING_TRAINED -> trainedQLearningAgent();
        };
        episodes = 0;
        bestScore = 0;
        newGame();
    }

    /**
     * Lädt die von {@link Main} gespeicherte Q-Table. Gibt es noch keine, wird kurz ohne Oberfläche trainiert.
     */
    private static QLearningAgent trainedQLearningAgent() {
        QLearningAgent agent;
        if (Files.exists(Main.Q_TABLE_FILE)) {
            agent = new QLearningAgent(SnakeEnvironment.STATE_ENCODER, QTable.load(Main.Q_TABLE_FILE),
                    QLearningParameters.DEFAULT, 0L);
        } else {
            agent = new QLearningAgent(SnakeEnvironment.STATE_ENCODER, SnakeEnvironment.ACTION_COUNT,
                    QLearningParameters.DEFAULT, System.nanoTime());
            new Trainer(new SnakeEnvironment(COLUMNS, ROWS, System.nanoTime()), agent)
                    .run(FALLBACK_TRAINING_EPISODES);
        }
        agent.setTraining(false);
        return agent;
    }

    private void newGame() {
        observation = environment.reset();
        keyboard.clear();
        lastResult = null;
        state = mode == Mode.HUMAN ? State.READY : State.RUNNING;
        lastStepNanos = System.nanoTime();
    }

    private void update(long now) {
        switch (state) {
            case RUNNING -> {
                long stepNanos = (long) (speed.getValue() * 1_000_000);
                if (now - lastStepNanos >= stepNanos) {
                    lastStepNanos = now;
                    step(now);
                }
            }
            case GAME_OVER -> {
                if (mode != Mode.HUMAN && now - gameOverNanos >= AGENT_RESTART_DELAY_NANOS) {
                    newGame();
                }
            }
            case READY, PAUSED -> {
                // nichts zu tun
            }
        }
    }

    /** Ein Schritt der Spielschleife – genau wie im {@code Trainer}. */
    private void step(long now) {
        int action = agent.chooseAction(observation);
        StepResult result = environment.step(action);
        agent.learn(observation, action, result);
        observation = result.observation();
        lastResult = result;

        if (result.done()) {
            state = State.GAME_OVER;
            gameOverNanos = now;
            episodes++;
            bestScore = Math.max(bestScore, environment.score());
        }
    }

    private void render() {
        view.draw(environment.game(), message());
        String text = String.format("Score: %d    Länge: %d    Schritte: %d    Episoden: %d    Bester Score: %d",
                environment.score(), environment.game().length(), environment.game().steps(), episodes, bestScore);
        if (agent instanceof QLearningAgent qAgent && qAgent.isTraining()) {
            text += String.format(Locale.ROOT, "    ε: %.3f    bekannte Zustände: %d",
                    qAgent.epsilon(), qAgent.table().visitedStates().size());
        }
        status.setText(text);
    }

    private String message() {
        return switch (state) {
            case READY -> "Pfeiltaste drücken zum Starten";
            case PAUSED -> "Pause\nLeertaste zum Fortsetzen";
            case GAME_OVER -> gameOverMessage();
            case RUNNING -> null;
        };
    }

    private String gameOverMessage() {
        String reason;
        if (lastResult.truncated()) {
            reason = "Zu lange kein Essen gefunden";
        } else if (environment.game().food().isEmpty()) {
            reason = "Gewonnen! Das Feld ist voll";
        } else {
            reason = "Game Over";
        }
        String next = mode == Mode.HUMAN ? "Enter für ein neues Spiel" : "Nächste Episode startet gleich ...";
        return reason + "\nScore: " + environment.score() + "\n" + next;
    }
}
