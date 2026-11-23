package com.github.martinfrank.elitegames.learnsnake.environment;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BinaryStateEncoderTest {

    @Test
    void eachFeatureIsOneBit() {
        BinaryStateEncoder encoder = new BinaryStateEncoder(3);
        assertEquals(0, encoder.encode(new double[]{0, 0, 0}));
        assertEquals(1, encoder.encode(new double[]{1, 0, 0}));
        assertEquals(2, encoder.encode(new double[]{0, 1, 0}));
        assertEquals(4, encoder.encode(new double[]{0, 0, 1}));
        assertEquals(7, encoder.encode(new double[]{1, 1, 1}));
    }

    @Test
    void exampleFromDocumentation() {
        double[] observation = {1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0};
        assertEquals(1 + 8 + 256, new BinaryStateEncoder(11).encode(observation));
    }

    @Test
    void decodeReversesEncode() {
        BinaryStateEncoder encoder = new BinaryStateEncoder(11);
        for (int state = 0; state < encoder.stateCount(); state++) {
            assertEquals(state, encoder.encode(encoder.decode(state)));
        }
        assertArrayEquals(new double[]{1, 0, 1, 1}, new BinaryStateEncoder(4).decode(13));
    }

    @Test
    void stateCountIsTwoToThePowerOfFeatures() {
        assertEquals(2048, new BinaryStateEncoder(11).stateCount());
        assertEquals(2, new BinaryStateEncoder(1).stateCount());
    }

    @Test
    void rejectsNonBinaryValues() {
        assertThrows(IllegalArgumentException.class, () -> new BinaryStateEncoder(2).encode(new double[]{0, 0.5}));
    }

    @Test
    void rejectsObservationOfWrongLength() {
        assertThrows(IllegalArgumentException.class, () -> new BinaryStateEncoder(2).encode(new double[]{0, 0, 1}));
    }

    @Test
    void rejectsInvalidFeatureCount() {
        assertThrows(IllegalArgumentException.class, () -> new BinaryStateEncoder(0));
        assertThrows(IllegalArgumentException.class, () -> new BinaryStateEncoder(31));
    }

    @Test
    void rejectsStateOutOfRange() {
        BinaryStateEncoder encoder = new BinaryStateEncoder(4);
        assertThrows(IllegalArgumentException.class, () -> encoder.decode(16));
        assertThrows(IllegalArgumentException.class, () -> encoder.decode(-1));
    }
}
