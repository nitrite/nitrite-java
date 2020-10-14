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
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.UpdateOptions;
import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.common.event.EventBus;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.store.NitriteMap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.dizitart.no2.common.Constants.COLLECTION_CATALOG;

/**
 * @author Anindya Chatterjee
 */
public class CollectionOperations {
    private final String collectionName;
    private final NitriteConfig nitriteConfig;
    private final NitriteMap<NitriteId, Document> nitriteMap;
    private final EventBus<CollectionEventInfo<?>, CollectionEventListener> eventBus;
    private IndexOperations indexOperations;
    private WriteOperations writeOperations;
    private ReadOperations readOperations;

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

    public void createIndex(Fields fields, String indexType, boolean async) {
        indexOperations.createIndex(fields, indexType, async);
    }

    public IndexDescriptor findIndex(Fields fields) {
        return indexOperations.findIndexDescriptor(fields);
    }

    public void rebuildIndex(IndexDescriptor indexDescriptor, boolean async) {
        indexOperations.buildIndex(indexDescriptor, async, true);
    }

    public Collection<IndexDescriptor> listIndexes() {
        return indexOperations.listIndexes();
    }

    public boolean hasIndex(Fields fields) {
        return indexOperations.hasIndexEntry(fields);
    }

    public boolean isIndexing(Fields fields) {
        return indexOperations.isIndexing(fields);
    }

    public void dropIndex(Fields fields) {
        indexOperations.dropIndex(fields);
    }

    public void dropAllIndices() {
        indexOperations.dropAllIndices();
    }

    public WriteResult insert(Document[] documents) {
        return writeOperations.insert(documents);
    }

    public WriteResult update(Filter filter, Document update, UpdateOptions updateOptions) {
        return writeOperations.update(filter, update, updateOptions);
    }

    public WriteResult remove(Document document) {
        return writeOperations.remove(document);
    }

    public WriteResult remove(Filter filter, boolean justOnce) {
        return writeOperations.remove(filter, justOnce);
    }

    public DocumentCursor find() {
        return readOperations.find();
    }

    public DocumentCursor find(Filter filter) {
        return readOperations.find(filter);
    }

    public Document getById(NitriteId nitriteId) {
        return readOperations.getById(nitriteId);
    }

    public void dropCollection() {
        indexOperations.dropAllIndices();
        dropNitriteMap();
    }

    public long getSize() {
        return nitriteMap.size();
    }

    public Attributes getAttributes() {
        return nitriteMap != null ? nitriteMap.getAttributes() : null;
    }

    public void setAttributes(Attributes attributes) {
        nitriteMap.setAttributes(attributes);
    }

    public void close() {
        if (indexOperations != null) {
            indexOperations.close();
        }
    }

    private void initialize() {
        this.indexOperations = new IndexOperations(nitriteConfig, nitriteMap, eventBus);
        DocumentIndexWriter indexWriter = new DocumentIndexWriter(nitriteConfig, nitriteMap, indexOperations);
        this.readOperations = new ReadOperations(collectionName, nitriteConfig, nitriteMap, indexWriter);
        this.writeOperations = new WriteOperations(indexWriter, readOperations, nitriteMap, eventBus);
    }

    private void dropNitriteMap() {
        NitriteMap<String, Document> catalogueMap = nitriteMap.getStore().openMap(COLLECTION_CATALOG,
            String.class, Document.class);
        for (Pair<String, Document> entry : catalogueMap.entries()) {
            String catalogue = entry.getFirst();
            Document document = entry.getSecond();

            Set<String> bin = new HashSet<>();
            boolean foundKey = false;
            for (String field : document.getFields()) {
                if (field.equals(nitriteMap.getName())) {
                    foundKey = true;
                    bin.add(field);
                }
            }

            for (String field : bin) {
                document.remove(field);
            }
            catalogueMap.put(catalogue, document);

            if (foundKey) break;
        }
        nitriteMap.drop();
    }
}
