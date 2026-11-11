package com.github.martinfrank.elitegames.learnsnake.game.snake;

import com.github.martinfrank.elitegames.learnsnake.environment.Environment;
import com.github.martinfrank.elitegames.learnsnake.environment.StepResult;

/**
 * Macht {@link SnakeGame} für Agenten spielbar.
 *
 * <h2>Aktionen</h2>
 * {@link #ACTION_STRAIGHT}, {@link #ACTION_LEFT}, {@link #ACTION_RIGHT}.
 *
 * <h2>Observation</h2>
 * 11 Werte, jeweils 0 oder 1 – die Indizes sind als Konstanten definiert:
 * <ul>
 *     <li>{@code DANGER_*}: Diese Aktion würde im nächsten Schritt zum Tod führen</li>
 *     <li>{@code DIRECTION_*}: aktuelle Bewegungsrichtung (genau einer ist 1)</li>
 *     <li>{@code FOOD_*}: in welcher Richtung das Essen vom Kopf aus liegt</li>
 * </ul>
 *
 * <h2>Episodenende</h2>
 * {@code terminated} bei Kollision oder vollem Feld, {@code truncated} wenn die Schlange
 * {@value #STARVATION_FACTOR} × Länge Schritte lang kein Essen gefunden hat.
 */
public final class SnakeEnvironment implements Environment {

    public static final int ACTION_STRAIGHT = 0;
    public static final int ACTION_LEFT = 1;
    public static final int ACTION_RIGHT = 2;
    public static final int ACTION_COUNT = 3;

    public static final int DANGER_STRAIGHT = 0;
    public static final int DANGER_LEFT = 1;
    public static final int DANGER_RIGHT = 2;
    public static final int DIRECTION_UP = 3;
    public static final int DIRECTION_RIGHT = 4;
    public static final int DIRECTION_DOWN = 5;
    public static final int DIRECTION_LEFT = 6;
    public static final int FOOD_UP = 7;
    public static final int FOOD_RIGHT = 8;
    public static final int FOOD_DOWN = 9;
    public static final int FOOD_LEFT = 10;
    public static final int OBSERVATION_SIZE = 11;

    public static final int STARVATION_FACTOR = 100;

    private final SnakeGame game;
    private final SnakeRewards rewards;
    private boolean done;

    public SnakeEnvironment(int width, int height, long seed) {
        this(width, height, seed, SnakeRewards.DEFAULT);
    }

    public SnakeEnvironment(int width, int height, long seed, SnakeRewards rewards) {
        this.game = new SnakeGame(width, height, seed);
        this.rewards = rewards;
    }

    @Override
    public double[] reset() {
        game.reset();
        done = false;
        return observe();
    }

    @Override
    public StepResult step(int action) {
        if (done) {
            throw new IllegalStateException("Episode ist beendet, zuerst reset() aufrufen");
        }

        StepOutcome outcome = game.step(toTurn(action));
        double reward = switch (outcome) {
            case ATE_FOOD, BOARD_FULL -> rewards.food();
            case HIT_WALL, HIT_SELF -> rewards.death();
            case MOVED -> rewards.step();
        };
        boolean terminated = outcome.isTerminal();
        boolean truncated = !terminated && game.stepsSinceLastFood() >= STARVATION_FACTOR * game.length();
        done = terminated || truncated;

        return new StepResult(observe(), reward, terminated, truncated);
    }

    /** Gegenstück zu {@link #toTurn(int)}: welche Aktionsnummer gehört zu dieser Drehung? */
    public static int actionFor(Turn turn) {
        return switch (turn) {
            case STRAIGHT -> ACTION_STRAIGHT;
            case LEFT -> ACTION_LEFT;
            case RIGHT -> ACTION_RIGHT;
        };
    }

    private static Turn toTurn(int action) {
        return switch (action) {
            case ACTION_STRAIGHT -> Turn.STRAIGHT;
            case ACTION_LEFT -> Turn.LEFT;
            case ACTION_RIGHT -> Turn.RIGHT;
            default -> throw new IllegalArgumentException("Ungültige Aktion: " + action);
        };
    }

    private double[] observe() {
        double[] obs = new double[OBSERVATION_SIZE];

        obs[DANGER_STRAIGHT] = game.wouldCollide(Turn.STRAIGHT) ? 1.0 : 0.0;
        obs[DANGER_LEFT] = game.wouldCollide(Turn.LEFT) ? 1.0 : 0.0;
        obs[DANGER_RIGHT] = game.wouldCollide(Turn.RIGHT) ? 1.0 : 0.0;

        Direction direction = game.direction();
        obs[DIRECTION_UP] = direction == Direction.UP ? 1.0 : 0.0;
        obs[DIRECTION_RIGHT] = direction == Direction.RIGHT ? 1.0 : 0.0;
        obs[DIRECTION_DOWN] = direction == Direction.DOWN ? 1.0 : 0.0;
        obs[DIRECTION_LEFT] = direction == Direction.LEFT ? 1.0 : 0.0;

        game.food().ifPresent(food -> {
            Position head = game.head();
            obs[FOOD_UP] = food.y() < head.y() ? 1.0 : 0.0;
            obs[FOOD_RIGHT] = food.x() > head.x() ? 1.0 : 0.0;
            obs[FOOD_DOWN] = food.y() > head.y() ? 1.0 : 0.0;
            obs[FOOD_LEFT] = food.x() < head.x() ? 1.0 : 0.0;
        });
        return obs;
    }

    @Override
    public int observationSize() {
        return OBSERVATION_SIZE;
    }

    @Override
    public int actionCount() {
        return ACTION_COUNT;
    }

    @Override
    public int score() {
        return game.score();
    }

    /** Zugriff auf das Spiel, z.B. zum Zeichnen in der Oberfläche. Nicht direkt verändern! */
    public SnakeGame game() {
        return game;
    }
}
