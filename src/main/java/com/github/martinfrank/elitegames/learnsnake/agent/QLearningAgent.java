package com.github.martinfrank.elitegames.learnsnake.agent;

import com.github.martinfrank.elitegames.learnsnake.environment.StateEncoder;
import com.github.martinfrank.elitegames.learnsnake.environment.StepResult;

import java.util.Random;

/**
 * Lernt durch Ausprobieren, welche Aktion in welchem Zustand gut ist (tabellarisches Q-Learning).
 * <p>
 * Der Agent ist spielunabhängig: Ein {@link StateEncoder} übersetzt jede Observation in eine Zeile der Q-Table.
 *
 * <h2>Entscheiden: ε-greedy</h2>
 * Mit Wahrscheinlichkeit ε wird zufällig gewählt (Exploration: Neues ausprobieren),
 * sonst die Aktion mit dem höchsten Q-Wert (Exploitation: Wissen nutzen).
 * ε startet hoch und sinkt nach jeder Episode – erst viel ausprobieren, dann immer mehr nutzen.
 *
 * <h2>Lernen: die Update-Formel</h2>
 * <pre>
 * Q(s,a) ← Q(s,a) + α · [ r + γ · max Q(s',a') − Q(s,a) ]
 *                         └──────── Ziel ───────┘
 * </pre>
 * Das "Ziel" ist die neue Schätzung aus diesem einen Erlebnis: die sofortige Belohnung r plus der
 * abgezinste Wert des besten nächsten Zugs. Der alte Wert wird um den Anteil α in Richtung Ziel geschoben.
 * <p>
 * Bei {@code terminated} (z.B. Kollision) gibt es keinen nächsten Zug, das Ziel ist nur r.
 * Bei {@code truncated} (Schrittlimit) wäre das Spiel weitergegangen – dort zählt der Folgezustand mit.
 *
 * <h2>Training und Auswertung</h2>
 * Mit {@link #setTraining(boolean) setTraining(false)} spielt der Agent nur noch die beste bekannte Aktion
 * und verändert die Tabelle nicht mehr. So misst man, was er wirklich gelernt hat.
 */
public final class QLearningAgent implements Agent {

    private final StateEncoder encoder;
    private final QTable table;
    private final QLearningParameters parameters;
    private final Random random;
    private double epsilon;
    private boolean training = true;
    private int episodes;

    /**
     * Neuer Agent mit leerer Tabelle.
     *
     * @param encoder übersetzt die Observations des Spiels in Zeilennummern – das Environment weiß, welcher passt
     */
    public QLearningAgent(StateEncoder encoder, int actionCount, QLearningParameters parameters, long seed) {
        this(encoder, new QTable(encoder.stateCount(), actionCount), parameters, seed);
    }

    /** Agent mit vorhandener Tabelle, z.B. aus {@link QTable#load}. */
    public QLearningAgent(StateEncoder encoder, QTable table, QLearningParameters parameters, long seed) {
        if (encoder.stateCount() != table.stateCount()) {
            throw new IllegalArgumentException("Encoder kennt " + encoder.stateCount()
                    + " Zustände, die Q-Table hat aber " + table.stateCount() + " Zeilen");
        }
        this.encoder = encoder;
        this.table = table;
        this.parameters = parameters;
        this.random = new Random(seed);
        this.epsilon = parameters.epsilonStart();
    }

    @Override
    public int chooseAction(double[] observation) {
        int state = stateOf(observation);
        if (training && random.nextDouble() < epsilon) {
            return random.nextInt(table.actionCount());
        }
        return table.bestAction(state);
    }

    @Override
    public void learn(double[] observation, int action, StepResult result) {
        if (!training) {
            return;
        }
        int state = stateOf(observation);

        double target = result.reward();
        if (!result.terminated()) {
            int nextState = stateOf(result.observation());
            target += parameters.discountFactor() * table.maxValue(nextState);
        }
        double oldValue = table.get(state, action);
        double newValue = oldValue + parameters.learningRate() * (target - oldValue);
        table.update(state, action, newValue);

        if (result.done()) {
            episodes++;
            epsilon = Math.max(parameters.epsilonMin(), epsilon * parameters.epsilonDecay());
        }
    }

    private int stateOf(double[] observation) {
        return encoder.encode(observation);
    }

    /** {@code false}: nur noch die beste Aktion spielen und nichts mehr lernen. */
    public void setTraining(boolean training) {
        this.training = training;
    }

    public boolean isTraining() {
        return training;
    }

    /** Aktuelle Wahrscheinlichkeit für eine zufällige Aktion (nur im Training wirksam). */
    public double epsilon() {
        return epsilon;
    }

    /** Anzahl Episoden, aus denen gelernt wurde. */
    public int trainedEpisodes() {
        return episodes;
    }

    public QTable table() {
        return table;
    }
}
