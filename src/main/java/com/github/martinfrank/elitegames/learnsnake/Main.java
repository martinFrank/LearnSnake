package com.github.martinfrank.elitegames.learnsnake;

import com.github.martinfrank.elitegames.learnsnake.agent.Agent;
import com.github.martinfrank.elitegames.learnsnake.agent.QLearningAgent;
import com.github.martinfrank.elitegames.learnsnake.agent.QLearningParameters;
import com.github.martinfrank.elitegames.learnsnake.agent.QTable;
import com.github.martinfrank.elitegames.learnsnake.agent.RandomAgent;
import com.github.martinfrank.elitegames.learnsnake.agent.RuleBasedSnakeAgent;
import com.github.martinfrank.elitegames.learnsnake.game.snake.SnakeEnvironment;
import com.github.martinfrank.elitegames.learnsnake.training.CsvWriter;
import com.github.martinfrank.elitegames.learnsnake.training.EpisodeLog;
import com.github.martinfrank.elitegames.learnsnake.training.Metrics;
import com.github.martinfrank.elitegames.learnsnake.training.Trainer;
import com.github.martinfrank.elitegames.learnsnake.training.TrainingStatistics;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Vergleicht die Agenten auf Snake:
 * <ol>
 *     <li>Random und Rule-Based als Vergleichslinien</li>
 *     <li>Q-Learning trainieren, dabei regelmäßig ohne Zufall auswerten</li>
 *     <li>Q-Learning ohne Zufall und ohne Weiterlernen auswerten</li>
 *     <li>Die wichtigsten Zeilen der Q-Table zeigen und die Tabelle speichern</li>
 * </ol>
 * Alle Messwerte landen als CSV in {@link #RESULTS_DIR} – anzeigen mit {@code startChart.bat}.
 */
public final class Main {

    public static final Path Q_TABLE_FILE = Path.of("models", "snake-qtable.csv");
    public static final Path RESULTS_DIR = Path.of("results");

    private static final int WIDTH = 10;
    private static final int HEIGHT = 10;
    private static final int EVALUATION_EPISODES = 10_000;
    private static final int TRAINING_EPISODES = 10_000;
    /** Alle so viele Trainingsepisoden wird ohne Zufall gemessen ... */
    private static final int EVALUATION_INTERVAL = 250;
    /** ... und zwar auf so vielen Episoden. */
    private static final int INTERVAL_EVALUATION_EPISODES = 200;
    private static final int PROGRESS_INTERVAL = 1_000;
    private static final int REPORT_ROWS = 20;
    private static final long SEED = 42L;
    /** Anderer Seed für die Auswertung: Der Agent soll auf Spielen getestet werden, die er nicht trainiert hat. */
    private static final long EVALUATION_SEED = 4242L;

    static void main() {
        evaluate("Random Agent", "random", new RandomAgent(SnakeEnvironment.ACTION_COUNT, SEED));
        evaluate("Rule-Based Agent", "rule-based", new RuleBasedSnakeAgent());

        QLearningAgent qAgent = train();
        qAgent.setTraining(false);
        evaluate("Q-Learning Agent (nach Training, ohne Zufall)", "q-learning", qAgent);

        printQTable(qAgent.table());
        qAgent.table().save(Q_TABLE_FILE);
        System.out.println("Q-Table gespeichert: " + Q_TABLE_FILE.toAbsolutePath());
        System.out.println("Messwerte gespeichert: " + RESULTS_DIR.toAbsolutePath() + " (Diagramm: startChart.bat)");
    }

    private static QLearningAgent train() {
        QLearningParameters parameters = QLearningParameters.DEFAULT;
        QLearningAgent agent = new QLearningAgent(SnakeEnvironment.STATE_ENCODER, SnakeEnvironment.ACTION_COUNT,
                parameters, SEED);
        Trainer trainer = new Trainer(new SnakeEnvironment(WIDTH, HEIGHT, SEED), agent);
        System.out.printf("Q-Learning Training auf %dx%d: %s%n", WIDTH, HEIGHT, parameters);

        try (EpisodeLog log = new EpisodeLog(RESULTS_DIR.resolve("q-learning-training.csv"),
                Map.of(Metrics.EPSILON, agent::epsilon));
             CsvWriter evaluationLog = new CsvWriter(RESULTS_DIR.resolve("q-learning-evaluation.csv"),
                     Metrics.EVALUATION_COLUMNS)) {

            TrainingStatistics training = new TrainingStatistics();
            for (int trained = 0; trained < TRAINING_EPISODES; trained += EVALUATION_INTERVAL) {
                trainer.run(EVALUATION_INTERVAL, result -> {
                    log.accept(result);
                    training.add(result);
                });

                TrainingStatistics evaluation = evaluateWithoutExploration(agent);
                int episodes = trained + EVALUATION_INTERVAL;
                evaluationLog.writeRow(episodes, log.totalSteps(),
                        evaluation.averageReward(), evaluation.rewardStandardDeviation(),
                        evaluation.averageScore(), evaluation.scoreStandardDeviation(),
                        evaluation.averageSteps(), evaluation.maxScore());

                if (episodes % PROGRESS_INTERVAL == 0) {
                    TrainingStatistics window = training.last(PROGRESS_INTERVAL);
                    System.out.printf(Locale.ROOT,
                            "  Episode %6d | ε %.3f | Training: Score avg %5.2f | Auswertung ohne Zufall:"
                                    + " Score avg %5.2f ± %4.2f | bekannte Zustände %d%n",
                            episodes, agent.epsilon(), window.averageScore(),
                            evaluation.averageScore(), evaluation.scoreStandardDeviation(),
                            agent.table().visitedStates().size());
                }
            }
        }
        System.out.println();
        return agent;
    }

    /** Misst den aktuellen Stand: ohne Zufall, ohne Lernen, immer auf denselben Spielen. */
    private static TrainingStatistics evaluateWithoutExploration(QLearningAgent agent) {
        agent.setTraining(false);
        try {
            return new Trainer(new SnakeEnvironment(WIDTH, HEIGHT, EVALUATION_SEED), agent)
                    .run(INTERVAL_EVALUATION_EPISODES);
        } finally {
            agent.setTraining(true);
        }
    }

    private static void evaluate(String name, String fileName, Agent agent) {
        SnakeEnvironment environment = new SnakeEnvironment(WIDTH, HEIGHT, EVALUATION_SEED);
        Trainer trainer = new Trainer(environment, agent);

        long start = System.nanoTime();
        TrainingStatistics statistics;
        try (EpisodeLog log = new EpisodeLog(RESULTS_DIR.resolve(fileName + ".csv"))) {
            statistics = trainer.run(EVALUATION_EPISODES, log);
        }
        long millis = (System.nanoTime() - start) / 1_000_000;

        System.out.printf("%s auf %dx%d:%n", name, WIDTH, HEIGHT);
        System.out.println(statistics.summary());
        System.out.printf("Dauer: %d ms (%.0f Schritte/s)%n%n",
                millis, statistics.averageSteps() * EVALUATION_EPISODES / Math.max(1, millis) * 1000);
    }

    /** Zeigt die am häufigsten erlebten Zustände mit ihren Q-Werten. */
    private static void printQTable(QTable table) {
        List<Integer> states = table.visitedStates().stream()
                .sorted(Comparator.comparingLong((Integer state) -> table.updates(state)).reversed())
                .toList();

        System.out.printf("Q-Table: %d von %d möglichen Zuständen erlebt. Die %d häufigsten:%n%n",
                states.size(), table.stateCount(), Math.min(REPORT_ROWS, states.size()));
        System.out.printf("%7s %9s  %-45s %10s %10s %10s  %s%n",
                "Zustand", "Updates", "Bedeutung", "Q(gerade)", "Q(links)", "Q(rechts)", "beste Aktion");
        for (int state : states.subList(0, Math.min(REPORT_ROWS, states.size()))) {
            System.out.printf(Locale.ROOT, "%7d %9d  %-45s %10.3f %10.3f %10.3f  %s%n",
                    state, table.updates(state), SnakeEnvironment.describeState(state),
                    table.get(state, SnakeEnvironment.ACTION_STRAIGHT),
                    table.get(state, SnakeEnvironment.ACTION_LEFT),
                    table.get(state, SnakeEnvironment.ACTION_RIGHT),
                    SnakeEnvironment.actionName(table.bestAction(state)));
        }
        System.out.println();
    }
}
