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

package org.dizitart.no2.sync;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.dizitart.no2.exceptions.InvalidOperationException;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static org.dizitart.no2.exceptions.ErrorMessage.REPLICATOR_ALREADY_RUNNING;

/**
 * Represents a handle for the replication operation.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 * @see Replicator
 */
@Setter(AccessLevel.PACKAGE)
public final class SyncHandle {
    private CollectionReplicator replicator;
    private ScheduledExecutorService replicatorPool;
    private ScheduledFuture replicatorHandle;
    private SyncService syncService;
    private boolean stopped =true;

    @Setter(AccessLevel.NONE)
    private SyncConfig syncConfig;

    /**
     * Indicates if the replication is paused or not.
     *
     * @return a boolean value indicating the pause status.
     * */
    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.NONE)
    private boolean paused;

    SyncHandle(SyncConfig syncConfig) {
        this.syncConfig = syncConfig;
    }

    /**
     * Starts replication in a background thread. This background thread
     * will run after every fixed amount of time (sync delay).
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link SyncEventListener}
     * instances with event type {@link EventType#STARTED}.
     *
     */
    public void startSync() {
        if (!stopped) {
            throw new InvalidOperationException(REPLICATOR_ALREADY_RUNNING);
        }

        TimeSpan syncDelay = syncConfig.getSyncDelay();
        this.replicatorHandle = replicatorPool.scheduleWithFixedDelay(replicator, 0,
                syncDelay.getTime(), syncDelay.getTimeUnit());

        stopped = false;
        syncService.notifyEvent(EventType.STARTED);
    }

    /**
     * Pauses the replicator. If any replicator thread is currently
     * running, it will not be paused but the next iteration will be paused
     * until it has been resumed by {@link #resumeSync()} call.
     *
     */
    public void pauseSync() {
        if (stopped) return;
        if (replicator != null) {
            replicator.pause();
        }
        paused = true;
    }

    /**
     * Resumes a paused replicator.
     */
    public void resumeSync() {
        if (stopped) return;
        if (replicator != null) {
            replicator.resume();
        }
        paused = false;
    }

    /**
     * Resets local collection with the remote collection.
     * Data can be fetched from remote in pages using `offset`
     * and `size` parameters.
     *
     * @param offset the pagination offset
     * @param size   the pagination size
     */
    public void resetLocalWithRemote(int offset, int size) {
        if (syncService != null) {
            syncService.resetLocalWithRemote(offset, size);
        }
    }

    /**
     * Resets remote collection with the local collection.
     * Data can be fetched from local in pages using `offset`
     * and `size` parameters.
     *
     * @param offset the pagination offset
     * @param size   the pagination size
     */
    public void resetRemoteWithLocal(int offset, int size) {
        if (syncService != null) {
            syncService.resetRemoteWithLocal(offset, size);
        }
    }

    /**
     * Cancels current replication thread.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link SyncEventListener}
     * instances with event type {@link EventType#CANCELED}.
     */
    public void cancelSync() {
        if (replicatorHandle != null && !replicatorHandle.isCancelled()) {
            replicatorHandle.cancel(true);
        }
        syncService.notifyEvent(EventType.CANCELED);
    }

    /**
     * Indicates whether the replicator thread is currently canceled.
     *
     * @return `true` if cancelled; `false` otherwise.
     */
    public boolean isCancelled() {
        return replicatorHandle == null || replicatorHandle.isCancelled();
    }

    /**
     * Stops current replicator.
     */
    public void stopSync() {
        if (replicator != null) {
            replicator.stop();
            stopped = true;
            syncService.notifyEvent(EventType.STOPPED);
        }
    }

    /**
     * Indicates whether the replicator is stopped.
     *
     * @return `true` if stopped; `false` otherwise.
     * */
    public boolean isStopped() {
        return stopped;
    }
}
