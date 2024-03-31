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

package org.dizitart.no2.spatial;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.FindPlan;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.FieldValues;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.NitriteIndex;
import org.dizitart.no2.index.NitriteIndexer;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@code SpatialIndexer} class implements the {@link NitriteIndexer}
 * interface and provides support for creating and managing spatial
 * indexes in Nitrite database. It uses the {@link SpatialIndex} class to create
 * and manage the indexes.
 * 
 * @since 4.0
 * @author Anindya Chatterjee
 */
public class SpatialIndexer implements NitriteIndexer {
    /**
     * The name of the spatial index type. To be used while creating a spatial
     * index.
     */
    public static final String SPATIAL_INDEX = "Spatial";
    private final Map<IndexDescriptor, NitriteIndex> indexRegistry;

    /**
     * Instantiates a new {@link SpatialIndexer}.
     */
    public SpatialIndexer() {
        this.indexRegistry = new ConcurrentHashMap<>();
    }

    @Override
    public String getIndexType() {
        return SPATIAL_INDEX;
    }

    @Override
    public void validateIndex(Fields fields) {
        if (fields.getFieldNames().size() > 1) {
            throw new IndexingException("Spatial index can only be created on a single field");
        }
    }

    @Override
    public void dropIndex(IndexDescriptor indexDescriptor, NitriteConfig nitriteConfig) {
        NitriteIndex spatialIndex = findSpatialIndex(indexDescriptor, nitriteConfig);
        spatialIndex.drop();
    }

    @Override
    public void writeIndexEntry(FieldValues fieldValues, IndexDescriptor indexDescriptor, NitriteConfig nitriteConfig) {
        NitriteIndex spatialIndex = findSpatialIndex(indexDescriptor, nitriteConfig);
        spatialIndex.write(fieldValues);
    }

    @Override
    public void removeIndexEntry(FieldValues fieldValues, IndexDescriptor indexDescriptor,
            NitriteConfig nitriteConfig) {
        NitriteIndex spatialIndex = findSpatialIndex(indexDescriptor, nitriteConfig);
        spatialIndex.remove(fieldValues);
    }

    @Override
    public LinkedHashSet<NitriteId> findByFilter(FindPlan findPlan, NitriteConfig nitriteConfig) {
        NitriteIndex spatialIndex = findSpatialIndex(findPlan.getIndexDescriptor(), nitriteConfig);
        return spatialIndex.findNitriteIds(findPlan);
    }

    @Override
    public void initialize(NitriteConfig nitriteConfig) {
    }

    private NitriteIndex findSpatialIndex(IndexDescriptor indexDescriptor, NitriteConfig nitriteConfig) {
        if (indexRegistry.containsKey(indexDescriptor)) {
            return indexRegistry.get(indexDescriptor);
        }

        SpatialIndex nitriteIndex = new SpatialIndex(indexDescriptor, nitriteConfig);
        indexRegistry.put(indexDescriptor, nitriteIndex);
        return nitriteIndex;
    }
}
