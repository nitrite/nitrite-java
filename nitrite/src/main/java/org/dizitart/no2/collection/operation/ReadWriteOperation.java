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

package org.dizitart.no2.collection.operation;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.Cursor;
import org.dizitart.no2.collection.Filter;
import org.dizitart.no2.collection.RemoveOptions;
import org.dizitart.no2.collection.UpdateOptions;
import org.dizitart.no2.event.*;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.UniqueConstraintException;
import org.dizitart.no2.store.NitriteMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.dizitart.no2.common.Constants.*;
import static org.dizitart.no2.exceptions.ErrorCodes.UCE_CONSTRAINT_VIOLATED;
import static org.dizitart.no2.exceptions.ErrorMessage.OBJ_MULTI_UPDATE_WITH_JUST_ONCE;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

/**
 *
 * @author Anindya Chatterjee
 * */
@Slf4j
class ReadWriteOperation {
    private final IndexTemplate indexTemplate;
    private final QueryTemplate queryTemplate;
    private final NitriteMap<NitriteId, Document> underlyingMap;
    private final EventBus<ChangeInfo, ChangeListener> eventBus;
    private final String name;

    private final Object lock = new Object();

    ReadWriteOperation(IndexTemplate indexTemplate,
                       QueryTemplate queryTemplate,
                       NitriteMap<NitriteId, Document> mapStore,
                       EventBus<ChangeInfo, ChangeListener> eventBus) {
        this.indexTemplate = indexTemplate;
        this.queryTemplate = queryTemplate;
        this.underlyingMap = mapStore;
        this.eventBus = eventBus;
        this.name = underlyingMap.getName();
    }

    WriteResultImpl insert(Document... documents) {
        List<NitriteId> nitriteIdList = new ArrayList<>();
        List<ChangedItem> changedItems = new ArrayList<>();

        log.debug("Total " + documents.length + " document(s) to be inserted in " + name);

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
                Document already = underlyingMap.putIfAbsent(nitriteId, document);
                log.debug("Inserting document " + document + " in " + name);

                if (already != null) {
                    // rollback changes
                    underlyingMap.put(nitriteId, already);
                    log.debug("Another document already exists with id " + nitriteId);
                    throw new UniqueConstraintException(errorMessage("id constraint violation, " +
                            "entry with same id already exists in " + name, UCE_CONSTRAINT_VIOLATED));
                } else {
                    try {
                        indexTemplate.updateIndexEntry(document, nitriteId);
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

        log.debug("Returning write result " + result + " for collection " + name);
        return result;
    }

    WriteResultImpl update(Filter filter, Document update, UpdateOptions updateOptions) {
        Cursor cursor;
        if (filter == null) {
            cursor = queryTemplate.find();
        } else {
            cursor = queryTemplate.find(filter);
        }

        WriteResultImpl writeResult = new WriteResultImpl();
        if (cursor == null || cursor.size() == 0) {
            log.debug("No document found to update by the filter " + filter + " in " + name);
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

            log.debug("Filter " + filter + " found total " + cursor.size()
                    + " document(s) to update with options " + updateOptions + " in " + name);

            List<ChangedItem> changedItems = new ArrayList<>();
            for(final Document document : cursor) {
                if (document != null) {
                    NitriteId nitriteId = document.getId();

                    synchronized (lock) {
                        Document oldDocument = new Document(document);

                        log.debug("Document to update " + document + " in " + name);

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

                        underlyingMap.put(nitriteId, document);
                        log.debug("Document " + document + " updated in " + name);

                        // if 'update' only contains id value, affected count = 0
                        if (update.size() > 0) {
                            writeResult.addToList(nitriteId);
                        }

                        indexTemplate.refreshIndexEntry(oldDocument, document, nitriteId);
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

        log.debug("Returning write result " + writeResult + " for collection " + name);
        return writeResult;
    }

    WriteResultImpl remove(Filter filter, RemoveOptions removeOptions) {
        Cursor cursor;
        if (filter == null) {
            cursor = queryTemplate.find();
        } else {
            cursor = queryTemplate.find(filter);
        }

        WriteResultImpl result = new WriteResultImpl();
        if (cursor == null) {
            log.debug("No document found to remove by the filter " + filter + " in " + name);
            return result;
        }

        log.debug("Filter " + filter + " found total " + cursor.size()
                + " document(s) to remove with options " + removeOptions + " from " + name);

        List<ChangedItem> changedItems = new ArrayList<>();

        synchronized (lock) {
            for (Document document : cursor) {
                NitriteId nitriteId = document.getId();
                indexTemplate.removeIndexEntry(document, nitriteId);

                Document removed = underlyingMap.remove(nitriteId);
                int rev = removed.getRevision();
                removed.put(DOC_REVISION, rev + 1);
                removed.put(DOC_MODIFIED, System.currentTimeMillis());

                log.debug("Document removed " + removed + " from " + name);

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

        log.debug("Returning write result " + result + " for collection " + name);
        return result;
    }

    private void notify(ChangeType action, Collection<ChangedItem> changedItems) {
        log.debug("Notifying " + action + " event for items " + changedItems + " from " + name);
        if (eventBus != null) {
            ChangeInfo changeInfo = new ChangeInfo(action);
            changeInfo.setChangedItems(changedItems);
            eventBus.post(changeInfo);
        }
    }
}
