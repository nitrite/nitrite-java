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

package org.dizitart.no2.collection;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteContext;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.operation.CollectionOperation;
import org.dizitart.no2.event.ChangeInfo;
import org.dizitart.no2.event.ChangeListener;
import org.dizitart.no2.event.ChangeType;
import org.dizitart.no2.event.EventBus;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.NotIdentifiableException;
import org.dizitart.no2.index.Index;
import org.dizitart.no2.meta.Attributes;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.util.Collection;

import static org.dizitart.no2.common.Constants.DOC_ID;
import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.*;
import static org.dizitart.no2.util.DocumentUtils.createUniqueFilter;
import static org.dizitart.no2.util.ValidationUtils.notNull;

/**
 * The default implementation of {@link NitriteCollection}.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 * */
class DefaultNitriteCollection implements NitriteCollection {
    private NitriteMap<NitriteId, Document> nitriteMap;
    private NitriteStore nitriteStore;
    private CollectionOperation collectionOperation;
    private volatile boolean isDropped;
    private EventBus<ChangeInfo, ChangeListener> eventBus;
    private String collectionName;

    DefaultNitriteCollection(NitriteMap<NitriteId, Document> nitriteMap, NitriteContext nitriteContext) {
        this.nitriteMap = nitriteMap;
        this.nitriteStore = nitriteMap.getStore();
        this.eventBus = new ChangeEventBus();
        this.collectionOperation = new CollectionOperation(nitriteMap, nitriteContext, eventBus);
        this.isDropped = false;
        this.collectionName = nitriteMap.getName();
    }

    @Override
    public void createIndex(String field, IndexOptions indexOptions) {
        checkOpened();
        try {
            // by default async is false while creating index
            if (indexOptions == null) {
                collectionOperation.createIndex(field, IndexType.Unique, false);
            } else {
                collectionOperation.createIndex(field, indexOptions.getIndexType(),
                        indexOptions.isAsync());
            }
        } catch (VirtualMachineError vme) {
            handleVirtualMachineError(vme);
        }
    }

    @Override
    public void rebuildIndex(String field, boolean async) {
        checkOpened();
        try {
            Index index = collectionOperation.findIndex(field);
            if (index != null) {
                validateRebuildIndex(index);
                collectionOperation.rebuildIndex(index, async);
            } else {
                throw new IndexingException(errorMessage(field + " is not indexed",
                        IE_REBUILD_INDEX_FIELD_NOT_INDEXED));
            }
        } catch (VirtualMachineError vme) {
            handleVirtualMachineError(vme);
        }
    }

    @Override
    public Collection<Index> listIndices() {
        checkOpened();
        try {
            return collectionOperation.listIndexes();
        } catch (VirtualMachineError vme) {
            handleVirtualMachineError(vme);
        }
        return null;
    }

    @Override
    public boolean hasIndex(String field) {
        checkOpened();
        try {
            return collectionOperation.hasIndex(field);
        } catch (VirtualMachineError vme) {
            handleVirtualMachineError(vme);
        }
        return false;
    }

    @Override
    public boolean isIndexing(String field) {
        checkOpened();
        try {
            return collectionOperation.isIndexing(field);
        } catch (VirtualMachineError vme) {
            handleVirtualMachineError(vme);
        }
        return false;
    }

    @Override
    public void dropIndex(String field) {
        checkOpened();
        try {
            collectionOperation.dropIndex(field);
        } catch (VirtualMachineError vme) {
            handleVirtualMachineError(vme);
        }
    }

    @Override
    public void dropAllIndices() {
        checkOpened();
        try {
            collectionOperation.dropAllIndices();
        } catch (VirtualMachineError vme) {
            handleVirtualMachineError(vme);
        }
    }

    @Override
    public WriteResult insert(Document document, Document... documents) {
        checkOpened();
        try {
            return collectionOperation.insert(document, documents);
        } catch (VirtualMachineError vme) {
            handleVirtualMachineError(vme);
        }
        return null;
    }

    @Override
    public WriteResult insert(Document[] documents) {
        checkOpened();
        try {
            return collectionOperation.insert(documents);
        } catch (VirtualMachineError vme) {
            handleVirtualMachineError(vme);
        }
        return null;
    }

    @Override
    public Cursor find(Filter filter) {
        checkOpened();
        try {
            return collectionOperation.find(filter);
        } catch (VirtualMachineError vme) {
            handleVirtualMachineError(vme);
        }
        return null;
    }

    @Override
    public Cursor find(FindOptions findOptions) {
        checkOpened();
        try {
            return collectionOperation.find(findOptions);
        } catch (VirtualMachineError vme) {
            handleVirtualMachineError(vme);
        }
        return null;
    }

    @Override
    public Cursor find(Filter filter, FindOptions findOptions) {
        checkOpened();
        try {
            return collectionOperation.find(filter, findOptions);
        } catch (VirtualMachineError vme) {
            handleVirtualMachineError(vme);
        }
        return null;
    }

    @Override
    public Cursor find() {
        checkOpened();
        try {
            return collectionOperation.find();
        } catch (VirtualMachineError vme) {
            handleVirtualMachineError(vme);
        }
        return null;
    }

    @Override
    public Document getById(NitriteId nitriteId) {
        checkOpened();
        try {
            return collectionOperation.getById(nitriteId);
        } catch (VirtualMachineError vme) {
            handleVirtualMachineError(vme);
        }
        return null;
    }

    @Override
    public void drop() {
        checkOpened();
        try {
            collectionOperation.dropCollection();
            isDropped = true;
            closeCollection();
            eventBus.post(new ChangeInfo(ChangeType.DROP));
            closeEventBus();
        } catch (VirtualMachineError vme) {
            handleVirtualMachineError(vme);
        }
    }

    @Override
    public boolean isDropped() {
        return isDropped;
    }

    @Override
    public boolean isClosed() {
        if (nitriteStore == null || nitriteStore.isClosed() || isDropped) {
            closeCollection();
            closeEventBus();
            return true;
        }
        else return false;
    }

    @Override
    public void close() {
        closeCollection();
        eventBus.post(new ChangeInfo(ChangeType.CLOSE));
        closeEventBus();
    }

    @Override
    public String getName() {
        return collectionName;
    }

    @Override
    public long size() {
        checkOpened();
        try {
            return nitriteMap.sizeAsLong();
        } catch (VirtualMachineError vme) {
            handleVirtualMachineError(vme);
        }
        return 0;
    }

    @Override
    public WriteResult update(Document document) {
        checkOpened();
        try {
            if (document.containsKey(DOC_ID)) {
                return update(createUniqueFilter(document), document);
            } else {
                throw new NotIdentifiableException(UPDATE_FAILED_AS_NO_ID_FOUND);
            }
        } catch (VirtualMachineError vme) {
            handleVirtualMachineError(vme);
        }
        return null;
    }

    @Override
    public WriteResult update(Document document, boolean upsert) {
        checkOpened();
        try {
            return update(createUniqueFilter(document), document, UpdateOptions.updateOptions(upsert));
        } catch (VirtualMachineError vme) {
            handleVirtualMachineError(vme);
        }
        return null;
    }

    @Override
    public WriteResult update(Filter filter, Document update) {
        checkOpened();
        try {
            return update(filter, update, new UpdateOptions());
        } catch (VirtualMachineError vme) {
            handleVirtualMachineError(vme);
        }
        return null;
    }

    @Override
    public WriteResult update(Filter filter, Document update, UpdateOptions updateOptions) {
        checkOpened();
        try {
            return collectionOperation.update(filter, update, updateOptions);
        } catch (VirtualMachineError vme) {
            handleVirtualMachineError(vme);
        }
        return null;
    }

    @Override
    public WriteResult remove(Document document) {
        checkOpened();
        try {
            notNull(document, errorMessage("document can not be null", VE_NC_REMOVE_NULL_DOCUMENT));
            if (document.containsKey(DOC_ID)) {
                return remove(createUniqueFilter(document));
            } else {
                throw new NotIdentifiableException(REMOVE_FAILED_AS_NO_ID_FOUND);
            }
        } catch (VirtualMachineError vme) {
            handleVirtualMachineError(vme);
        }
        return null;
    }

    @Override
    public WriteResult remove(Filter filter) {
        checkOpened();
        try {
            return remove(filter, new RemoveOptions());
        } catch (VirtualMachineError vme) {
            handleVirtualMachineError(vme);
        }
        return null;
    }

    @Override
    public WriteResult remove(Filter filter, RemoveOptions removeOptions) {
        checkOpened();
        try {
            return collectionOperation.remove(filter, removeOptions);
        } catch (VirtualMachineError vme) {
            handleVirtualMachineError(vme);
        }
        return null;
    }

    @Override
    public void register(ChangeListener listener) {
        if (eventBus == null && !isClosed() && !isDropped()) {
            eventBus = new ChangeEventBus();
        }
        eventBus.register(listener);
    }

    @Override
    public void deregister(ChangeListener listener) {
        if (eventBus != null) {
            eventBus.deregister(listener);
        }
    }

    @Override
    public Attributes getAttributes() {
        return nitriteMap != null ? nitriteMap.getAttributes() : null;
    }

    @Override
    public void setAttributes(Attributes attributes) {
        nitriteMap.setAttributes(attributes);
    }

    private void checkOpened() {
        if (nitriteStore == null || nitriteStore.isClosed()) {
            throw new NitriteIOException(STORE_IS_CLOSED);
        }

        if (isDropped) {
            throw new NitriteIOException(COLLECTION_IS_DROPPED);
        }
    }

    private void validateRebuildIndex(Index index) {
        notNull(index, errorMessage("index can not be null", VE_NC_REBUILD_INDEX_NULL_INDEX));
        if (!hasIndex(index.getField())) {
            throw new IndexingException(errorMessage(index + " does not exists for " +
                    "collection " + nitriteMap.getName(), IE_REBUILD_INDEX_DOES_NOT_EXISTS));
        }

        if (isIndexing(index.getField())) {
            throw new IndexingException(errorMessage("indexing on value " + index.getField() +
                    " is currently running", IE_VALIDATE_REBUILD_INDEX_RUNNING));
        }
    }

    private void handleVirtualMachineError(VirtualMachineError vme) {
        if (nitriteStore != null) {
            // if there is any fatal error, close store immediately
            nitriteStore.closeImmediately();
            close();
        }
        if (vme != null) {
            throw vme;
        }
    }

    private void closeCollection() {
        nitriteStore = null;
        nitriteMap = null;
        collectionOperation = null;
    }

    private void closeEventBus() {
        if (eventBus != null) {
            eventBus.close();
        }
        eventBus = null;
    }
}
