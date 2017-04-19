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
