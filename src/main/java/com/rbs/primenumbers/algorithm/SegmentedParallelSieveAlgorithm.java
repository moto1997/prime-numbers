package com.rbs.primenumbers.algorithm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Segmented Sieve with parallel segments.
 * Good for large max values. Configurable segment size.
 */
@Slf4j
@Component("segmented")
@RequiredArgsConstructor
public class SegmentedParallelSieveAlgorithm implements PrimeAlgorithm {

    @Value("${primes.segment-size:1000000}")
    private int segmentSize;

    @Override
    public String name() { return "segmented"; }

    @Override
    public List<Integer> computeUpTo(int max) {
        if (max < 2) return List.of();

        int limit = (int) Math.sqrt(max);
        List<Integer> basePrimes = simpleSieve(limit);

        int segSize = Math.max(10_000, segmentSize);
        int start = 2;
        int totalNumbers = max - start + 1;
        int segments = Math.max(1, (totalNumbers + segSize - 1) / segSize);

        log.debug("Segmented sieve up to {}, segments={}, segmentSize={}", max, segments, segSize);

        return IntStream.range(0, segments)
                .parallel()
                .mapToObj(segIndex -> {
                    int low = start + segIndex * segSize;
                    int high = Math.min(low + segSize - 1, max);
                    return sieveSegment(low, high, basePrimes);
                })
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    // --- helpers ---

    private List<Integer> sieveSegment(int low, int high, List<Integer> basePrimes) {
        int len = high - low + 1;
        BitSet composite = new BitSet(len);

        for (int p : basePrimes) {
            long p2 = (long) p * p;
            long first = Math.max(p2, ((long) Math.ceil((double) low / p)) * p);
            for (long m = first; m <= high; m += p) {
                composite.set((int) (m - low));
            }
        }

        if (low == 0) { composite.set(0); composite.set(1); }
        if (low == 1) { composite.set(0); }

        List<Integer> primes = new ArrayList<>();
        for (int i = composite.nextClearBit(0); i >= 0 && i < len; i = composite.nextClearBit(i + 1)) {
            primes.add(low + i);
        }
        return primes;
    }

    private List<Integer> simpleSieve(int n) {
        if (n < 2) return List.of();
        BitSet composite = new BitSet(n + 1);
        composite.set(0); composite.set(1);

        int limit = (int) Math.sqrt(n);
        for (int p = 2; p <= limit; p = composite.nextClearBit(p + 1)) {
            if (!composite.get(p)) {
                for (long m = (long) p * p; m <= n; m += p) {
                    composite.set((int) m);
                }
            }
        }

        List<Integer> primes = new ArrayList<>();
        for (int i = composite.nextClearBit(2); i >= 0 && i <= n; i = composite.nextClearBit(i + 1)) {
            primes.add(i);
            if (i == Integer.MAX_VALUE) break;
        }
        return primes;
    }
}
