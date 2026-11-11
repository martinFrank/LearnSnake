package com.github.martinfrank.elitegames.learnsnake.game.snake;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

/**
 * Reine Snake-Spiellogik – ohne Oberfläche, ohne Timer, ohne Threads.
 * <p>
 * Ein Aufruf von {@link #step(Turn)} ist genau ein Spielschritt. Wer das Spiel antreibt
 * (Mensch über die UI, Agent im Training), entscheidet selbst über das Tempo.
 * <p>
 * Der Zufallsgenerator wird mit einem Seed initialisiert, damit Spielverläufe reproduzierbar sind.
 */
public final class SnakeGame {

    public static final int INITIAL_LENGTH = 3;
    public static final int MIN_SIZE = 5;

    private final int width;
    private final int height;
    private final Random random;

    /** Körper der Schlange, erstes Element ist der Kopf. */
    private final Deque<Position> body = new ArrayDeque<>();
    /** Dieselben Zellen wie {@link #body}, für schnelle Kollisionsabfragen. */
    private final Set<Position> occupied = new HashSet<>();

    private Direction direction;
    private Position food;
    private int score;
    private int steps;
    private int stepsSinceLastFood;
    private boolean gameOver;

    public SnakeGame(int width, int height, long seed) {
        if (width < MIN_SIZE || height < MIN_SIZE) {
            throw new IllegalArgumentException(
                    "Spielfeld muss mindestens " + MIN_SIZE + "x" + MIN_SIZE + " sein, war " + width + "x" + height);
        }
        this.width = width;
        this.height = height;
        this.random = new Random(seed);
        reset();
    }

    /**
     * Startet ein neues Spiel: Schlange mit Länge {@value #INITIAL_LENGTH} in der Mitte, Blickrichtung rechts.
     * Der Zufallsgenerator wird dabei nicht zurückgesetzt – aufeinanderfolgende Spiele sind also
     * unterschiedlich, die gesamte Folge ist aber durch den Seed festgelegt.
     */
    public void reset() {
        body.clear();
        occupied.clear();
        direction = Direction.RIGHT;
        int centerX = width / 2;
        int centerY = height / 2;
        for (int i = 0; i < INITIAL_LENGTH; i++) {
            Position segment = new Position(centerX - i, centerY);
            body.addLast(segment);
            occupied.add(segment);
        }
        score = 0;
        steps = 0;
        stepsSinceLastFood = 0;
        gameOver = false;
        placeRandomFood();
    }

    /**
     * Führt einen Spielschritt aus.
     *
     * @throws IllegalStateException wenn das Spiel bereits vorbei ist
     */
    public StepOutcome step(Turn turn) {
        if (gameOver) {
            throw new IllegalStateException("Spiel ist vorbei, zuerst reset() aufrufen");
        }
        direction = direction.apply(turn);
        Position newHead = head().move(direction);
        steps++;
        stepsSinceLastFood++;

        if (!isInside(newHead)) {
            gameOver = true;
            return StepOutcome.HIT_WALL;
        }

        if (hitsBody(newHead)) {
            gameOver = true;
            return StepOutcome.HIT_SELF;
        }

        boolean eats = newHead.equals(food);
        if (!eats) {
            //wenn essen=true, dann wird die Schlange um eins länger, deshalb wird in diesem Fall das letzte element NICHT entfernt
            //wenn essen=false, dann wird das letzte element entfernt, siehe hier:
            occupied.remove(body.removeLast());
        }
        body.addFirst(newHead);
        occupied.add(newHead);

        if (!eats) {
            return StepOutcome.MOVED;
        }
        score++;
        stepsSinceLastFood = 0;
        if (!placeRandomFood()) {
            gameOver = true;
            return StepOutcome.BOARD_FULL;
        }
        return StepOutcome.ATE_FOOD;
    }

    /**
     * Vorschau: Würde die Schlange mit dieser Aktion sterben? Verändert den Spielzustand nicht.
     * Verwendet exakt dieselbe Regel wie {@link #step(Turn)}.
     */
    public boolean wouldCollide(Turn turn) {
        Position next = head().move(direction.apply(turn));
        return !isInside(next) || hitsBody(next);
    }

    private boolean hitsBody(Position next) {
        // Das Schwanzende bewegt sich im selben Schritt weiter – außer die Schlange wächst gerade.
        // In die Zelle des Schwanzendes zu laufen ist daher erlaubt, wenn nicht gefressen wird.
        boolean eats = next.equals(food);
        boolean movesIntoVacatedTail = !eats && next.equals(body.peekLast());
        return occupied.contains(next) && !movesIntoVacatedTail;
    }

    /** @return {@code false}, wenn keine freie Zelle mehr existiert */
    private boolean placeRandomFood() {
        List<Position> free = new ArrayList<>(width * height - body.size());
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Position p = new Position(x, y);
                if (!occupied.contains(p)) {
                    free.add(p);
                }
            }
        }
        if (free.isEmpty()) {
            food = null;
            return false;
        }
        food = free.get(random.nextInt(free.size()));
        return true;
    }

    /** Nur für Tests: Essen gezielt platzieren. */
    void placeFoodAt(Position position) {
        if (!isInside(position) || occupied.contains(position)) {
            throw new IllegalArgumentException("Essen kann nicht auf " + position + " platziert werden");
        }
        food = position;
    }

    public boolean isInside(Position p) {
        return p.x() >= 0 && p.x() < width && p.y() >= 0 && p.y() < height;
    }

    public boolean isOccupied(Position p) {
        return occupied.contains(p);
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public Position head() {
        return body.peekFirst();
    }

    /** @return unveränderliche Kopie des Körpers, Kopf zuerst */
    public List<Position> body() {
        return List.copyOf(body);
    }

    public int length() {
        return body.size();
    }

    public Direction direction() {
        return direction;
    }

    /** @return Position des Essens, leer wenn das Feld voll ist */
    public Optional<Position> food() {
        return Optional.ofNullable(food);
    }

    public int score() {
        return score;
    }

    public int steps() {
        return steps;
    }

    public int stepsSinceLastFood() {
        return stepsSinceLastFood;
    }

    public boolean isGameOver() {
        return gameOver;
    }
}
