package com.rbs.primenumbers.algorithm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

@Slf4j
@Component("simple") // bean name used for selection
public class SimpleSieveAlgorithm implements PrimeAlgorithm {

    @Override
    public String name() { return "simple"; }

    @Override
    public List<Integer> computeUpTo(int max) {
        if (max < 2) return List.of();

        BitSet composite = new BitSet(max + 1);
        composite.set(0); composite.set(1);

        int limit = (int) Math.sqrt(max);
        for (int p = 2; p <= limit; p = composite.nextClearBit(p + 1)) {
            if (!composite.get(p)) {
                for (long m = (long) p * p; m <= max; m += p) {
                    composite.set((int) m);
                }
            }
        }

        List<Integer> primes = new ArrayList<>();
        for (int i = composite.nextClearBit(2); i >= 0 && i <= max; i = composite.nextClearBit(i + 1)) {
            primes.add(i);
            if (i == Integer.MAX_VALUE) break;
        }
        return primes;
    }
}
