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

package org.dizitart.no2.index;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.FindPlan;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.FieldValues;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.common.module.NitritePlugin;

import java.util.LinkedHashSet;

/**
 * An abstract class representing a Nitrite indexer plugin.
 * <p>
 * NitriteIndexer extends NitritePlugin and provides a base class for all Nitrite
 * indexer plugins. It defines the basic structure and functionality of an indexer
 * plugin that can be used to index Nitrite collections.
 *
 * @author Anindya Chatterjee.
 * @since 4.0
 */
public interface NitriteIndexer extends NitritePlugin {
    /**
     * Gets the index type.
     *
     * @return the index type
     */
    String getIndexType();

    /**
     * Validates the given fields for indexing.
     *
     * @param fields the fields to be validated
     */
    void validateIndex(Fields fields);

    /**
     * Drops the index from the collection.
     *
     * @param indexDescriptor the descriptor of the index to be dropped.
     * @param nitriteConfig   the Nitrite configuration object.
     */
    void dropIndex(IndexDescriptor indexDescriptor, NitriteConfig nitriteConfig);

    /**
     * Writes an index entry for the given field values and index descriptor.
     *
     * @param fieldValues     the field values to be indexed
     * @param indexDescriptor the descriptor of the index
     * @param nitriteConfig   the NitriteConfig to use for indexing
     */
    void writeIndexEntry(FieldValues fieldValues, IndexDescriptor indexDescriptor, NitriteConfig nitriteConfig);

    /**
     * Removes an index entry for the given field values and index descriptor from the Nitrite database.
     *
     * @param fieldValues     the field values to remove the index entry for
     * @param indexDescriptor the index descriptor for the index entry to remove
     * @param nitriteConfig   the Nitrite configuration object
     */
    void removeIndexEntry(FieldValues fieldValues, IndexDescriptor indexDescriptor, NitriteConfig nitriteConfig);

    /**
     * Finds the NitriteIds of the documents that match the given filter in the specified collection.
     *
     * @param findPlan the plan for finding the documents.
     * @param nitriteConfig the Nitrite configuration.
     * @return a set of NitriteIds of the documents that match the given filter.
     */
    LinkedHashSet<NitriteId> findByFilter(FindPlan findPlan, NitriteConfig nitriteConfig);
}
