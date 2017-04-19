package org.dizitart.no2.benchmark.core;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Anindya Chatterjee.
 */
public class RunResult {
    @Getter
    private List<ThreadResult> threadResults = new ArrayList<>();

    public float averageTime() {
        float time = 0;
        for (ThreadResult result : threadResults) {
            time += result.averageTime();
        }
        return time / threads();
    }

    public float averageMemory() {
        float memory = 0;
        for (ThreadResult result : threadResults) {
            memory += result.averageMemory();
        }
        return memory / threads();
    }

    public long minTime() {
        long time = Long.MAX_VALUE;
        for (ThreadResult result : threadResults) {
            time = Math.min(time, result.minTime());
        }
        return time;
    }

    public long minMemory() {
        long memory = Long.MAX_VALUE;
        for (ThreadResult result : threadResults) {
            memory = Math.min(memory, result.minMemory());
        }
        return memory;
    }

    public int threads() {
        return threadResults.size();
    }
}
