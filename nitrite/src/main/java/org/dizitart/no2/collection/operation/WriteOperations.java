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
import org.dizitart.no2.common.processors.ProcessorChain;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.exceptions.UniqueConstraintException;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.store.NitriteMap;

import java.util.ArrayList;
import java.util.List;

import static org.dizitart.no2.common.Constants.*;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
class WriteOperations {
    private final DocumentIndexWriter documentIndexWriter;
    private final ReadOperations readOperations;
    private final EventBus<CollectionEventInfo<?>, CollectionEventListener> eventBus;
    private final NitriteMap<NitriteId, Document> nitriteMap;
    private final ProcessorChain processorChain;

    WriteOperations(DocumentIndexWriter documentIndexWriter,
                    ReadOperations readOperations,
                    NitriteMap<NitriteId, Document> nitriteMap,
                    EventBus<CollectionEventInfo<?>, CollectionEventListener> eventBus,
                    ProcessorChain processorChain) {
        this.documentIndexWriter = documentIndexWriter;
        this.readOperations = readOperations;
        this.eventBus = eventBus;
        this.nitriteMap = nitriteMap;
        this.processorChain = processorChain;
    }

    WriteResult insert(Document... documents) {
        List<NitriteId> nitriteIds = new ArrayList<>(documents.length);
        log.debug("Total {} document(s) to be inserted in {}", documents.length, nitriteMap.getName());

        for (Document document : documents) {
            Document newDoc = document.clone();
            NitriteId nitriteId = newDoc.getId();
            String source = newDoc.getSource();
            long time = System.currentTimeMillis();

            if (!REPLICATOR.contentEquals(newDoc.getSource())) {
                // if replicator is not inserting the document that means
                // it is being inserted by user, so update metadata
                newDoc.remove(DOC_SOURCE);
                newDoc.put(DOC_REVISION, 1);
                newDoc.put(DOC_MODIFIED, time);
            } else {
                // if replicator is inserting the document, remove the source
                // but keep the revision intact
                newDoc.remove(DOC_SOURCE);
            }

            // run processors
            Document unprocessed = newDoc.clone();
            Document processed = processorChain.processBeforeWrite(unprocessed);
            log.debug("Document processed from {} to {} before insert", newDoc, processed);

            log.debug("Inserting processed document {} in {}", processed, nitriteMap.getName());
            Document already = nitriteMap.putIfAbsent(nitriteId, processed);

            if (already != null) {
                log.warn("Another document {} already exists with same id {}", already, nitriteId);

                throw new UniqueConstraintException("id constraint violation, " +
                    "entry with same id already exists in " + nitriteMap.getName());
            } else {
                try {
                    documentIndexWriter.writeIndexEntry(processed);
                } catch (UniqueConstraintException | IndexingException e) {
                    log.error("Index operation has failed during insertion for the document "
                        + document + " in " + nitriteMap.getName(), e);
                    nitriteMap.remove(nitriteId);
                    throw e;
                }
            }

            nitriteIds.add(nitriteId);

            CollectionEventInfo<Document> eventInfo = new CollectionEventInfo<>();
            eventInfo.setItem(newDoc);
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
        DocumentCursor cursor = readOperations.find(filter, null);

        WriteResultImpl writeResult = new WriteResultImpl();
        Document document = update.clone();
        document.remove(DOC_ID);

        if (!REPLICATOR.contentEquals(document.getSource())) {
            document.remove(DOC_REVISION);
        }

        if (document.size() == 0) {
            alert(EventType.Update, new CollectionEventInfo<>());
            return writeResult;
        }

        long count = 0;
        for (Document doc : cursor) {
            if (doc != null) {
                count++;

                if (count > 1 && updateOptions.isJustOnce()) {
                    break;
                }

                Document newDoc = doc.clone();
                Document oldDocument = doc.clone();
                String source = document.getSource();
                long time = System.currentTimeMillis();

                NitriteId nitriteId = newDoc.getId();
                log.debug("Document to update {} in {}", newDoc, nitriteMap.getName());

                if (!REPLICATOR.contentEquals(document.getSource())) {
                    document.remove(DOC_SOURCE);
                    newDoc.merge(document);
                    int rev = newDoc.getRevision();
                    newDoc.put(DOC_REVISION, rev + 1);
                    newDoc.put(DOC_MODIFIED, time);
                } else {
                    document.remove(DOC_SOURCE);
                    newDoc.merge(document);
                }

                // run processor
                Document unprocessed = newDoc.clone();
                Document processed = processorChain.processBeforeWrite(unprocessed);
                log.debug("Document processed from {} to {} before update", newDoc, processed);

                nitriteMap.put(nitriteId, processed);
                log.debug("Document {} updated in {}", processed, nitriteMap.getName());

                // if 'update' only contains id value, affected count = 0
                if (document.size() > 0) {
                    writeResult.addToList(nitriteId);
                }

                try {
                    documentIndexWriter.updateIndexEntry(oldDocument, processed);
                } catch (UniqueConstraintException | IndexingException e) {
                    log.error("Index operation failed during update, reverting changes for the document "
                        + oldDocument + " in " + nitriteMap.getName(), e);
                    nitriteMap.put(nitriteId, oldDocument);
                    documentIndexWriter.updateIndexEntry(processed, oldDocument);
                    throw e;
                }

                CollectionEventInfo<Document> eventInfo = new CollectionEventInfo<>();
                eventInfo.setItem(newDoc);
                eventInfo.setEventType(EventType.Update);
                eventInfo.setTimestamp(time);
                eventInfo.setOriginator(source);
                alert(EventType.Update, eventInfo);
            }
        }

        if (count == 0) {
            log.debug("No document found to update by the filter {} in {}", filter, nitriteMap.getName());
            if (updateOptions.isInsertIfAbsent()) {
                return insert(update);
            } else {
                return writeResult;
            }
        }

        log.debug("Filter {} updated total {} document(s) with options {} in {}",
            filter, count, updateOptions, nitriteMap.getName());

        log.debug("Returning write result {} for collection {}", writeResult, nitriteMap.getName());
        return writeResult;
    }

    WriteResult remove(Filter filter, boolean justOnce) {
        DocumentCursor cursor = readOperations.find(filter, null);
        WriteResultImpl result = new WriteResultImpl();

        long count = 0;
        for (Document document : cursor) {
            if (document != null) {
                count++;

                // run processor
                Document unprocessed = document.clone();
                Document processed = processorChain.processAfterRead(unprocessed);
                log.debug("Document processed from {} to {} after remove", document, processed);

                CollectionEventInfo<Document> eventInfo = removeAndCreateEvent(processed, result);
                if (eventInfo != null) {
                    alert(EventType.Remove, eventInfo);
                }

                if (justOnce) {
                    break;
                }
            }
        }

        if (count == 0) {
            log.debug("No document found to remove by the filter {} in {}", filter, nitriteMap.getName());
            return result;
        }

        log.debug("Filter {} removed total {} document(s) with options {} from {}",
            filter, count, justOnce, nitriteMap.getName());

        log.debug("Returning write result {} for collection {}", result, nitriteMap.getName());
        return result;
    }

    WriteResult remove(Document document) {
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
            documentIndexWriter.removeIndexEntry(document);
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
