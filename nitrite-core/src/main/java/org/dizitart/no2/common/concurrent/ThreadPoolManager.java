/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.common.concurrent;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.dizitart.no2.common.Constants.DAEMON_THREAD_NAME;

/**
 * A factory class for creating {@link ExecutorService}.
 *
 * @author Anindya Chatterjee.
 * @since 4.0.0
 */
@Slf4j
public class ThreadPoolManager {

    /**
     * Creates an {@link ExecutorService} with pull size {@link Runtime#availableProcessors()}
     * where all {@link Thread}s are daemon threads and uncaught error aware.
     *
     * @return the {@link ExecutorService}.
     */
    public static ExecutorService workerPool() {
        return getThreadPool(Runtime.getRuntime().availableProcessors(), DAEMON_THREAD_NAME);
    }

    /**
     * Creates an {@link ExecutorService} with provided size where
     * all {@link Thread}s are daemon threads and uncaught error aware.
     *
     * @return the {@link ExecutorService}.
     */
    public static ExecutorService getThreadPool(int size, String threadName) {
        return Executors.newFixedThreadPool(size, threadFactory(threadName));
    }

    public static ErrorAwareThreadFactory threadFactory(String name) {
        return new ErrorAwareThreadFactory() {
            @Override
            public Thread createThread(Runnable runnable) {
                Thread thread = new Thread(runnable);
                thread.setName(name);
                thread.setDaemon(true);
                return thread;
            }
        };
    }
}
