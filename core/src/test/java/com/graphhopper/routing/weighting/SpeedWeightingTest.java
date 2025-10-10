package com.graphhopper.routing.weighting;

import com.github.javafaker.Faker;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.storage.TurnCostStorage;
import com.graphhopper.util.EdgeIteratorState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for SpeedWeighting.
 * Chaque test est documenté (intention, données, oracle attendu).
 */
public class SpeedWeightingTest {

    private DecimalEncodedValue speedEnc;
    private EdgeIteratorState edge;

    @BeforeEach
    void setUp() {
        speedEnc = mock(DecimalEncodedValue.class);
        edge = mock(EdgeIteratorState.class);
    }

    @Test
    void testCalcEdgeWeightNormal() {
        when(speedEnc.getMaxStorableDecimal()).thenReturn(100.0);
        when(edge.getDistance()).thenReturn(1000.0);
        when(edge.get(speedEnc)).thenReturn(50.0);

        SpeedWeighting sw = new SpeedWeighting(speedEnc);
        double result = sw.calcEdgeWeight(edge, false);

        assertEquals(20.0, result); // 1000 / 50
    }

    @Test
    void testCalcEdgeWeightZeroSpeed() {
        when(edge.get(speedEnc)).thenReturn(0.0);

        SpeedWeighting sw = new SpeedWeighting(speedEnc);
        double result = sw.calcEdgeWeight(edge, false);

        assertEquals(Double.POSITIVE_INFINITY, result);
    }

    @Test
    void testCalcEdgeWeightReverse() {
        when(edge.getReverse(speedEnc)).thenReturn(25.0);
        when(edge.getDistance()).thenReturn(500.0);

        SpeedWeighting sw = new SpeedWeighting(speedEnc);
        double result = sw.calcEdgeWeight(edge, true);

        assertEquals(20.0, result); // 500 / 25
    }

    @Test
    void testCalcEdgeMillis() {
        when(edge.get(speedEnc)).thenReturn(10.0);
        when(edge.getDistance()).thenReturn(100.0);

        SpeedWeighting sw = new SpeedWeighting(speedEnc);
        long millis = sw.calcEdgeMillis(edge, false);

        assertEquals(10000L, millis); // (100/10)*1000
    }

    @Test
    void testCalcMinWeightPerDistance() {
        when(speedEnc.getMaxStorableDecimal()).thenReturn(120.0);

        SpeedWeighting sw = new SpeedWeighting(speedEnc);
        assertEquals(1.0 / 120.0, sw.calcMinWeightPerDistance());
    }

    @Test
    void testGetName() {
        SpeedWeighting sw = new SpeedWeighting(speedEnc);
        assertEquals("speed", sw.getName());
    }

    @Test
    void testHasTurnCosts() {
        TurnCostStorage storage = mock(TurnCostStorage.class);
        DecimalEncodedValue turnEnc = mock(DecimalEncodedValue.class);

        SpeedWeighting sw = new SpeedWeighting(speedEnc, turnEnc, storage, 5.0);

        assertTrue(sw.hasTurnCosts());
    }

    /**
     *  Nouveau test 8 : Utilisation de Faker pour générer des données de test déterministes
     */
    @Test
    void testCalcEdgeWeightWithFakerDeterministic() {
        // Seed fixe pour reproductibilité
        Faker faker = new Faker(new Random(12345));

        // Valeurs réalistes mais déterministes
        double distance = faker.number().numberBetween(100, 2000);
        double speed = faker.number().numberBetween(5, 120);

        when(edge.getDistance()).thenReturn(distance);
        when(edge.get(speedEnc)).thenReturn(speed);

        SpeedWeighting sw = new SpeedWeighting(speedEnc);

        double expected = distance / speed;
        double actual = sw.calcEdgeWeight(edge, false);

        assertEquals(expected, actual, 1e-9,
                "calcEdgeWeight must be distance/speed with Faker-generated values");
    }
}
