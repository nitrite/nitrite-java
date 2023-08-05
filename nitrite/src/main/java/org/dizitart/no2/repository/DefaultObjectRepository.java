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
import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.UpdateOptions;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.common.meta.Attributes;
import org.dizitart.no2.common.processors.Processor;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.IndexOptions;
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
    private final NitriteConfig nitriteConfig;
    private Class<T> type;
    private EntityDecorator<T> entityDecorator;
    private RepositoryOperations operations;

    DefaultObjectRepository(Class<T> type,
                            NitriteCollection collection,
                            NitriteConfig nitriteConfig) {
        this.type = type;
        this.collection = collection;
        this.nitriteConfig = nitriteConfig;
        initialize();
    }

    DefaultObjectRepository(EntityDecorator<T> entityDecorator,
                            NitriteCollection collection,
                            NitriteConfig nitriteConfig) {
        this.entityDecorator = entityDecorator;
        this.collection = collection;
        this.nitriteConfig = nitriteConfig;
        initialize();
    }

    @Override
    public void addProcessor(Processor processor) {
        notNull(processor, "a null processor cannot be added");
        collection.addProcessor(processor);
    }

    @Override
    public void createIndex(IndexOptions indexOptions, String... fields) {
        collection.createIndex(indexOptions, fields);
    }

    @Override
    public void rebuildIndex(String... fields) {
        collection.rebuildIndex(fields);
    }

    @Override
    public Collection<IndexDescriptor> listIndices() {
        return collection.listIndices();
    }

    @Override
    public boolean hasIndex(String... fields) {
        return collection.hasIndex(fields);
    }

    @Override
    public boolean isIndexing(String... fields) {
        return collection.isIndexing(fields);
    }

    @Override
    public void dropIndex(String... fields) {
        collection.dropIndex(fields);
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
        return update(operations.createUniqueFilter(element), element, updateOptions(insertIfAbsent, true));
    }

    @Override
    public WriteResult update(Filter filter, T update, UpdateOptions updateOptions) {
        notNull(update, "a null object cannot be used for update");
        Document updateDocument = operations.toDocument(update, true);
        if (updateOptions == null || !updateOptions.isInsertIfAbsent()) {
            operations.removeNitriteId(updateDocument);
        }

        return collection.update(operations.asObjectFilter(filter), updateDocument,
            updateOptions);
    }

    @Override
    public WriteResult update(Filter filter, Document update, boolean justOnce) {
        notNull(update, "a null document cannot be used for update");
        operations.removeNitriteId(update);
        operations.serializeFields(update);
        return collection.update(operations.asObjectFilter(filter), update, updateOptions(false, justOnce));
    }

    @Override
    public WriteResult remove(T element) {
        notNull(element, "a null object cannot be removed");
        return remove(operations.createUniqueFilter(element));
    }

    @Override
    public WriteResult remove(Filter filter, boolean justOne) {
        return collection.remove(operations.asObjectFilter(filter), justOne);
    }

    @Override
    public void clear() {
        collection.clear();
    }


    @Override
    public Cursor<T> find(Filter filter, FindOptions findOptions) {
        return operations.find(filter, findOptions, getType());
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
    public NitriteStore<?> getStore() {
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
        if (entityDecorator != null) {
            return entityDecorator.getEntityType();
        } else {
            return type;
        }
    }

    @Override
    public NitriteCollection getDocumentCollection() {
        return collection;
    }

    private void initialize() {
        if (entityDecorator != null) {
            operations = new RepositoryOperations(entityDecorator, collection, nitriteConfig);
        } else {
            operations = new RepositoryOperations(type, collection, nitriteConfig);
        }
        operations.createIndices();
    }
}
