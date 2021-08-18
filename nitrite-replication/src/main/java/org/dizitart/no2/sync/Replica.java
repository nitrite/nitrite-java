/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.sync;

import org.dizitart.no2.common.concurrent.ThreadPoolManager;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.dizitart.no2.common.Constants.SYNC_THREAD_NAME;

/**
 * Represents a remote replica of the local {@link org.dizitart.no2.collection.NitriteCollection}
 * or {@link org.dizitart.no2.repository.ObjectRepository}.
 *
 * @author Anindya Chatterjee
 * @since 4.0.0
 */
public class Replica implements AutoCloseable {
    private final Config config;
    private final ReplicatedCollection replicatedCollection;
    private ScheduledExecutorService scheduledExecutorService;
    private ScheduledFuture<?> replicationTask;

    Replica(Config config, ReplicatedCollection replicatedCollection) {
        this.config = config;
        this.replicatedCollection = replicatedCollection;
        configure();
    }

    public static ReplicaBuilder builder() {
        return new ReplicaBuilder();
    }

    public void connect() {
        if (scheduledExecutorService != null) {
            if (scheduledExecutorService.isShutdown() || scheduledExecutorService.isTerminated()) {
                scheduledExecutorService = getSyncThreadPool();
            }

            if (replicationTask == null || replicationTask.isCancelled()) {
                replicationTask = scheduledExecutorService.scheduleAtFixedRate(() -> {
                    if (!isConnected()) {
                        replicatedCollection.startReplication();
                    }
                }, 0, config.getPollingRate(), TimeUnit.MILLISECONDS);
            }
        } else {
            throw new ReplicationException("replica is not configured properly", true);
        }
    }

    public void disconnect() {
        disconnectInternal(false);
    }

    public void disconnectNow() {
        disconnectInternal(true);
    }

    public boolean isConnected() {
        return replicatedCollection.isConnected();
    }

    public void close() {
        replicatedCollection.stopReplication(null, "User close");
        ThreadPoolManager.shutdownThreadPool(scheduledExecutorService);
    }

    private void disconnectInternal(boolean mayInterruptIfRunning) {
        if (scheduledExecutorService != null) {
            if (!scheduledExecutorService.isShutdown() && !scheduledExecutorService.isTerminated()) {
                replicationTask.cancel(mayInterruptIfRunning);
                ThreadPoolManager.shutdownThreadPool(scheduledExecutorService);
            }
        } else {
            throw new ReplicationException("replica is not configured properly", true);
        }
    }

    private void configure() {
        this.scheduledExecutorService = getSyncThreadPool();
    }

    private static ScheduledExecutorService getSyncThreadPool() {
        return ThreadPoolManager.getScheduledThreadPool(1, SYNC_THREAD_NAME);
    }
}
