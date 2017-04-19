package org.dizitart.no2.benchmark.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Anindya Chatterjee.
 */
public class BenchmarkRunner {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private List<Class<? extends Benchmark>> testPack = new ArrayList<>();
    private TestConfig testConfig;
    private ExecutorService executorService = Executors.newCachedThreadPool(new BenchmarkThreadFactory());

    public BenchmarkRunner(TestConfig config) {
        testConfig = config;
    }

    @SafeVarargs
    public final void registerTests(Class<? extends Benchmark>... tests) {
        Collections.addAll(testPack, tests);
    }

    public TestResult runTests() {
        TestResult testResult = new TestResult();

        for (final Class<? extends Benchmark> testClass : testPack) {
            final Benchmark test;
            try {
                test = testClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                logger.error("Can not instantiate test class", e);
                continue;
            }

            final String testName = testClass.getName();

            // warm up run
            runOnThreads(test, testName, true);

            // actual run
            RunResult runResult = runOnThreads(test, testName, false);
            testResult.getRunResults().put(testName, runResult);
        }

        executorService.shutdown();
        return testResult;
    }

    private RunResult runOnThreads(final Benchmark test, final String testName, final boolean isWarmUp) {
        RunResult runResult = new RunResult();

        logger.debug("");
        if (isWarmUp) {
            logger.debug("------ Warming up for " + testName + " -------");
        } else {
            logger.debug("------ Benchmark for " + testName + " -------");
        }

        // forceful gc
        forceGc();

        try {
            if (isWarmUp) {
                logger.debug("beforeTest warm up of " + testName);
            } else {
                logger.debug("beforeTest for " + testName);
            }

            test.beforeTest();

            for (int i = 0; i < testConfig.getThreads(); i++) {
                Future<ThreadResult> iterationResults = executorService.submit(new Callable<ThreadResult>() {
                    @Override
                    public ThreadResult call() throws Exception {
                        return runIterations(test, testName, isWarmUp);
                    }
                });

                ThreadResult results = iterationResults.get();
                runResult.getThreadResults().add(results);
            }
        } catch (Throwable error) {
            if (isWarmUp) {
                logger.error("Error while running warm up thread for test " + testName, error);
            } else {
                logger.error("Error while running thread for test " + testName, error);
            }
        } finally {
            if (isWarmUp) {
                logger.debug("afterTest warm up for " + testName);
            } else {
                logger.debug("afterTest for " + testName);
            }

            try {
                test.afterTest();
            } catch (Throwable error) {
                if (isWarmUp) {
                    logger.error("error while executing afterTest warm up for " + testName, error);
                } else {
                    logger.error("error while executing afterTest for " + testName, error);
                }
            }
        }

        return runResult;
    }

    private ThreadResult runIterations(Benchmark test, String testName, boolean isWarmUp) {
        ThreadResult threadResult = new ThreadResult();

        try {
            int iteration = isWarmUp ? testConfig.getWarmupIterations() : testConfig.getIterations();

            logger.debug("");
            if (isWarmUp) {
                logger.debug("beforeRun warm up for " + testName);
            } else {
                logger.debug("beforeRun for " + testName);
            }

            test.beforeRun();

            for (int i = 1; i < iteration + 1; i++) {
                IterationResult iterationResult = new IterationResult(i);

                if (isWarmUp) {
                    logger.debug("warm up iteration " + i);
                } else {
                    logger.debug("iteration " + i);
                }

                forceGc();

                Runtime runtime = Runtime.getRuntime();
                long usedMemory = runtime.totalMemory() - runtime.freeMemory();
                long startTime = System.nanoTime();

                test.runTest();

                long timeTaken = System.nanoTime() - startTime;
                long consumedMemory = runtime.totalMemory() - runtime.freeMemory() - usedMemory;

                logger.debug("Time taken = " + timeTaken + "ns");
                logger.debug("Memory consumed = " + consumedMemory + " bytes");
                logger.debug("");

                iterationResult.setMemory(consumedMemory);
                iterationResult.setTime(timeTaken);

                threadResult.getIterationResults().add(iterationResult);
            }
        } catch (Throwable error) {
            if (isWarmUp) {
                logger.error("Error while running warm up for test " + testName, error);
            } else {
                logger.error("Error while running test " + testName, error);
            }
        } finally {
            if (isWarmUp) {
                logger.debug("afterRun warm up for " + testName);
            } else {
                logger.debug("afterRun for " + testName);
            }

            try {
                test.afterRun();
            } catch (Throwable error) {
                if (isWarmUp) {
                    logger.debug("error while executing afterTest warm up for " + testName, error);
                } else {
                    logger.debug("error while executing afterTest for " + testName, error);
                }
            }
        }

        logger.debug("---------------- Average " + Thread.currentThread().getName() + " -----------------");
        logger.debug("Average Memory (mb): " + threadResult.averageMemory() / (1024 * 1024));
        logger.debug("Average Time (s): " + threadResult.averageTime() / 1000000000);
        logger.debug("---------------------------------------------------");
        logger.debug("");

        return threadResult;
    }


    private static class BenchmarkThreadFactory implements ThreadFactory {
        private AtomicInteger atomicInteger = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("Bench-" + atomicInteger.getAndIncrement());
            return thread;
        }
    }

    private static void forceGc() {
        System.gc();
        System.runFinalization();
        final CountDownLatch latch = new CountDownLatch(1);
        new Object() {
            @Override protected void finalize() {
                latch.countDown();
            }
        };
        System.gc();
        System.runFinalization();
        try {
            latch.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
