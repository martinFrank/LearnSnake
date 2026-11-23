package com.github.martinfrank.elitegames.learnsnake.agent;

import com.github.martinfrank.elitegames.learnsnake.game.snake.Direction;
import com.github.martinfrank.elitegames.learnsnake.game.snake.SnakeEnvironment;
import com.github.martinfrank.elitegames.learnsnake.game.snake.Turn;

import java.util.List;

/**
 * Handgeschriebene Strategie für Snake: aufs Essen zugehen, Gefahren vermeiden.
 * <p>
 * Der Agent sieht bewusst nur die 11 Merkmale aus {@link SnakeEnvironment} – genau das, was später
 * auch der Q-Learning-Agent sieht. Dadurch ist der Vergleich fair, und seine Entscheidungen
 * eignen sich als Trainingsdaten für Imitation Learning.
 * <p>
 * Regeln, in dieser Reihenfolge:
 * <ol>
 *     <li>Nur Drehungen ohne Gefahr kommen in Frage.</li>
 *     <li>Davon wird eine gewählt, die in Richtung Essen führt.</li>
 *     <li>Führt keine sichere Drehung zum Essen, wird die erste sichere genommen.</li>
 *     <li>Ist keine Drehung sicher, ist das Spiel verloren – dann geradeaus.</li>
 * </ol>
 * Bei Gleichstand gewinnt die Reihenfolge geradeaus, links, rechts.
 * <p>
 * Bekannte Schwäche: Der Agent schaut nur einen Schritt voraus und schließt sich deshalb
 * irgendwann mit dem eigenen Körper ein.
 */
public final class RuleBasedSnakeAgent implements Agent {

    private static final List<Turn> PREFERENCE = List.of(Turn.STRAIGHT, Turn.LEFT, Turn.RIGHT);

    @Override
    public int chooseAction(double[] observation) {
        if (observation.length != SnakeEnvironment.OBSERVATION_SIZE) {
            throw new IllegalArgumentException("Observation muss " + SnakeEnvironment.OBSERVATION_SIZE
                    + " Werte haben, hat aber " + observation.length);
        }
        Direction direction = currentDirection(observation);

        List<Turn> safeTurns = PREFERENCE.stream()
                .filter(turn -> !isDangerous(observation, turn))
                .toList();

        Turn choice = safeTurns.stream()
                .filter(turn -> isFoodIn(observation, direction.apply(turn)))
                .findFirst()
                .orElse(safeTurns.isEmpty() ? Turn.STRAIGHT : safeTurns.getFirst());

        return SnakeEnvironment.actionFor(choice);
    }

    private static Direction currentDirection(double[] observation) {
        if (isSet(observation, SnakeEnvironment.DIRECTION_UP)) {
            return Direction.UP;
        }
        if (isSet(observation, SnakeEnvironment.DIRECTION_RIGHT)) {
            return Direction.RIGHT;
        }
        if (isSet(observation, SnakeEnvironment.DIRECTION_DOWN)) {
            return Direction.DOWN;
        }
        if (isSet(observation, SnakeEnvironment.DIRECTION_LEFT)) {
            return Direction.LEFT;
        }
        throw new IllegalArgumentException("Observation enthält keine Bewegungsrichtung");
    }

    private static boolean isDangerous(double[] observation, Turn turn) {
        int index = switch (turn) {
            case STRAIGHT -> SnakeEnvironment.DANGER_STRAIGHT;
            case LEFT -> SnakeEnvironment.DANGER_LEFT;
            case RIGHT -> SnakeEnvironment.DANGER_RIGHT;
        };
        return isSet(observation, index);
    }

    private static boolean isFoodIn(double[] observation, Direction direction) {
        int index = switch (direction) {
            case UP -> SnakeEnvironment.FOOD_UP;
            case RIGHT -> SnakeEnvironment.FOOD_RIGHT;
            case DOWN -> SnakeEnvironment.FOOD_DOWN;
            case LEFT -> SnakeEnvironment.FOOD_LEFT;
        };
        return isSet(observation, index);
    }

    private static boolean isSet(double[] observation, int index) {
        return observation[index] > 0.5;
    }
}
