package com.rbs.primenumbers.domain;

import com.rbs.primenumbers.algorithm.SimpleSieveAlgorithm;
import com.rbs.primenumbers.algorithm.SegmentedParallelSieveAlgorithm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bean-injected tests for PrimesService.
 * Loads real algorithm beans and config via Spring Boot.
 */
@SpringBootTest(classes = {
        PrimesService.class,
        SimpleSieveAlgorithm.class,
        SegmentedParallelSieveAlgorithm.class
})
@TestPropertySource(properties = {
        "primes.max-allowed=1000000",
        "primes.segment-size=32",
        "primes.algorithm.default=simple"
})
class PrimesServiceTest {

    @Autowired
    private PrimesService service;

    // -------- guardUpperBound --------

    @Test
    void guardUpperBound_allowsAtLimit() {
        assertDoesNotThrow(() -> service.guardUpperBound(1_000_000));
    }

    @Test
    void guardUpperBound_rejectsAboveLimit() {
        var ex = assertThrows(PrimesService.UpperBoundExceededException.class,
                () -> service.guardUpperBound(1_000_001));
        assertTrue(ex.getMessage().contains("must be ≤"));
    }

    @Test
    void guardUpperBound_rejectsNegative() {
        var ex = assertThrows(IllegalArgumentException.class, () -> service.guardUpperBound(-1));
        assertTrue(ex.getMessage().contains("≥ 0"));
    }

    // -------- compute (algorithm selection & correctness) --------

    @Test
    void compute_usesDefaultAlgorithm_whenNoneProvided() {
        var primes = service.compute(10, null);
        assertEquals(List.of(2, 3, 5, 7), primes);
    }

    @Test
    void compute_blankAlgorithm_usesDefault() {
        var expected = service.compute(30, null);
        var actual = service.compute(30, "   ");
        assertEquals(expected, actual);
    }

    @Test
    void compute_withExplicitSimpleAlgorithm_matchesDefault() {
        var expected = service.compute(10, null);
        var actual = service.compute(10, "simple");
        assertEquals(expected, actual);
    }

    @Test
    void compute_withSegmentedAlgorithm_matchesSimple() {
        var simple = service.compute(200, "simple");
        var segmented = service.compute(200, "segmented");
        assertEquals(simple, segmented, "Segmented output should match simple output");
    }

    @Test
    void compute_unknownAlgorithm_throwsHelpfulError() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> service.compute(10, "does-not-exist"));
        assertTrue(ex.getMessage().contains("Unknown algorithm"));
        assertTrue(ex.getMessage().contains("simple"));
        assertTrue(ex.getMessage().contains("segmented"));
    }



}
