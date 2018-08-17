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
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.event.EventBus;
import org.dizitart.no2.meta.Attributes;
import org.dizitart.no2.sync.types.ChangeFeed;
import org.dizitart.no2.sync.types.FeedOptions;

import java.util.List;

import static org.dizitart.no2.collection.FindOptions.limit;
import static org.dizitart.no2.common.Constants.*;
import static org.dizitart.no2.filters.Filters.*;
import static org.dizitart.no2.util.Iterables.toList;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
class SyncService {
    @Getter @Setter(AccessLevel.NONE)
    private SyncConfig syncConfig;
    @Getter @Setter
    private NitriteCollection changeLogRepository;
    @Getter @Setter
    private EventBus<SyncEventData, SyncEventListener> syncEventBus;
    @Getter
    private LocalCollection localCollection;

    private String originator;
    private SyncTemplate syncTemplate;

    void pullChanges() {
        if (syncTemplate.isOnline()) {
            // pull all changes from remote to local, but local changes are
            // not pushed to remote.
            // create the last sync time of local, pull everything from remote
            // from that time and update local.
            try {
                if (syncTemplate.trySyncLock(syncConfig.getSyncDelay(), originator)) {
                    notifyEvent(EventType.IN_PROGRESS);

                    Attributes attributes = localCollection.getAttributes();
                    attributes = attributes == null ? new Attributes(localCollection.getName()) : attributes;

                    long from = attributes.getLastSynced();

                    FeedOptions feedOptions = new FeedOptions();
                    feedOptions.setFromSequence(from);

                    ChangeFeed remoteChangeFeed = syncTemplate.changedSince(feedOptions);
                    localCollection.change(remoteChangeFeed);

                    attributes.setLastSynced(remoteChangeFeed.getSequenceNumber());
                    localCollection.setAttributes(attributes);

                    notifyEvent(EventType.COMPLETED);
                }
            } catch (Exception error) {
                notifyErrorEvent(error, EventType.REPLICATION_ERROR);
            } finally {
                syncTemplate.releaseLock(originator);
            }
        }
    }

    void pushChanges() {
        if (syncTemplate.isOnline()) {
            // push all changes from local to remote, but remote changes
            // are not downloaded to local.
            try {
                if (syncTemplate.trySyncLock(syncConfig.getSyncDelay(), originator)) {
                    notifyEvent(EventType.IN_PROGRESS);

                    Attributes attributes = localCollection.getAttributes();
                    attributes = attributes == null ? new Attributes(localCollection.getName()) : attributes;

                    long lastSequence = attributes.getLastSynced();
                    long newSequence = System.currentTimeMillis();

                    ChangeFeed changeFeed = localCollection.changedSince(lastSequence, newSequence);
                    changeFeed.setOriginator(originator);
                    changeFeed.setSequenceNumber(newSequence);
                    if (syncTemplate.change(changeFeed)) {
                        clearRemoveLogSince(newSequence);
                    }

                    attributes.setLastSynced(newSequence);
                    localCollection.setAttributes(attributes);

                    notifyEvent(EventType.COMPLETED);
                }
            } catch (Exception error) {
                notifyErrorEvent(error, EventType.REPLICATION_ERROR);
            } finally {
                syncTemplate.releaseLock(originator);
            }
        }
    }

    void mergeChanges() {
        if (syncTemplate.isOnline()) {
            try {
                if (syncTemplate.trySyncLock(syncConfig.getSyncDelay(), originator)) {
                    notifyEvent(EventType.IN_PROGRESS);

                    Attributes attributes = localCollection.getAttributes() == null
                            ? new Attributes(localCollection.getName())
                            : localCollection.getAttributes();

                    long lastSequence = attributes.getLastSynced();

                    FeedOptions feedOptions = new FeedOptions();
                    feedOptions.setFromSequence(lastSequence);

                    ChangeFeed remoteChanges = syncTemplate.changedSince(feedOptions);

                    ChangeFeed localChanges = localCollection.changedSince(lastSequence,
                            remoteChanges.getSequenceNumber());
                    localChanges.setOriginator(originator);

                    if (mergeChanges(remoteChanges, localChanges)) {
                        clearRemoveLogSince(remoteChanges.getSequenceNumber());
                    }

                    attributes.setLastSynced(remoteChanges.getSequenceNumber());
                    localCollection.setAttributes(attributes);

                    notifyEvent(EventType.COMPLETED);
                }
            } catch (Exception error) {
                notifyErrorEvent(error, EventType.REPLICATION_ERROR);
            } finally {
                syncTemplate.releaseLock(originator);
            }
        }
    }

    void resetLocalWithRemote(int offset, int size) {
        if (syncTemplate.isOnline()) {
            try {
                notifyEvent(EventType.IN_PROGRESS);
                localCollection.clear();

                List<Document> documents = syncTemplate.fetch(offset, size);

                for (Document document : documents) {
                    document.put(DOC_SOURCE, REPLICATOR);
                }
                localCollection.insert(documents.toArray(new Document[0]));

                notifyEvent(EventType.COMPLETED);
            } catch (Exception error) {
                notifyErrorEvent(error, EventType.REPLICATION_ERROR);
            }
        }
    }

    void resetRemoteWithLocal(int offset, int size) {
        if (syncTemplate.isOnline()) {
            try {
                if (syncTemplate.trySyncLock(syncConfig.getSyncDelay(), originator)) {
                    notifyEvent(EventType.IN_PROGRESS);
                    syncTemplate.clear();

                    NitriteCollection nitriteCollection = localCollection.getCollection();

                    Iterable<Document> documents = nitriteCollection.find(limit(offset, size));

                    ChangeFeed changeFeed = new ChangeFeed();
                    changeFeed.setModifiedDocuments(toList(documents));
                    changeFeed.setOriginator(originator);

                    syncTemplate.change(changeFeed);
                    notifyEvent(EventType.COMPLETED);
                }
            } catch (Exception error) {
                notifyErrorEvent(error, EventType.REPLICATION_ERROR);
            } finally {
                syncTemplate.releaseLock(originator);
            }
        }
    }

    void setSyncConfig(SyncConfig syncConfig) {
        this.syncConfig = syncConfig;
        this.syncTemplate = syncConfig.getSyncTemplate();
    }

    void setLocalCollection(LocalCollection localCollection) {
        this.localCollection = localCollection;
        this.localCollection.setChangeLogRepository(changeLogRepository);

        Attributes attributes = localCollection.getAttributes() == null
                ? new Attributes(localCollection.getName())
                : localCollection.getAttributes();
        localCollection.setAttributes(attributes);

        this.originator = attributes.getUuid();
    }

    private void clearRemoveLogSince(long upto) {
        changeLogRepository.remove(
                and(
                        eq(COLLECTION, localCollection.getName()),
                        lte(DELETE_TIME, upto)
                )
        );
    }

    private boolean mergeChanges(ChangeFeed remoteChanges, ChangeFeed localChanges) {
        boolean result = syncTemplate.change(localChanges);
        localCollection.change(remoteChanges);
        return result;
    }

    void notifyEvent(EventType eventType) {
        SyncEventData eventData = new SyncEventData();
        eventData.setEventType(eventType);
        eventData.setCollectionName(localCollection.getName());
        syncEventBus.post(eventData);
    }

    private void notifyErrorEvent(Throwable error, EventType eventType) {
        log.error("Replication error", error);
        SyncEventData eventData = new SyncEventData();
        eventData.setEventType(eventType);
        eventData.setCollectionName(localCollection.getName());
        eventData.setError(error);
        syncEventBus.post(eventData);
    }
}
