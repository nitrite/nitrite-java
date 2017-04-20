/*
 * Copyright 2017 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.benchmark;

import org.dizitart.no2.benchmark.core.BenchmarkRunner;
import org.dizitart.no2.benchmark.core.TestConfig;
import org.dizitart.no2.benchmark.core.TestResult;
import org.dizitart.no2.benchmark.tests.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;

/**
 * @author Anindya Chatterjee.
 */
public class BenchmarkSuite {
    private static Logger logger = LoggerFactory.getLogger(BenchmarkSuite.class);

    public static void main(String[] args) throws IOException {
        logger.info("----------------------------------------------------");
        logger.info("Initiating benchmark test at " + new Date());
        logger.info("Java Runtime - " + System.getProperty("java.vm.name") + " v" + System.getProperty("java.version") );
        logger.info("Operating System - " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch"));
        logger.info("Processors Count - " + Runtime.getRuntime().availableProcessors());
        logger.info("Total Memory - " + Runtime.getRuntime().totalMemory());
        logger.info("----------------------------------------------------");

        TestConfig testConfig = new TestConfig();
        testConfig.setIterations(20);
        testConfig.setWarmupIterations(20);
        testConfig.setThreads(4);

        BenchmarkRunner runner = new BenchmarkRunner(testConfig);
        runner.registerTests(NitriteInsert.class);
        runner.registerTests(OrientInsert.class);
        runner.registerTests(MongoInsert.class);

        runner.registerTests(NitriteSearch.class);
        runner.registerTests(OrientSearch.class);
        runner.registerTests(MongoSearch.class);


        TestResult testResult = runner.runTests();

        for (String test : testResult) {
            logger.info("");
            logger.info("---------------- " + test + " -----------------");
            logger.info(String.format(Locale.ENGLISH, "Average Memory (mb): %f",
                    testResult.averageMemory(test) / (1024 * 1024)));

            logger.info(String.format(Locale.ENGLISH, "Average Time (ms): %f",
                    testResult.averageTime(test) / 1000000));

            logger.info("");

            logger.info(String.format(Locale.ENGLISH, "Min Memory (mb): %f",
                    (float) testResult.minMemory(test) / (1024 * 1024)));

            logger.info(String.format(Locale.ENGLISH, "Min Time (ms): %f",
                    (float) testResult.minTime(test) / 1000000));
            logger.info("-----------------------------------------------");
        }

//        NitriteSearch run = new NitriteSearch();
//        run.beforeTest();
//        run.beforeRun();
//        run.closeDb();
    }
}
