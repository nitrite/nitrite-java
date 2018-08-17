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
import org.dizitart.no2.Document;
import org.dizitart.no2.collection.Cursor;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.filters.Filters;
import org.dizitart.no2.meta.Attributes;
import org.dizitart.no2.sync.types.ChangeFeed;
import org.dizitart.no2.sync.types.FeedOptions;

import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.dizitart.no2.Document.createDocument;
import static org.dizitart.no2.collection.FindOptions.limit;
import static org.dizitart.no2.common.Constants.*;
import static org.dizitart.no2.filters.Filters.*;
import static org.dizitart.no2.util.DocumentUtils.isRecent;
import static org.dizitart.no2.util.Iterables.toList;


/**
 * @author Anindya Chatterjee.
 */
@Slf4j
class MockSyncTemplate implements SyncTemplate {
    private NitriteCollection remoteCollection;
    private NitriteCollection removeLogRepository;

    public MockSyncTemplate(NitriteCollection nitriteCollection,
                            NitriteCollection removeLogRepository) {
        this.remoteCollection = nitriteCollection;
        this.removeLogRepository = removeLogRepository;
    }

    @Override
    public ChangeFeed changedSince(FeedOptions feedOptions) {
        ChangeFeed changeFeed = new ChangeFeed();
        long newSequence = System.currentTimeMillis();
        changeFeed.setSequenceNumber(newSequence);

        changeFeed.setRemovedDocuments(
                removedItems(feedOptions.getFromSequence(), newSequence));
        changeFeed.setModifiedDocuments(
                modifiedItems(feedOptions.getFromSequence(), newSequence));

        return changeFeed;
    }

    @Override
    public boolean change(ChangeFeed changeFeed) {
        if (changeFeed.getRemovedDocuments() != null) {
            remove(changeFeed.getRemovedDocuments());
        }

        if (changeFeed.getModifiedDocuments() != null) {
            modify(changeFeed.getModifiedDocuments());
        }
        return true;
    }

    @Override
    public List<Document> fetch(int offset, int size) {
        return toList(remoteCollection.find(limit(offset, size)));
    }

    @Override
    public long size() {
        return remoteCollection.size();
    }

    @Override
    public void clear() {
        remoteCollection.remove(ALL);
    }

    @Override
    public boolean isOnline() {
        return true;
    }

    @Override
    public String getCollectionName() {
        return remoteCollection.getName();
    }

    @Override
    public synchronized boolean trySyncLock(TimeSpan expiryDelay, String issuer) {
        Attributes attributes = remoteCollection.getAttributes();
        if (attributes == null) {
            attributes = new Attributes(remoteCollection.getName());
        }
        long syncLock = attributes.getSyncLock();
        long expiryWait = attributes.getExpiryWait();
        long expiryTime = syncLock + expiryWait;   // in milliseconds
        if (syncLock == 0
                || expiryTime < System.currentTimeMillis()) {
            // acquire lock
            acquireLock(attributes, expiryDelay);
            log.debug("Remote lock acquired");
            return true;
        }
        log.debug("Failed to acquire remote lock");
        return false;
    }

    @Override
    public void releaseLock(String issuer) {
        Attributes attributes = remoteCollection.getAttributes();
        attributes.setSyncLock(0);
        attributes.setExpiryWait(0);
        remoteCollection.setAttributes(attributes);
        log.debug("Remote lock released");
    }

    private void modify(List<Document> insertedDocuments) {
        long time = System.currentTimeMillis();

        for (Document document : insertedDocuments) {
            Document doc = new Document(document);
            doc.put(DOC_SOURCE, REPLICATOR);
            doc.put(DOC_SYNCED, time);

            Document existing = getExistingDocument(doc);
            if (existing != null && isRecent(doc, existing)) {
                existing.putAll(doc);
                remoteCollection.update(existing);
            } else if (existing == null) {
                Cursor removeLogs = removeLogRepository.find(
                        and(
                                eq(COLLECTION, remoteCollection.getName()),
                                eq(DELETED_ID, document.getId().getIdValue()),
                                gt(DELETE_TIME, document.getLastModifiedTime())
                        )
                );
                if (removeLogs == null || removeLogs.size() == 0) {
                    remoteCollection.insert(doc);
                }
            }
        }
    }

    private void remove(List<Document> removedDocuments) {
        long time = System.currentTimeMillis();

        for (Document document : removedDocuments) {
            Document doc = new Document(document);
            doc.put(DOC_SOURCE, REPLICATOR);
            doc.put(DOC_SYNCED, time);
            remoteCollection.remove(document);

            if (removeLogRepository != null) {
                Document logEntry = createDocument(COLLECTION, remoteCollection.getName())
                        .put(DELETE_TIME, time)
                        .put(DELETED_ITEM, document);

                removeLogRepository.insert(logEntry);
            }
        }
    }

    private List<Document> removedItems(long fromTimestamp, long toTimestamp) {
        List<Document> documentList = new ArrayList<>();
        if (removeLogRepository != null) {
            Iterable<Document> removeLogs = removeLogRepository.find(
                    and(
                            eq(COLLECTION, remoteCollection.getName()),
                            gte(DELETE_TIME, fromTimestamp),
                            lte(DELETE_TIME, toTimestamp)
                    )
            );

            if (removeLogs != null) {
                for (Document logEntry : removeLogs) {
                    Document document = new Document(logEntry.get(DELETED_ITEM, Document.class));
                    document.remove(DOC_SYNCED);
                    document.remove(DOC_SOURCE);
                    documentList.add(document);
                }
            }
        }

        log.debug("Removed since in " + remoteCollection.getName() + ": " +
                "from " + fromTimestamp + " now " + toTimestamp + " - " + documentList);
        return documentList;
    }

    private List<Document> modifiedItems(long lastSequence, long newSequence) {
        Iterable<Document> findResult = remoteCollection.find(
                Filters.and(
                        Filters.gte(DOC_SYNCED, lastSequence),
                        Filters.lte(DOC_SYNCED, newSequence))
                );
        List<Document> result = new ArrayList<>();
        for (Document document : findResult) {
            Document doc = new Document(document);
            doc.remove(DOC_SYNCED);
            doc.remove(DOC_SOURCE);
            result.add(doc);
        }

        log.debug("Updated since in " + remoteCollection.getName() + ": from "
                + lastSequence + " now " + newSequence + " - " + result);
        return result;

    }

    private Document getExistingDocument(Document document) {
        return remoteCollection.getById(document.getId());
    }

    private void acquireLock(Attributes attributes, TimeSpan expireDelay) {
        attributes.setSyncLock(System.currentTimeMillis());
        attributes.setExpiryWait(MILLISECONDS
                .convert(expireDelay.getTime(), expireDelay.getTimeUnit()));
        remoteCollection.setAttributes(attributes);
    }
}
