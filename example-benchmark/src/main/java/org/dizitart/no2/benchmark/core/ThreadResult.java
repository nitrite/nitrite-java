package org.dizitart.no2.benchmark.core;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Anindya Chatterjee.
 */
public class ThreadResult {
    @Getter
    private List<IterationResult> iterationResults = new ArrayList<>();

    public float averageTime() {
        long time = 0;
        for (IterationResult result : iterationResults) {
            time += result.getTime();
        }
        return (float) time / iterations();
    }

    public float averageMemory() {
        long memory = 0;
        for (IterationResult result : iterationResults) {
            memory += result.getMemory();
        }
        return (float) memory / iterations();
    }

    public long minTime() {
        long time = Long.MAX_VALUE;
        for (IterationResult result : iterationResults) {
            time = Math.min(time, result.getTime());
        }
        return time;
    }

    public long minMemory() {
        long memory = Long.MAX_VALUE;
        for (IterationResult result : iterationResults) {
            memory = Math.min(memory, result.getMemory());
        }
        return memory;
    }

    public int iterations() {
        return iterationResults.size();
    }
}
