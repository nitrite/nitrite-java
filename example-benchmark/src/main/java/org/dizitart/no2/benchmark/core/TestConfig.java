package org.dizitart.no2.benchmark.core;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Anindya Chatterjee.
 */
public class TestConfig {
    @Getter @Setter
    private int iterations;

    @Getter @Setter
    private int warmupIterations;

    @Getter @Setter
    private int threads;

    public TestConfig() {
        warmupIterations = iterations = 10;
        threads = Runtime.getRuntime().availableProcessors();
    }
}
