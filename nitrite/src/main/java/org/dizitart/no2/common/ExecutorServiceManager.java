/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
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
 *
 */

package org.dizitart.no2.common;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

import static org.dizitart.no2.common.Constants.DAEMON_THREAD_NAME;
import static org.dizitart.no2.common.Constants.SCHEDULED_THREAD_NAME;

/**
 * A factory for managing for all {@link ExecutorService}.
 *
 * @author Anindya Chatterjee.
 * @since 4.0.0
 */
@Slf4j
public class ExecutorServiceManager {
    private static Map<Integer, ExecutorService> daemonExecutors = new ConcurrentHashMap<>();
    private static Map<Integer, ScheduledExecutorService> scheduledExecutors = new ConcurrentHashMap<>();
    private static final Object lock = new Object();

    /**
     * Creates an {@link ExecutorService} with pull size {@link Integer#MAX_VALUE}
     * where all {@link Thread}s are daemon threads and uncaught error aware.
     *
     * @return the {@link ExecutorService}.
     */
    public static ExecutorService daemonExecutor() {
        return daemonExecutor(Integer.MAX_VALUE);
    }

    /**
     * Creates an {@link ExecutorService} where all {@link Thread}s are
     * daemon threads and uncaught error aware.
     *
     * @param poolSize maximum pool size.
     * @return the {@link ExecutorService}.
     */
    public static ExecutorService daemonExecutor(int poolSize) {
        if (daemonExecutors.containsKey(poolSize)) {
            return daemonExecutors.get(poolSize);
        }

        final BlockingQueue<Runnable> queue = new SynchronousQueue<>();
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(0, poolSize,
                60L, TimeUnit.SECONDS,
                queue,
                new ErrorAwareThreadFactory() {
                    @Override
                    public Thread createThread(Runnable runnable) {
                        Thread thread = new Thread(runnable);
                        thread.setName(DAEMON_THREAD_NAME);
                        thread.setDaemon(true);
                        return thread;
                    }
                });
        threadPool.allowCoreThreadTimeOut(true);
        threadPool.setRejectedExecutionHandler((r, executor) -> {
            try {
                executor.getQueue().put(r);
            } catch (InterruptedException ie) {
                log.error("Thread interrupted while submitting rejected job", ie);
            }
        });

        daemonExecutors.put(poolSize, threadPool);
        return threadPool;
    }

    /**
     * Creates a {@link ScheduledExecutorService} with pool size 1
     * where all {@link Thread}s are daemon threads and uncaught error aware.
     *
     * @return the {@link ScheduledExecutorService}.
     */
    public static ScheduledExecutorService scheduledExecutor() {
        return scheduledExecutor(1);
    }

    /**
     * Creates a {@link ScheduledExecutorService} where all {@link Thread}s are
     * daemon threads and uncaught error aware.
     *
     * @param poolSize maximum pool size
     * @return the {@link ScheduledExecutorService}.
     */
    public static ScheduledExecutorService scheduledExecutor(int poolSize) {
        if (scheduledExecutors.containsKey(poolSize)) {
            return scheduledExecutors.get(poolSize);
        }

        ScheduledExecutorService executorService =
                Executors.newScheduledThreadPool(poolSize, new ErrorAwareThreadFactory() {
                    @Override
                    public Thread createThread(Runnable runnable) {
                        Thread thread = new Thread(runnable);
                        thread.setName(SCHEDULED_THREAD_NAME);
                        thread.setDaemon(true);
                        return thread;
                    }
                });
        scheduledExecutors.put(poolSize, executorService);
        return executorService;
    }

    /**
     * Shuts down and awaits termination of all {@link ExecutorService}s.
     *
     * @param timeout the timeout in seconds
     */
    public static void shutdownExecutors(int timeout) {
        if (!daemonExecutors.isEmpty()) {
            for (ExecutorService daemonExecutor : daemonExecutors.values()) {
                shutdownAndAwaitTermination(daemonExecutor, timeout);
            }
        }

        if (!scheduledExecutors.isEmpty()) {
            for (ExecutorService scheduledExecutor : scheduledExecutors.values()) {
                shutdownAndAwaitTermination(scheduledExecutor, timeout);
            }
        }

        daemonExecutors.clear();
        scheduledExecutors.clear();
    }


    private static void shutdownAndAwaitTermination(final ExecutorService pool, int timeout) {
        synchronized (lock) {
            // Disable new tasks from being submitted
            pool.shutdown();
        }
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(timeout, TimeUnit.SECONDS)) {
                synchronized (lock) {
                    pool.shutdownNow(); // Cancel currently executing tasks
                }
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(timeout, TimeUnit.SECONDS)) {
                    log.error("Executor did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            synchronized (lock) {
                pool.shutdownNow();
            }
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
