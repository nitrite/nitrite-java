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
