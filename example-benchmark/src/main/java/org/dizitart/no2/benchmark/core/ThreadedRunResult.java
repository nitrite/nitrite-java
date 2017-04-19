package org.dizitart.no2.benchmark.core;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Anindya Chatterjee.
 */
public class ThreadedRunResult {
    @Getter
    private Map<Integer, ThreadResult> resultSet = new HashMap<>();

    public void add(Integer testName, ThreadResult threadResult) {
        resultSet.put(testName, threadResult);
    }
}
