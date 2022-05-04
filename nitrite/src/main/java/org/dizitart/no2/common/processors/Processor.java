/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.common.processors;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.PersistentCollection;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.repository.ObjectRepository;

import static org.dizitart.no2.collection.UpdateOptions.updateOptions;
import static org.dizitart.no2.common.util.DocumentUtils.createUniqueFilter;

/**
 * Represents a document processor.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
public interface Processor {

    /**
     * Processes a document before writing it into database.
     *
     * @param document the document
     * @return the document
     */
    Document processBeforeWrite(Document document);

    /**
     * Processes a document after reading from the database.
     *
     * @param document the document
     * @return the document
     */
    Document processAfterRead(Document document);

    /**
     * Processes all documents of a {@link PersistentCollection}.
     *
     * @param collection the collection to process
     */
    default void process(PersistentCollection<?> collection) {
        NitriteCollection nitriteCollection = null;
        if (collection instanceof NitriteCollection) {
            nitriteCollection = (NitriteCollection) collection;
        } else if (collection instanceof ObjectRepository<?>) {
            ObjectRepository<?> repository = (ObjectRepository<?>) collection;
            nitriteCollection = repository.getDocumentCollection();
        }

        if (nitriteCollection != null) {
            for (Document document : nitriteCollection.find(Filter.ALL, null)) {
                Document processed = processBeforeWrite(document);
                nitriteCollection.update(createUniqueFilter(document), processed, updateOptions(false));
            }
        }
    }
}

