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

package org.dizitart.no2.repository;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.filters.NitriteFilter;
import org.dizitart.no2.index.IndexEntry;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.store.NitriteStore;

import java.util.Collection;

import static org.dizitart.no2.collection.UpdateOptions.updateOptions;
import static org.dizitart.no2.common.util.ValidationUtils.containsNull;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * @author Anindya Chatterjee
 */
class DefaultObjectRepository<T> implements ObjectRepository<T> {
    private final NitriteCollection collection;
    private final Class<T> type;
    private NitriteMapper nitriteMapper;
    private RepositoryOperations operations;

    DefaultObjectRepository(Class<T> type,
                            NitriteCollection collection,
                            NitriteConfig nitriteConfig) {
        this.type = type;
        this.collection = collection;
        init(nitriteConfig);
    }

    @Override
    public void createIndex(String field, IndexOptions indexOptions) {
        collection.createIndex(field, indexOptions);
    }

    @Override
    public void rebuildIndex(String field, boolean isAsync) {
        collection.rebuildIndex(field, isAsync);
    }

    @Override
    public Collection<IndexEntry> listIndices() {
        return collection.listIndices();
    }

    @Override
    public boolean hasIndex(String field) {
        return collection.hasIndex(field);
    }

    @Override
    public boolean isIndexing(String field) {
        return collection.isIndexing(field);
    }

    @Override
    public void dropIndex(String field) {
        collection.dropIndex(field);
    }

    @Override
    public void dropAllIndices() {
        collection.dropAllIndices();
    }

    @Override
    public WriteResult insert(T[] elements) {
        notNull(elements, "a null object cannot be inserted");
        containsNull(elements, "a null object cannot be inserted");
        return collection.insert(operations.toDocuments(elements));
    }

    @Override
    public WriteResult update(T element, boolean insertIfAbsent) {
        notNull(element, "a null object cannot be used for update");
        return update(operations.createUniqueFilter(element), element, insertIfAbsent);
    }

    @Override
    public WriteResult update(Filter filter, T update, boolean insertIfAbsent) {
        notNull(update, "a null object cannot be used for update");
        Document updateDocument = operations.toDocument(update, true);
        if (!insertIfAbsent) {
            operations.removeNitriteId(updateDocument);
        }
        return collection.update(asObjectFilter(filter), updateDocument, updateOptions(insertIfAbsent, true));
    }

    @Override
    public WriteResult update(Filter filter, Document update, boolean justOnce) {
        notNull(update, "a null document cannot be used for update");
        operations.removeNitriteId(update);
        operations.serializeFields(update);
        return collection.update(asObjectFilter(filter), update, updateOptions(false, justOnce));
    }

    @Override
    public WriteResult remove(T element) {
        notNull(element, "a null object cannot be removed");
        return remove(operations.createUniqueFilter(element));
    }

    @Override
    public WriteResult remove(Filter filter, boolean justOne) {
        return collection.remove(asObjectFilter(filter), justOne);
    }

    @Override
    public Cursor<T> find() {
        return new ObjectCursor<>(nitriteMapper, collection.find(), type);
    }

    @Override
    public Cursor<T> find(Filter filter) {
        return new ObjectCursor<>(nitriteMapper, collection.find(asObjectFilter(filter)), type);
    }

    @Override
    public <I> T getById(I id) {
        Filter idFilter = operations.createIdFilter(id);
        return find(idFilter).firstOrNull();
    }

    @Override
    public void drop() {
        collection.drop();
    }

    @Override
    public boolean isDropped() {
        return collection.isDropped();
    }

    @Override
    public boolean isOpen() {
        return collection.isOpen();
    }

    @Override
    public void close() {
        collection.close();
    }

    @Override
    public long size() {
        return collection.size();
    }

    @Override
    public NitriteStore getStore() {
        return collection.getStore();
    }

    @Override
    public void subscribe(CollectionEventListener listener) {
        collection.subscribe(listener);
    }

    @Override
    public void unsubscribe(CollectionEventListener listener) {
        collection.unsubscribe(listener);
    }

    @Override
    public Attributes getAttributes() {
        return collection.getAttributes();
    }

    @Override
    public void setAttributes(Attributes attributes) {
        collection.setAttributes(attributes);
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public NitriteCollection getDocumentCollection() {
        return collection;
    }

    private void init(NitriteConfig nitriteConfig) {
        nitriteMapper = nitriteConfig.nitriteMapper();
        operations = new RepositoryOperations(type, nitriteMapper, collection);
        operations.createIndexes();
    }

    private Filter asObjectFilter(Filter filter) {
        if (filter instanceof NitriteFilter) {
            NitriteFilter nitriteFilter = (NitriteFilter) filter;
            nitriteFilter.setObjectFilter(true);
            return nitriteFilter;
        }
        return filter;
    }
}
