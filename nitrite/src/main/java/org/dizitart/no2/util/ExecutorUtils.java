package org.dizitart.no2.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

import static org.dizitart.no2.Constants.DAEMON_THREAD_NAME;
import static org.dizitart.no2.Constants.SCHEDULED_THREAD_NAME;

/**
 * A utility class for {@link ExecutorService}.
 *
 * @author Anindya Chatterjee.
 * @since 1.0
 */
@Slf4j
@UtilityClass
public class ExecutorUtils {
    /**
     * Creates an {@link ExecutorService} where all {@link Thread}s are
     * daemon threads and uncaught error aware.
     *
     * @return the {@link ExecutorService}.
     */
    public static ExecutorService daemonExecutor() {
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(10, 10,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
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
        return threadPool;
    }

    /**
     * Creates a {@link ScheduledExecutorService} where all {@link Thread}s are
     * daemon threads and uncaught error aware.
     *
     * @return the {@link ScheduledExecutorService}.
     */
    public static ScheduledExecutorService scheduledExecutor() {
        return Executors.newScheduledThreadPool(1, new ErrorAwareThreadFactory() {
            @Override
            public Thread createThread(Runnable runnable) {
                Thread thread = new Thread(runnable);
                thread.setName(SCHEDULED_THREAD_NAME);
                thread.setDaemon(true);
                return thread;
            }
        });
    }

    /**
     * Shuts down and awaits termination of an {@link ExecutorService}.
     *
     * @param pool    the {@link ExecutorService}
     * @param timeout the timeout in seconds
     */
    public static void shutdownAndAwaitTermination(ExecutorService pool, int timeout) {
        synchronized (pool) {
            // Disable new tasks from being submitted
            pool.shutdown();
        }
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(timeout, TimeUnit.SECONDS)) {
                synchronized (pool) {
                    pool.shutdownNow(); // Cancel currently executing tasks
                }
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(timeout, TimeUnit.SECONDS)) {
                    log.error("Executor did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            synchronized (pool) {
                pool.shutdownNow();
            }
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
