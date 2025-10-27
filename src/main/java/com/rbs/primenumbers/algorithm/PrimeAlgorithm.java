package com.rbs.primenumbers.algorithm;

import java.util.List;

/** Strategy interface for prime-number algorithms. */
public interface PrimeAlgorithm {
    /**
     * Compute all primes <= max.
     */
    List<Integer> computeUpTo(int max);

    /**
     * Machine-friendly name used for selection (e.g., "simple", "segmented").
     */
    String name();
}
