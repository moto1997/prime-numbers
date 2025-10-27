package com.rbs.primenumbers.algorithm;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bean-injected unit tests for the segmented/parallel sieve.
 *
 * We load BOTH the segmented and simple beans so we can assert
 * that outputs are identical for the same range.
 *
 * We also set a small segment size via TestPropertySource to ensure
 * multiple segments are exercised in tests.
 */
@SpringBootTest(classes = {
        SegmentedParallelSieveAlgorithm.class,
        SimpleSieveAlgorithm.class
})
@TestPropertySource(properties = {
        // Force smaller segments so tests actually span multiple segments
        "primes.segment-size=16"
})
class SegmentedParallelSieveAlgorithmTest {

    @Autowired
    private SegmentedParallelSieveAlgorithm segmented;

    @Autowired
    private SimpleSieveAlgorithm simple;

    @Test
    void returnsEmptyForLessThan2() {
        assertEquals(List.of(), segmented.computeUpTo(0));
        assertEquals(List.of(), segmented.computeUpTo(1));
    }

    @Test
    void returnsSinglePrimeFor2() {
        assertEquals(List.of(2), segmented.computeUpTo(2));
    }

    @Test
    void matchesSimpleForSmallRange() {
        int max = 1000;
        var expected = simple.computeUpTo(max);
        var actual = segmented.computeUpTo(max);
        assertEquals(expected, actual);
    }

    @Test
    void multipleSegmentsStillMatchSimple() {
        int max = 200; // with segment-size=16 this spans multiple segments
        var expected = simple.computeUpTo(max);
        var actual = segmented.computeUpTo(max);

        assertEquals(expected, actual);
        // Ensure ascending order
        assertEquals(actual, actual.stream().sorted().toList());
    }

    @Test
    void returnsKnownSetUpTo100() {
        var expected = List.of(
                2,3,5,7,11,13,17,19,23,29,
                31,37,41,43,47,53,59,61,67,71,
                73,79,83,89,97
        );
        assertEquals(expected, segmented.computeUpTo(100));
    }

    @Test
    void handlesLargeInputWithoutErrors() {
        int max = 1_000_000;
        var result = segmented.computeUpTo(max);
        assertTrue(result.contains(999_983)); // largest prime < 1 million
        assertEquals(result, result.stream().sorted().toList()); // still sorted
    }

}
