package com.rbs.primenumbers.algorithm;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bean-injected unit tests for the simple (single-thread) sieve.
 * Loads only the SimpleSieveAlgorithm bean.
 */
@SpringBootTest(classes = { SimpleSieveAlgorithm.class })
class SimpleSieveAlgorithmTest {

    @Autowired
    private SimpleSieveAlgorithm algo;

    @Test
    void returnsEmptyForLessThan2() {
        assertEquals(List.of(), algo.computeUpTo(0));
        assertEquals(List.of(), algo.computeUpTo(1));
    }

    @Test
    void returnsSinglePrimeFor2() {
        assertEquals(List.of(2), algo.computeUpTo(2));
    }

    @Test
    void returnsPrimesUpTo10() {
        assertEquals(List.of(2,3,5,7), algo.computeUpTo(10));
    }

    @Test
    void returnsKnownSetUpTo100() {
        var expected = List.of(
                2,3,5,7,11,13,17,19,23,29,
                31,37,41,43,47,53,59,61,67,71,
                73,79,83,89,97
        );
        assertEquals(expected, algo.computeUpTo(100));
    }
}

