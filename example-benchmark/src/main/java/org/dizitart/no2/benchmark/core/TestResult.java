package org.dizitart.no2.benchmark.core;

import lombok.Getter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Anindya Chatterjee.
 */
public class TestResult implements Iterable<String> {
    @Getter
    private Map<String, RunResult> runResults = new HashMap<>();

    public float averageTime(String name) {
        RunResult runResult = runResults.get(name);
        return runResult.averageTime();
    }

    public float averageMemory(String name) {
        RunResult runResult = runResults.get(name);
        return runResult.averageMemory();
    }

    public long minTime(String name) {
        RunResult runResult = runResults.get(name);
        return runResult.minTime();
    }

    public long minMemory(String name) {
        RunResult runResult = runResults.get(name);
        return runResult.minMemory();
    }

    public int tests() {
        return runResults.size();
    }

    @Override
    public Iterator<String> iterator() {
        return runResults.keySet().iterator();
    }
}
