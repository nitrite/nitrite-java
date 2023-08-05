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

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.*;
import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.common.meta.Attributes;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.common.event.EventBus;
import org.dizitart.no2.common.processors.Processor;
import org.dizitart.no2.common.processors.ProcessorChain;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.StoreCatalog;

import java.util.Collection;

/**
 * The collection operations.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 */
public class CollectionOperations implements AutoCloseable {
    private final String collectionName;
    private final NitriteConfig nitriteConfig;
    private final NitriteMap<NitriteId, Document> nitriteMap;
    private final EventBus<CollectionEventInfo<?>, CollectionEventListener> eventBus;
    private ProcessorChain processorChain;
    private IndexOperations indexOperations;
    private WriteOperations writeOperations;
    private ReadOperations readOperations;

    /**
     * Instantiates a new Collection operations.
     *
     * @param collectionName the collection name
     * @param nitriteMap     the nitrite map
     * @param nitriteConfig  the nitrite config
     * @param eventBus       the event bus
     */
    public CollectionOperations(String collectionName,
                                NitriteMap<NitriteId, Document> nitriteMap,
                                NitriteConfig nitriteConfig,
                                EventBus<CollectionEventInfo<?>, CollectionEventListener> eventBus) {
        this.collectionName = collectionName;
        this.nitriteMap = nitriteMap;
        this.nitriteConfig = nitriteConfig;
        this.eventBus = eventBus;
        initialize();
    }

    /**
     * Adds a document processor.
     *
     * @param processor the processor
     */
    public void addProcessor(Processor processor) {
        processorChain.add(processor);
    }

    /**
     * Creates index.
     *
     * @param fields    the fields
     * @param indexType the index type
     */
    public void createIndex(Fields fields, String indexType) {
        indexOperations.createIndex(fields, indexType);
    }

    /**
     * Finds index descriptor.
     *
     * @param fields the fields
     * @return the index descriptor
     */
    public IndexDescriptor findIndex(Fields fields) {
        return indexOperations.findIndexDescriptor(fields);
    }

    /**
     * Rebuilds index.
     *
     * @param indexDescriptor the index descriptor
     */
    public void rebuildIndex(IndexDescriptor indexDescriptor) {
        indexOperations.buildIndex(indexDescriptor, true);
    }

    /**
     * Lists all indexes.
     *
     * @return the collection
     */
    public Collection<IndexDescriptor> listIndexes() {
        return indexOperations.listIndexes();
    }

    /**
     * Checks if an index exists on the fields.
     *
     * @param fields the fields
     * @return the boolean
     */
    public boolean hasIndex(Fields fields) {
        return indexOperations.hasIndexEntry(fields);
    }

    /**
     * Checks if indexing is going on the fields.
     *
     * @param fields the fields
     * @return the boolean
     */
    public boolean isIndexing(Fields fields) {
        return indexOperations.isIndexing(fields);
    }

    /**
     * Drops index.
     *
     * @param fields the fields
     */
    public void dropIndex(Fields fields) {
        indexOperations.dropIndex(fields);
    }

    /**
     * Drops all indices.
     */
    public void dropAllIndices() {
        indexOperations.dropAllIndices();
    }

    /**
     * Inserts documents to the collection.
     *
     * @param documents the documents
     * @return the write result
     */
    public WriteResult insert(Document[] documents) {
        return writeOperations.insert(documents);
    }

    /**
     * Updates documents in the collection.
     *
     * @param filter        the filter
     * @param update        the update
     * @param updateOptions the update options
     * @return the write result
     */
    public WriteResult update(Filter filter, Document update, UpdateOptions updateOptions) {
        return writeOperations.update(filter, update, updateOptions);
    }

    /**
     * Removes document from the collection.
     *
     * @param document the document
     * @return the write result
     */
    public WriteResult remove(Document document) {
        return writeOperations.remove(document);
    }

    /**
     * Removes document from collection.
     *
     * @param filter   the filter
     * @param justOnce the just once
     * @return the write result
     */
    public WriteResult remove(Filter filter, boolean justOnce) {
        return writeOperations.remove(filter, justOnce);
    }

    /**
     * Finds documents using filter.
     *
     * @param filter      the filter
     * @param findOptions the find options
     * @return the document cursor
     */
    public DocumentCursor find(Filter filter, FindOptions findOptions) {
        return readOperations.find(filter, findOptions);
    }

    /**
     * Gets document by id.
     *
     * @param nitriteId the nitrite id
     * @return the by id
     */
    public Document getById(NitriteId nitriteId) {
        return readOperations.getById(nitriteId);
    }

    /**
     * Drops the collection.
     */
    public void dropCollection() {
        indexOperations.dropAllIndices();
        dropNitriteMap();
    }

    /**
     * Gets the size of the collection.
     *
     * @return the size
     */
    public long getSize() {
        return nitriteMap.size();
    }

    /**
     * Gets the additional attributes for the collection.
     *
     * @return the attributes
     */
    public Attributes getAttributes() {
        return nitriteMap != null ? nitriteMap.getAttributes() : null;
    }

    /**
     * Sets additional attributes in the collection.
     *
     * @param attributes the attributes
     */
    public void setAttributes(Attributes attributes) {
        nitriteMap.setAttributes(attributes);
    }

    public void close() {
        if (indexOperations != null) {
            indexOperations.close();
        }
        nitriteMap.close();
    }

    public void clear() {
        nitriteMap.clear();
        indexOperations.clear();
    }

    public void initialize() {
        this.processorChain = new ProcessorChain();
        this.indexOperations = new IndexOperations(collectionName, nitriteConfig, nitriteMap, eventBus);
        this.readOperations = new ReadOperations(collectionName, indexOperations,
            nitriteConfig, nitriteMap, processorChain);

        DocumentIndexWriter indexWriter = new DocumentIndexWriter(nitriteConfig, indexOperations);
        this.writeOperations = new WriteOperations(indexWriter, readOperations,
            nitriteMap, eventBus, processorChain);
    }

    private void dropNitriteMap() {
        // remove the collection name from the catalog
        StoreCatalog catalog = nitriteMap.getStore().getCatalog();
        catalog.remove(nitriteMap.getName());

        // drop the map
        nitriteMap.drop();
    }
}
