package com.github.martinfrank.elitegames.learnsnake.environment;

/**
 * {@link StateEncoder} für Observations, die nur aus Ja/Nein-Werten (0 oder 1) bestehen.
 * <p>
 * Jedes Merkmal wird zu einem Bit: Merkmal 0 ist das niederwertigste Bit, Merkmal 1 das nächste usw.
 * Bei 11 Merkmalen entstehen so Zahlen von 0 bis 2047 – genau die Zeilennummern der Q-Table.
 * <pre>
 * Observation [1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0]
 *              ^        ^              ^
 *              Bit 0    Bit 3          Bit 8     →  1 + 8 + 256 = 265
 * </pre>
 * Weil die Umrechnung umkehrbar ist, gibt es zusätzlich {@link #decode(int)} – praktisch, um eine
 * Zeile der Q-Table wieder in Worten anzuzeigen.
 */
public final class BinaryStateEncoder implements StateEncoder {

    /** Mehr Merkmale passen nicht sinnvoll in eine Tabelle (2^30 Zeilen wären schon über eine Milliarde). */
    public static final int MAX_FEATURES = 30;

    private final int featureCount;

    public BinaryStateEncoder(int featureCount) {
        if (featureCount < 1 || featureCount > MAX_FEATURES) {
            throw new IllegalArgumentException("featureCount muss zwischen 1 und " + MAX_FEATURES + " liegen");
        }
        this.featureCount = featureCount;
    }

    public int featureCount() {
        return featureCount;
    }

    /** 2^featureCount. */
    @Override
    public int stateCount() {
        return 1 << featureCount;
    }

    @Override
    public int encode(double[] observation) {
        if (observation.length != featureCount) {
            throw new IllegalArgumentException(
                    "Observation muss " + featureCount + " Werte haben, hat aber " + observation.length);
        }
        int state = 0;
        for (int i = 0; i < featureCount; i++) {
            double value = observation[i];
            if (value == 1.0) {
                state |= 1 << i;
            } else if (value != 0.0) {
                throw new IllegalArgumentException(
                        "Merkmal " + i + " ist " + value + " – erlaubt sind nur 0 und 1");
            }
        }
        return state;
    }

    public double[] decode(int state) {
        if (state < 0 || state >= stateCount()) {
            throw new IllegalArgumentException("Zustand " + state + " passt nicht zu " + featureCount + " Merkmalen");
        }
        double[] observation = new double[featureCount];
        for (int i = 0; i < featureCount; i++) {
            observation[i] = (state >> i & 1) == 1 ? 1.0 : 0.0;
        }
        return observation;
    }
}
