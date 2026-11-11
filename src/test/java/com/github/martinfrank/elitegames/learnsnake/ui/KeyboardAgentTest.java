package com.github.martinfrank.elitegames.learnsnake.ui;

import com.github.martinfrank.elitegames.learnsnake.game.snake.Direction;
import com.github.martinfrank.elitegames.learnsnake.game.snake.SnakeEnvironment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.martinfrank.elitegames.learnsnake.game.snake.SnakeEnvironment.ACTION_LEFT;
import static com.github.martinfrank.elitegames.learnsnake.game.snake.SnakeEnvironment.ACTION_RIGHT;
import static com.github.martinfrank.elitegames.learnsnake.game.snake.SnakeEnvironment.ACTION_STRAIGHT;
import static org.junit.jupiter.api.Assertions.assertEquals;

class KeyboardAgentTest {

    private SnakeEnvironment env;
    private KeyboardAgent agent;

    @BeforeEach
    void setUp() {
        env = new SnakeEnvironment(10, 10, 1L);
        env.reset(); // Schlange blickt nach rechts
        agent = new KeyboardAgent(env.game());
    }

    @Test
    void noKeyMeansStraight() {
        assertEquals(ACTION_STRAIGHT, agent.chooseAction(null));
    }

    @Test
    void absoluteKeysBecomeRelativeTurns() {
        agent.press(Direction.UP);
        assertEquals(ACTION_LEFT, agent.chooseAction(null));

        agent.press(Direction.DOWN);
        assertEquals(ACTION_RIGHT, agent.chooseAction(null));
    }

    @Test
    void oppositeDirectionIsIgnored() {
        agent.press(Direction.LEFT);
        assertEquals(ACTION_STRAIGHT, agent.chooseAction(null));
    }

    @Test
    void bufferedKeysAllowQuickUTurn() {
        // Blick nach rechts: hoch + links = Kehrtwende in zwei Schritten
        agent.press(Direction.UP);
        agent.press(Direction.LEFT);

        int first = agent.chooseAction(null);
        assertEquals(ACTION_LEFT, first);
        env.step(first); // jetzt Blick nach oben

        assertEquals(ACTION_LEFT, agent.chooseAction(null));
    }

    @Test
    void bufferIsLimited() {
        for (int i = 0; i < 5; i++) {
            agent.press(Direction.UP);
        }
        for (int i = 0; i < KeyboardAgent.MAX_BUFFERED; i++) {
            agent.chooseAction(null);
        }
        assertEquals(ACTION_STRAIGHT, agent.chooseAction(null), "überzählige Tastendrücke werden verworfen");
    }

    @Test
    void clearDropsBufferedKeys() {
        agent.press(Direction.UP);
        agent.clear();
        assertEquals(ACTION_STRAIGHT, agent.chooseAction(null));
    }
}
