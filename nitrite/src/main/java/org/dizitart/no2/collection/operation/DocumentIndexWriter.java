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
import org.dizitart.no2.common.FieldValues;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.common.util.DocumentUtils;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.NitriteIndexer;

import java.util.Collection;

/**
 *
 * @since 4.0
 * @author Anindya Chatterjee
 */
class DocumentIndexWriter {
    private final NitriteConfig nitriteConfig;
    private final IndexOperations indexOperations;

    DocumentIndexWriter(NitriteConfig nitriteConfig,
                        IndexOperations indexOperations) {
        this.nitriteConfig = nitriteConfig;
        this.indexOperations = indexOperations;
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

                removeIndexEntryInternal(indexDescriptor, oldDocument, nitriteIndexer);
                writeIndexEntryInternal(indexDescriptor, newDocument, nitriteIndexer);
            }
        }
    }

    private void writeIndexEntryInternal(IndexDescriptor indexDescriptor, Document document,
                                         NitriteIndexer nitriteIndexer) {
        if (indexDescriptor != null) {
            Fields fields = indexDescriptor.getFields();
            FieldValues fieldValues = DocumentUtils.getValues(document, fields);

            // if dirty index and currently indexing is not running, rebuild
            if (indexOperations.shouldRebuildIndex(fields)) {
                // rebuild will also take care of the current document
                indexOperations.buildIndex(indexDescriptor, true);
            } else if (nitriteIndexer != null) {
                nitriteIndexer.writeIndexEntry(fieldValues, indexDescriptor, nitriteConfig);
            }
        }
    }

    private void removeIndexEntryInternal(IndexDescriptor indexDescriptor, Document document,
                                          NitriteIndexer nitriteIndexer) {
        if (indexDescriptor != null) {
            Fields fields = indexDescriptor.getFields();
            FieldValues fieldValues = DocumentUtils.getValues(document, fields);

            // if dirty index and currently indexing is not running, rebuild
            if (indexOperations.shouldRebuildIndex(fields)) {
                // rebuild will also take care of the current document
                indexOperations.buildIndex(indexDescriptor, true);
            } else if (nitriteIndexer != null) {
                nitriteIndexer.removeIndexEntry(fieldValues, indexDescriptor, nitriteConfig);
            }
        }
    }

}
