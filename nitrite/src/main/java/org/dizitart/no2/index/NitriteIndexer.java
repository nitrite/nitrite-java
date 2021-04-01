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
import org.dizitart.no2.module.NitritePlugin;

import java.util.LinkedHashSet;

/**
 * Represents an indexer for creating a nitrite index.
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
     * Validates an index on the fields.
     *
     * @param fields the fields
     */
    void validateIndex(Fields fields);

    /**
     * Drops the index specified by the index descriptor.
     *
     * @param indexDescriptor the index descriptor
     * @param nitriteConfig   the nitrite config
     */
    void dropIndex(IndexDescriptor indexDescriptor, NitriteConfig nitriteConfig);

    /**
     * Writes an index entry.
     *
     * @param fieldValues     the field values
     * @param indexDescriptor the index descriptor
     * @param nitriteConfig   the nitrite config
     */
    void writeIndexEntry(FieldValues fieldValues, IndexDescriptor indexDescriptor, NitriteConfig nitriteConfig);

    /**
     * Removes an index entry.
     *
     * @param fieldValues     the field values
     * @param indexDescriptor the index descriptor
     * @param nitriteConfig   the nitrite config
     */
    void removeIndexEntry(FieldValues fieldValues, IndexDescriptor indexDescriptor, NitriteConfig nitriteConfig);

    /**
     * Finds a list of {@link NitriteId} after executing the {@link FindPlan} on the index.
     *
     * @param findPlan      the find plan
     * @param nitriteConfig the nitrite config
     * @return the linked hash set
     */
    LinkedHashSet<NitriteId> findByFilter(FindPlan findPlan, NitriteConfig nitriteConfig);
}
