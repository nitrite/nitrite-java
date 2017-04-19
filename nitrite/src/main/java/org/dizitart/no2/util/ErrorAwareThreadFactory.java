package org.dizitart.no2.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadFactory;

/**
 * A {@link ThreadFactory} implementation which creates
 * {@link Thread} which is aware of any uncaught exception.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
@Slf4j
public abstract class ErrorAwareThreadFactory implements ThreadFactory {
    /**
     * Creates a new {@link Thread}.
     *
     * @param runnable the runnable
     * @return the thread
     */
    public abstract Thread createThread(Runnable runnable);

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = createThread(r);
        if (thread.getUncaughtExceptionHandler() == null) {
            thread.setUncaughtExceptionHandler(getUncaughtErrorHandler());
        }
        return thread;
    }

    /**
     * Gets unhandled error handler.
     *
     * @return the unhandled error handler
     */
    protected Thread.UncaughtExceptionHandler getUncaughtErrorHandler() {
        return new UncaughtErrorHandler();
    }

    private class UncaughtErrorHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            log.error("Unhandled error in " + t.getName(), e);
        }
    }
}
