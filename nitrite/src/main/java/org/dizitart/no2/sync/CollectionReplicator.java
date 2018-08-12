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

import lombok.extern.slf4j.Slf4j;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
class CollectionReplicator implements Runnable {
    private boolean running;
    private boolean paused;
    private boolean stopped;
    private ReplicationType replicationType;
    private SyncService syncService;

    @Override
    public void run() {
        try {
            if (stopped) return;

            String localName = syncService.getLocalCollection().getName();
            String remoteName = syncService.getSyncConfig().getSyncTemplate().getCollectionName();

            log.debug("Initiating synchronization sequence for [" + localName + " <-> "
                    + remoteName + "]");

            if (running) {
                log.warn("Previous replicator is still running, aborting the recent run");
                return;
            }

            if (paused) {
                log.warn("Sync is currently paused");
                return;
            }

            running = true;

            switch (replicationType) {
                case PULL:
                    syncService.pullChanges();
                    break;
                case PUSH:
                    syncService.pushChanges();
                    break;
                case BOTH_WAY:
                    syncService.mergeChanges();
                    break;
            }

            running = false;
        } catch (Throwable error) {
            log.error("Synchronization error", error);
        }
    }

    void pause() {
        paused = true;
    }

    void resume() {
        paused = false;
    }

    void stop() {
        stopped = true;
    }

    void setSyncService(SyncService syncService) {
        this.syncService = syncService;
    }

    void setReplicationType(ReplicationType replicationType) {
        this.replicationType = replicationType;
    }
}
