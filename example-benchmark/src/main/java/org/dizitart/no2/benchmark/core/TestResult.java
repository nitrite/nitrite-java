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
