/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.collection.operation;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.UpdateOptions;
import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.collection.events.EventType;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.common.event.EventBus;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.UniqueConstraintException;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.store.NitriteMap;

import java.util.HashSet;
import java.util.Set;

import static org.dizitart.no2.common.Constants.*;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
class WriteOperations {
    private final IndexOperations indexOperations;
    private final ReadOperations readOperations;
    private final EventBus<CollectionEventInfo<?>, CollectionEventListener> eventBus;
    private final NitriteMap<NitriteId, Document> nitriteMap;

    WriteOperations(IndexOperations indexOperations,
                    ReadOperations readOperations,
                    NitriteMap<NitriteId, Document> nitriteMap,
                    EventBus<CollectionEventInfo<?>, CollectionEventListener> eventBus) {
        this.indexOperations = indexOperations;
        this.readOperations = readOperations;
        this.eventBus = eventBus;
        this.nitriteMap = nitriteMap;
    }

    WriteResult insert(Document... documents) {
        Set<NitriteId> nitriteIds = new HashSet<>(documents.length);
        log.debug("Total {} document(s) to be inserted in {}", documents.length, nitriteMap.getName());

        for (Document document : documents) {
            Document item = document.clone();
            NitriteId nitriteId = item.getId();
            String source = item.getSource();
            long time = System.currentTimeMillis();

            if (!REPLICATOR.contentEquals(item.getSource())) {
                // if replicator is not inserting the document that means
                // it is being inserted by user, so update metadata
                item.remove(DOC_SOURCE);
                item.put(DOC_REVISION, 1);
                item.put(DOC_MODIFIED, time);
            } else {
                // if replicator is inserting the document, remove the source
                // but keep the revision intact
                item.remove(DOC_SOURCE);
            }

            log.debug("Inserting document {} in {}", item, nitriteMap.getName());
            Document already = nitriteMap.putIfAbsent(nitriteId, item);

            if (already != null) {
                log.warn("Another document {} already exists with same id {}", already, nitriteId);
                // rollback changes
                nitriteMap.put(nitriteId, already);
                throw new UniqueConstraintException("id constraint violation, " +
                    "entry with same id already exists in " + nitriteMap.getName());
            } else {
                try {
                    indexOperations.writeIndex(item, nitriteId);
                } catch (UniqueConstraintException uce) {
                    log.error("Unique constraint violated for the document "
                        + document + " in " + nitriteMap.getName(), uce);
                    nitriteMap.remove(nitriteId);
                    throw uce;
                }
            }

            nitriteIds.add(nitriteId);

            Document eventDoc = item.clone();
            CollectionEventInfo<Document> eventInfo = new CollectionEventInfo<>();
            eventInfo.setItem(eventDoc);
            eventInfo.setTimestamp(time);
            eventInfo.setEventType(EventType.Insert);
            eventInfo.setOriginator(source);
            alert(EventType.Insert, eventInfo);
        }

        WriteResultImpl result = new WriteResultImpl();
        result.setNitriteIds(nitriteIds);

        log.debug("Returning write result {} for collection {}", result, nitriteMap.getName());
        return result;
    }

    WriteResult update(Filter filter, Document update, UpdateOptions updateOptions) {
        DocumentCursor cursor;
        if (filter == null || filter == Filter.ALL) {
            cursor = readOperations.find();
        } else {
            cursor = readOperations.find(filter);
        }

        WriteResultImpl writeResult = new WriteResultImpl();
        if (cursor == null || cursor.size() == 0) {
            log.debug("No document found to update by the filter {} in {}", filter, nitriteMap.getName());
            if (updateOptions.isInsertIfAbsent()) {
                return insert(update);
            } else {
                return writeResult;
            }
        } else {
            if (cursor.size() > 1 && updateOptions.isJustOnce()) {
                throw new InvalidOperationException("cannot update multiple items as justOnce is set to true");
            }

            update = update.clone();
            update.remove(DOC_ID);

            if (!REPLICATOR.contentEquals(update.getSource())) {
                update.remove(DOC_REVISION);
            }

            if (update.size() == 0) {
                alert(EventType.Update, new CollectionEventInfo<>());
                return writeResult;
            }

            log.debug("Filter {} found total {} document(s) to update with options {} in {}",
                filter, cursor.size(), updateOptions, nitriteMap.getName());

            for (final Document document : cursor) {
                if (document != null) {
                    Document item = document.clone();
                    Document oldDocument = document.clone();
                    String source = update.getSource();
                    long time = System.currentTimeMillis();

                    NitriteId nitriteId = item.getId();
                    log.debug("Document to update {} in {}", item, nitriteMap.getName());

                    if (!REPLICATOR.contentEquals(update.getSource())) {
                        update.remove(DOC_SOURCE);
                        item.merge(update);
                        int rev = item.getRevision();
                        item.put(DOC_REVISION, rev + 1);
                        item.put(DOC_MODIFIED, time);
                    } else {
                        update.remove(DOC_SOURCE);
                        item.merge(update);
                    }

                    nitriteMap.put(nitriteId, item);
                    log.debug("Document {} updated in {}", item, nitriteMap.getName());

                    // if 'update' only contains id value, affected count = 0
                    if (update.size() > 0) {
                        writeResult.addToList(nitriteId);
                    }

                    indexOperations.updateIndex(oldDocument, item, nitriteId);

                    CollectionEventInfo<Document> eventInfo = new CollectionEventInfo<>();
                    Document eventDoc = item.clone();
                    eventInfo.setItem(eventDoc);
                    eventInfo.setEventType(EventType.Update);
                    eventInfo.setTimestamp(time);
                    eventInfo.setOriginator(source);
                    alert(EventType.Update, eventInfo);
                }
            }
        }

        log.debug("Returning write result {} for collection {}", writeResult, nitriteMap.getName());
        return writeResult;
    }

    WriteResult remove(Filter filter, boolean justOnce) {
        DocumentCursor cursor;
        if (filter == null || filter == Filter.ALL) {
            cursor = readOperations.find();
        } else {
            cursor = readOperations.find(filter);
        }

        WriteResultImpl result = new WriteResultImpl();
        if (cursor == null || cursor.size() == 0) {
            log.debug("No document found to remove by the filter {} in {}", filter, nitriteMap.getName());
            return result;
        }

        log.debug("Filter {} found total {} document(s) to remove with options {} from {}",
            filter, cursor.size(), justOnce, nitriteMap.getName());

        for (Document document : cursor) {
            Document item = document.clone();
            CollectionEventInfo<Document> eventInfo = removeAndCreateEvent(item, result);
            if (eventInfo != null) {
                alert(EventType.Remove, eventInfo);
            }

            if (justOnce) {
                return result;
            }
        }

        log.debug("Returning write result {} for collection {}", result, nitriteMap.getName());
        return result;
    }

    public WriteResult remove(Document document) {
        WriteResultImpl result = new WriteResultImpl();
        CollectionEventInfo<Document> eventInfo = removeAndCreateEvent(document, result);
        if (eventInfo != null) {
            eventInfo.setOriginator(document.getSource());
            alert(EventType.Remove, eventInfo);
        }
        return result;
    }

    private CollectionEventInfo<Document> removeAndCreateEvent(Document document, WriteResultImpl writeResult) {
        NitriteId nitriteId = document.getId();
        document = nitriteMap.remove(nitriteId);
        if (document != null) {
            long time = System.currentTimeMillis();
            indexOperations.removeIndex(document, nitriteId);
            writeResult.addToList(nitriteId);

            int rev = document.getRevision();
            document.put(DOC_REVISION, rev + 1);
            document.put(DOC_MODIFIED, time);

            log.debug("Document removed {} from {}", document, nitriteMap.getName());

            CollectionEventInfo<Document> eventInfo = new CollectionEventInfo<>();
            Document eventDoc = document.clone();
            eventInfo.setItem(eventDoc);
            eventInfo.setEventType(EventType.Remove);
            eventInfo.setTimestamp(time);
            return eventInfo;
        }
        return null;
    }

    private void alert(EventType action, CollectionEventInfo<?> changedItem) {
        log.debug("Notifying {} event for item {} from {}", action, changedItem, nitriteMap.getName());
        if (eventBus != null) {
            eventBus.post(changedItem);
        }
    }
}
