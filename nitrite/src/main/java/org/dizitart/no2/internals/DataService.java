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

package org.dizitart.no2.internals;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.*;
import org.dizitart.no2.event.*;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.UniqueConstraintException;
import org.dizitart.no2.store.NitriteMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.dizitart.no2.Constants.*;
import static org.dizitart.no2.exceptions.ErrorCodes.UCE_CONSTRAINT_VIOLATED;
import static org.dizitart.no2.exceptions.ErrorMessage.OBJ_MULTI_UPDATE_WITH_JUST_ONCE;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

/**
 *
 * @author Anindya Chatterjee
 * */
@Slf4j
class DataService {
    private final IndexingService indexingService;
    private final SearchService searchService;
    private final NitriteMap<NitriteId, Document> underlyingMap;
    private final EventBus<ChangeInfo, ChangeListener> eventBus;
    private final String name;

    private final Object lock = new Object();

    DataService(IndexingService indexingService, SearchService searchService,
                NitriteMap<NitriteId, Document> mapStore,
                EventBus<ChangeInfo, ChangeListener> eventBus) {
        this.indexingService = indexingService;
        this.searchService = searchService;
        this.underlyingMap = mapStore;
        this.eventBus = eventBus;
        this.name = underlyingMap.getName();
    }

    WriteResultImpl insert(Document... documents) {
        List<NitriteId> nitriteIdList = new ArrayList<>(documents.length);
        List<ChangedItem> changedItems = new ArrayList<>(documents.length);

        log.debug("Total {} document(s) to be inserted in {}", documents.length, name);

        for (Document document : documents) {
            NitriteId nitriteId = document.getId();

            if (!REPLICATOR.contentEquals(document.getSource())) {
                // if replicator is not inserting the document that means
                // it is being inserted by user, so update metadata
                document.remove(DOC_SOURCE);
                document.put(DOC_REVISION, 1);
                document.put(DOC_MODIFIED, System.currentTimeMillis());
            } else {
                // if replicator is inserting the document, remove the source
                // but keep the revision intact
                document.remove(DOC_SOURCE);
            }

            synchronized (lock) {
                Document item = new Document(document);
                Document already = underlyingMap.putIfAbsent(nitriteId, item);
                log.debug("Inserting document {} in {}", document, name);

                if (already != null) {
                    // rollback changes
                    underlyingMap.put(nitriteId, already);
                    log.debug("Another document already exists with id {}", nitriteId);
                    throw new UniqueConstraintException(errorMessage("id constraint violation, " +
                            "entry with same id already exists in " + name, UCE_CONSTRAINT_VIOLATED));
                } else {
                    try {
                        indexingService.updateIndexEntry(item, nitriteId);
                    } catch (UniqueConstraintException uce) {
                        log.error("Unique constraint violated for the document "
                                + document + " in " + name, uce);
                        underlyingMap.remove(nitriteId);
                        throw uce;
                    }
                }
            }

            nitriteIdList.add(nitriteId);

            ChangedItem changedItem = new ChangedItem();
            changedItem.setDocument(document);
            changedItem.setChangeTimestamp(document.getLastModifiedTime());
            changedItem.setChangeType(ChangeType.INSERT);
            changedItems.add(changedItem);
        }

        notify(ChangeType.INSERT, changedItems);

        WriteResultImpl result = new WriteResultImpl();
        result.setNitriteIdList(nitriteIdList);

        log.debug("Returning write result {}  for collection {}", result, name);
        return result;
    }

    WriteResultImpl update(Filter filter, Document update, UpdateOptions updateOptions) {
        Cursor cursor;
        if (filter == null) {
            cursor = searchService.find();
        } else {
            cursor = searchService.find(filter);
        }

        WriteResultImpl writeResult = new WriteResultImpl();
        if (cursor == null || cursor.size() == 0) {
            log.debug("No document found to update by the filter {}  in  {}", filter, name);
            if (updateOptions.isUpsert()) {
                return insert(update);
            } else {
                return writeResult;
            }
        } else {
            if (cursor.size() > 1 && updateOptions.isJustOnce()) {
                throw new InvalidOperationException(OBJ_MULTI_UPDATE_WITH_JUST_ONCE);
            }

            update = new Document(update);
            update.remove(DOC_ID);

            if (!REPLICATOR.contentEquals(update.getSource())) {
                update.remove(DOC_REVISION);
            }

            if (update.size() == 0) {
                notify(ChangeType.UPDATE, null);
                return writeResult;
            }

            log.debug("Filter {} found total {} document(s) to update with options {} in {}", filter, cursor.size(), updateOptions, name);

            List<ChangedItem> changedItems = new ArrayList<>(cursor.size());
            for(final Document document : cursor) {
                if (document != null) {
                    NitriteId nitriteId = document.getId();

                    synchronized (lock) {
                        Document oldDocument = new Document(document);

                        log.debug("Document to update {} in {}", document, name);

                        if (!REPLICATOR.contentEquals(update.getSource())) {
                            update.remove(DOC_SOURCE);
                            document.putAll(update);
                            int rev = document.getRevision();
                            document.put(DOC_REVISION, rev + 1);
                            document.put(DOC_MODIFIED, System.currentTimeMillis());
                        } else {
                            update.remove(DOC_SOURCE);
                            document.putAll(update);
                        }

                        Document item = new Document(document);
                        underlyingMap.put(nitriteId, item);
                        log.debug("Document {} updated in {}", document, name);

                        // if 'update' only contains id value, affected count = 0
                        if (update.size() > 0) {
                            writeResult.addToList(nitriteId);
                        }

                        indexingService.refreshIndexEntry(oldDocument, item, nitriteId);
                    }

                    ChangedItem changedItem = new ChangedItem();
                    changedItem.setDocument(document);
                    changedItem.setChangeType(ChangeType.UPDATE);
                    changedItem.setChangeTimestamp(document.getLastModifiedTime());
                    changedItems.add(changedItem);
                }
            }

            notify(ChangeType.UPDATE, changedItems);
        }

        log.debug("Returning write result {} for collection {}", writeResult, name);
        return writeResult;
    }

    WriteResultImpl remove(Filter filter, RemoveOptions removeOptions) {
        Cursor cursor;
        if (filter == null) {
            cursor = searchService.find();
        } else {
            cursor = searchService.find(filter);
        }

        WriteResultImpl result = new WriteResultImpl();
        if (cursor == null) {
            log.debug("No document found to remove by the filter {} in {}", filter, name);
            return result;
        }

        log.debug("Filter {} found total {} document(s) to remove with options {} from {}", filter, cursor.size(), removeOptions, name);

        List<ChangedItem> changedItems = new ArrayList<>(cursor.size());

        synchronized (lock) {
            for (Document document : cursor) {
                NitriteId nitriteId = document.getId();
                indexingService.removeIndexEntry(document, nitriteId);

                Document removed = underlyingMap.remove(nitriteId);
                int rev = removed.getRevision();
                removed.put(DOC_REVISION, rev + 1);
                removed.put(DOC_MODIFIED, System.currentTimeMillis());

                log.debug("Document removed {} from {}", removed, name);

                result.addToList(nitriteId);

                ChangedItem changedItem = new ChangedItem();
                changedItem.setDocument(removed);
                changedItem.setChangeType(ChangeType.REMOVE);
                changedItem.setChangeTimestamp(removed.getLastModifiedTime());
                changedItems.add(changedItem);

                if (removeOptions.isJustOne()) {
                    notify(ChangeType.REMOVE, changedItems);
                    return result;
                }
            }
        }

        notify(ChangeType.REMOVE, changedItems);

        log.debug("Returning write result {} for collection {}", result, name);
        return result;
    }

    Document getById(NitriteId nitriteId) {
        return underlyingMap.get(nitriteId);
    }

    private void notify(ChangeType action, Collection<ChangedItem> changedItems) {
        log.debug("Notifying {} event for items {} from {}", action, changedItems, name);
        if (eventBus != null) {
            ChangeInfo changeInfo = new ChangeInfo(action);
            changeInfo.setChangedItems(changedItems);
            eventBus.post(changeInfo);
        }
    }
}
