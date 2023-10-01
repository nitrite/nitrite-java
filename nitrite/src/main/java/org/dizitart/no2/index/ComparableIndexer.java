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
import org.dizitart.no2.exceptions.IndexingException;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Anindya Chatterjee
 * @since 4.0
 */
public abstract class ComparableIndexer implements NitriteIndexer {
    private final Map<IndexDescriptor, NitriteIndex> indexRegistry;

    /**
     * Instantiates a new Comparable indexer.
     */
    public ComparableIndexer() {
        this.indexRegistry = new ConcurrentHashMap<>();
    }

    /**
     * Indicates if it is an unique index.
     *
     * @return the boolean
     */
    abstract boolean isUnique();

    @Override
    public void initialize(NitriteConfig nitriteConfig) {
    }

    @Override
    public void validateIndex(Fields fields) {
        // nothing to validate
    }

    @Override
    public LinkedHashSet<NitriteId> findByFilter(FindPlan findPlan, NitriteConfig nitriteConfig) {
        NitriteIndex nitriteIndex = findNitriteIndex(findPlan.getIndexDescriptor(), nitriteConfig);
        return nitriteIndex.findNitriteIds(findPlan);
    }

    @Override
    public void writeIndexEntry(FieldValues fieldValues, IndexDescriptor indexDescriptor,
                                NitriteConfig nitriteConfig) {
        NitriteIndex nitriteIndex = findNitriteIndex(indexDescriptor, nitriteConfig);
        nitriteIndex.write(fieldValues);
    }

    @Override
    public void removeIndexEntry(FieldValues fieldValues, IndexDescriptor indexDescriptor,
                                 NitriteConfig nitriteConfig) {
        NitriteIndex nitriteIndex = findNitriteIndex(indexDescriptor, nitriteConfig);
        nitriteIndex.remove(fieldValues);
    }

    @Override
    public void dropIndex(IndexDescriptor indexDescriptor, NitriteConfig nitriteConfig) {
        NitriteIndex nitriteIndex = findNitriteIndex(indexDescriptor, nitriteConfig);
        nitriteIndex.drop();
    }

    private NitriteIndex findNitriteIndex(IndexDescriptor indexDescriptor, NitriteConfig nitriteConfig) {
        if (indexDescriptor == null) {
            throw new IndexingException("Index descriptor cannot be null");
        }

        if (indexRegistry.containsKey(indexDescriptor)) {
            return indexRegistry.get(indexDescriptor);
        }

        NitriteIndex nitriteIndex;
        if (indexDescriptor.isCompoundIndex()) {
            nitriteIndex = new CompoundIndex(indexDescriptor, nitriteConfig.getNitriteStore());
        } else {
            nitriteIndex = new SingleFieldIndex(indexDescriptor, nitriteConfig.getNitriteStore());
        }
        indexRegistry.put(indexDescriptor, nitriteIndex);
        return nitriteIndex;
    }
}
