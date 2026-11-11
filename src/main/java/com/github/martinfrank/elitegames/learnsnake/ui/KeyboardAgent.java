package com.github.martinfrank.elitegames.learnsnake.ui;

import com.github.martinfrank.elitegames.learnsnake.agent.Agent;
import com.github.martinfrank.elitegames.learnsnake.game.snake.Direction;
import com.github.martinfrank.elitegames.learnsnake.game.snake.SnakeEnvironment;
import com.github.martinfrank.elitegames.learnsnake.game.snake.SnakeGame;
import com.github.martinfrank.elitegames.learnsnake.game.snake.Turn;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Der menschliche Spieler als Agent: Er spielt über dieselbe Schnittstelle wie jeder lernende Agent.
 * <p>
 * Menschen denken in absoluten Richtungen (Pfeiltaste "hoch"), das Environment erwartet aber relative
 * Aktionen (links/rechts/geradeaus). Die Umrechnung passiert erst, wenn der Schritt ausgeführt wird –
 * dann ist die aktuelle Richtung bekannt.
 * <p>
 * Bis zu {@value #MAX_BUFFERED} Tastendrücke werden gepuffert, damit schnelle Doppel-Drehungen
 * (z.B. hoch + links für eine Kehrtwende) nicht verloren gehen.
 */
public final class KeyboardAgent implements Agent {

    static final int MAX_BUFFERED = 2;

    private final SnakeGame game;
    private final Deque<Direction> pressed = new ArrayDeque<>();

    public KeyboardAgent(SnakeGame game) {
        this.game = game;
    }

    public void press(Direction direction) {
        if (pressed.size() < MAX_BUFFERED) {
            pressed.addLast(direction);
        }
    }

    public void clear() {
        pressed.clear();
    }

    @Override
    public int chooseAction(double[] observation) {
        Direction wanted = pressed.pollFirst();
        Turn turn = wanted == null ? Turn.STRAIGHT : game.direction().turnTowards(wanted);
        return SnakeEnvironment.actionFor(turn);
    }
}
