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
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.FieldValues;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.common.util.DocumentUtils;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.NitriteIndexer;
import org.dizitart.no2.store.IndexCatalog;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.util.Collection;

/**
 * @author Anindya Chatterjee
 */
class DocumentIndexWriter {
    private final NitriteConfig nitriteConfig;
    private final NitriteMap<NitriteId, Document> nitriteMap;
    private final IndexOperations indexOperations;
    private String collectionName;
    private IndexCatalog indexCatalog;

    DocumentIndexWriter(NitriteConfig nitriteConfig,
                        NitriteMap<NitriteId, Document> nitriteMap,
                        IndexOperations indexOperations) {
        this.nitriteConfig = nitriteConfig;
        this.nitriteMap = nitriteMap;
        this.indexOperations = indexOperations;
        initialize();
    }

    void writeIndexEntry(Document document) {
        Collection<IndexDescriptor> indexEntries = indexOperations.listIndexes();
        if (indexEntries != null) {
            for (IndexDescriptor indexDescriptor : indexEntries) {
                String indexType = indexDescriptor.getIndexType();
                NitriteIndexer nitriteIndexer = nitriteConfig.findIndexer(indexType);

                writeIndexEntryInternal(indexDescriptor, document, nitriteIndexer);
            }
        }
    }

    void removeIndexEntry(Document document) {
        Collection<IndexDescriptor> indexEntries = indexOperations.listIndexes();
        if (indexEntries != null) {
            for (IndexDescriptor indexDescriptor : indexEntries) {
                String indexType = indexDescriptor.getIndexType();
                NitriteIndexer nitriteIndexer = nitriteConfig.findIndexer(indexType);

                removeIndexEntryInternal(indexDescriptor, document, nitriteIndexer);
            }
        }
    }

    void updateIndexEntry(Document oldDocument, Document newDocument) {
        Collection<IndexDescriptor> indexEntries = indexOperations.listIndexes();
        if (indexEntries != null) {
            for (IndexDescriptor indexDescriptor : indexEntries) {
                String indexType = indexDescriptor.getIndexType();
                NitriteIndexer nitriteIndexer = nitriteConfig.findIndexer(indexType);

//                Fields fields = indexDescriptor.getFields();
//                Object newValue = newDocument.get(field);
//                Object oldValue = oldDocument.get(field);
//
//                if (newValue == null) continue;
//                if (newValue instanceof Comparable && oldValue instanceof Comparable) {
//                    if (((Comparable) newValue).compareTo(oldValue) == 0) continue;
//                }
//
//                validateDocumentIndexField(newValue, fields);
//
//                if (indexCatalog.isDirtyIndex(collectionName, fields)
//                    && !getBuildFlag(field).get()) {
//                    // rebuild will also take care of the current document
//                    rebuildIndex(indexDescriptor, true);
//                } else {
//                    String indexType = indexDescriptor.getIndexType();
//                    NitriteIndexer nitriteIndexer = nitriteConfig.findIndexer(indexType);
//                    nitriteIndexer.updateIndex(nitriteMap, nitriteId, field, newValue, oldValue);
//                }

                removeIndexEntryInternal(indexDescriptor, oldDocument, nitriteIndexer);
                writeIndexEntryInternal(indexDescriptor, newDocument, nitriteIndexer);
            }
        }
    }

    private void initialize() {
        NitriteStore<?> nitriteStore = nitriteConfig.getNitriteStore();
        this.indexCatalog = nitriteStore.getIndexCatalog();
        this.collectionName = nitriteMap.getName();
    }

    private void writeIndexEntryInternal(IndexDescriptor indexDescriptor, Document document,
                                         NitriteIndexer nitriteIndexer) {
        if (indexDescriptor != null) {
            Fields fields = indexDescriptor.getIndexFields();
            FieldValues fieldValues = DocumentUtils.getValues(document, fields);

//            Object fieldValue = document.get(field);
//            validateDocumentIndexField(fieldValue, field);

            // if dirty index and currently indexing is not running, rebuild
            if (indexCatalog.isDirtyIndex(collectionName, fields)
                && !indexOperations.getBuildFlag(fields).get()) {
                // rebuild will also take care of the current document
                indexOperations.buildIndex(indexDescriptor, true);
            } else if (nitriteIndexer != null) {
                nitriteIndexer.writeIndexEntry(indexDescriptor, fieldValues, nitriteConfig);
            }
        }
    }

    private void removeIndexEntryInternal(IndexDescriptor indexDescriptor, Document document,
                                          NitriteIndexer nitriteIndexer) {
        if (indexDescriptor != null) {
            Fields fields = indexDescriptor.getIndexFields();
            FieldValues fieldValues = DocumentUtils.getValues(document, fields);

//            Object fieldValue = document.get(field);
//            if (fieldValue == null) return;
//
//            validateDocumentIndexField(fieldValue, field);

            // if dirty index and currently indexing is not running, rebuild
            if (indexCatalog.isDirtyIndex(collectionName, fields)
                && !indexOperations.getBuildFlag(fields).get()) {
                // rebuild will also take care of the current document
                indexOperations.buildIndex(indexDescriptor, true);
            } else if (nitriteIndexer != null) {
                nitriteIndexer.removeIndexEntry(indexDescriptor, fieldValues, nitriteConfig);
            }
        }
    }

}
