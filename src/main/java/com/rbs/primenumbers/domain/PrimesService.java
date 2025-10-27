package com.rbs.primenumbers.domain;

import com.rbs.primenumbers.algorithm.PrimeAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrimesService {

    // Inject all algorithm beans: key = bean name ("simple", "segmented"), value = instance
    private final Map<String, PrimeAlgorithm> algorithms;

    // Limits and defaults from config
    @Value("${primes.max-allowed:1000000}")
    private int maxAllowed;

    @Value("${primes.algorithm.default:simple}")
    private String defaultAlgorithmName;

    public static class UpperBoundExceededException extends RuntimeException {
        public UpperBoundExceededException(String message) { super(message); }
    }

    public void guardUpperBound(int max) {
        if (max > maxAllowed) {
            throw new UpperBoundExceededException("max must be ≤ " + maxAllowed);
        }
        if (max < 0) {
            throw new IllegalArgumentException("max must be ≥ 0");
        }
    }


    /** Compute using a specific algorithm name or fallback to default. */
    public List<Integer> compute(int max, String algorithmName) {
        String key = (algorithmName == null || algorithmName.isBlank())
                ? defaultAlgorithmName
                : algorithmName;

        PrimeAlgorithm algo = algorithms.get(key);
        if (algo == null) {
            throw new IllegalArgumentException("Unknown algorithm: " + key + " (available=" + algorithms.keySet() + ")");
        }

        log.debug("Using algorithm='{}' for max={}", key, max);
        return algo.computeUpTo(max);
    }




}
