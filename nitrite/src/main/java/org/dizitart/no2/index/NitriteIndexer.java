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
import org.dizitart.no2.common.DBValue;
import org.dizitart.no2.common.FieldValues;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.common.module.NitritePlugin;
import org.dizitart.no2.common.tuples.Pair;

import java.util.LinkedHashSet;
import java.util.List;

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

    /**
     * Streams the ids matching the plan lazily, or returns {@code null} when the indexer has no
     * lazy path for it and {@link #findByFilter(FindPlan, NitriteConfig)} must be used. The
     * default is {@code null}, so existing indexer plugins are unaffected.
     *
     * @param findPlan      the find plan
     * @param nitriteConfig the nitrite config
     * @return a re-iterable stream of ids, or {@code null}
     */
    default RecordStream<NitriteId> findByFilterStream(FindPlan findPlan, NitriteConfig nitriteConfig) {
        return null;
    }

    /**
     * Reads every {@code (indexed value, id)} pair out of the given index, so a sorted query
     * can decide its order without deserializing a single document.
     *
     * @param indexDescriptor the index to read.
     * @param nitriteConfig   the Nitrite configuration.
     * @param collectionSize  the number of documents in the indexed collection.
     * @return the pairs, in no particular order, or {@code null} when this indexer cannot
     * supply them or the index is not a faithful stand-in for the collection.
     * @since 4.4
     */
    default List<Pair<DBValue, NitriteId>> readSortKeys(IndexDescriptor indexDescriptor,
                                                        NitriteConfig nitriteConfig,
                                                        long collectionSize) {
        return null;
    }
}
