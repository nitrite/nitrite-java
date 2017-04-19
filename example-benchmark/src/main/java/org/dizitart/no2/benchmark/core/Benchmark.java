package org.dizitart.no2.benchmark.core;

/**
 * @author Anindya Chatterjee.
 */
public interface Benchmark {
    void beforeTest();
    void beforeRun();
    void runTest();
    void afterRun();
    void afterTest() throws Exception;
}
