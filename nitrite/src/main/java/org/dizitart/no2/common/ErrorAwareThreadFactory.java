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

import javax.validation.constraints.NotNull;
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
    public Thread newThread(@NotNull Runnable r) {
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
