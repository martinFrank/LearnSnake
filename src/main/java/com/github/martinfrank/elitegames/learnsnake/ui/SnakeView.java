package com.github.martinfrank.elitegames.learnsnake.ui;

import com.github.martinfrank.elitegames.learnsnake.game.snake.Direction;
import com.github.martinfrank.elitegames.learnsnake.game.snake.Position;
import com.github.martinfrank.elitegames.learnsnake.game.snake.SnakeGame;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.List;

/**
 * Zeichnet ein {@link SnakeGame}. Liest den Spielzustand nur, verändert ihn nie.
 */
public final class SnakeView extends Canvas {

    static final int CELL_SIZE = 36;

    private static final Color BACKGROUND = Color.web("#1e1e2e");
    private static final Color BACKGROUND_ALT = Color.web("#242436");
    private static final Color FOOD = Color.web("#f38ba8");
    private static final Color HEAD = Color.web("#a6e3a1");
    private static final Color TAIL = Color.web("#40a02b");
    private static final Color EYES = Color.web("#1e1e2e");
    private static final Color OVERLAY = Color.color(0, 0, 0, 0.65);

    public SnakeView(int columns, int rows) {
        super(columns * CELL_SIZE, rows * CELL_SIZE);
    }

    /**
     * @param message Text, der über dem Spielfeld eingeblendet wird (z.B. "Game Over"), oder {@code null}
     */
    public void draw(SnakeGame game, String message) {
        GraphicsContext gc = getGraphicsContext2D();
        drawBoard(gc, game);
        game.food().ifPresent(food -> drawFood(gc, food));
        drawSnake(gc, game);
        if (message != null) {
            drawMessage(gc, message);
        }
    }

    private void drawBoard(GraphicsContext gc, SnakeGame game) {
        for (int y = 0; y < game.height(); y++) {
            for (int x = 0; x < game.width(); x++) {
                boolean alternate = (x + y) % 2 == 1;
                gc.setFill(alternate ? BACKGROUND_ALT : BACKGROUND);
                gc.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
    }

    private void drawFood(GraphicsContext gc, Position food) {
        double padding = CELL_SIZE * 0.2;
        gc.setFill(FOOD);
        gc.fillOval(food.x() * CELL_SIZE + padding, food.y() * CELL_SIZE + padding,
                CELL_SIZE - 2 * padding, CELL_SIZE - 2 * padding);
    }

    private void drawSnake(GraphicsContext gc, SnakeGame game) {
        List<Position> body = game.body();
        // Vom Schwanz zum Kopf zeichnen, damit der Kopf obenauf liegt
        for (int i = body.size() - 1; i >= 0; i--) {
            double fromHeadToTail = body.size() == 1 ? 0 : i / (double) (body.size() - 1);
            gc.setFill(HEAD.interpolate(TAIL, fromHeadToTail));
            Position segment = body.get(i);
            gc.fillRoundRect(segment.x() * CELL_SIZE + 2, segment.y() * CELL_SIZE + 2,
                    CELL_SIZE - 4, CELL_SIZE - 4, 12, 12);
        }
        drawEyes(gc, game.head(), game.direction());
    }

    private void drawEyes(GraphicsContext gc, Position head, Direction direction) {
        double centerX = head.x() * CELL_SIZE + CELL_SIZE / 2.0;
        double centerY = head.y() * CELL_SIZE + CELL_SIZE / 2.0;
        double forward = CELL_SIZE * 0.18;
        double sideways = CELL_SIZE * 0.18;
        double radius = CELL_SIZE * 0.08;

        // Augen sitzen nach vorne versetzt, links und rechts der Blickrichtung
        double eyeX = centerX + direction.dx() * forward;
        double eyeY = centerY + direction.dy() * forward;
        double sideX = -direction.dy() * sideways;
        double sideY = direction.dx() * sideways;

        gc.setFill(EYES);
        gc.fillOval(eyeX + sideX - radius, eyeY + sideY - radius, 2 * radius, 2 * radius);
        gc.fillOval(eyeX - sideX - radius, eyeY - sideY - radius, 2 * radius, 2 * radius);
    }

    private void drawMessage(GraphicsContext gc, String message) {
        gc.setFill(OVERLAY);
        gc.fillRect(0, 0, getWidth(), getHeight());

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System", FontWeight.BOLD, 18));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        String[] lines = message.split("\n");
        double lineHeight = 28;
        double firstLineY = getHeight() / 2 - (lines.length - 1) * lineHeight / 2;
        for (int i = 0; i < lines.length; i++) {
            gc.fillText(lines[i], getWidth() / 2, firstLineY + i * lineHeight);
        }
    }
}
