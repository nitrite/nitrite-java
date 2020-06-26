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
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.ReadableStream;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.index.BoundingBox;
import org.dizitart.no2.index.Indexer;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteRTree;
import org.dizitart.no2.store.NitriteStore;
import org.locationtech.jts.geom.Geometry;

/**
 * @author Anindya Chatterjee
 * @since 4.0.0
 */
public class SpatialIndexer implements Indexer {
    public static final String SpatialIndex = "Spatial";

    private NitriteMapper nitriteMapper;
    private NitriteStore nitriteStore;

    public ReadableStream<NitriteId> findWithin(String collectionName, String field, Geometry geometry) {
        NitriteRTree<BoundingBox, Geometry> indexMap = getIndexMap(collectionName, field);
        BoundingBox boundingBox = new NitriteBoundingBox(geometry);
        return indexMap.findContainedKeys(boundingBox);
    }

    public ReadableStream<NitriteId> findIntersects(String collectionName, String field, Geometry geometry) {
        NitriteRTree<BoundingBox, Geometry> indexMap = getIndexMap(collectionName, field);
        BoundingBox boundingBox = new NitriteBoundingBox(geometry);
        return indexMap.findIntersectingKeys(boundingBox);
    }

    @Override
    public String getIndexType() {
        return SpatialIndex;
    }

    @Override
    public void writeIndex(NitriteMap<NitriteId, Document> collection, NitriteId nitriteId, String field, Object fieldValue) {
        if (fieldValue == null) return;
        NitriteRTree<BoundingBox, Geometry> indexMap = getIndexMap(collection.getName(), field);
        Geometry geometry = parseGeometry(field, fieldValue);
        BoundingBox boundingBox = new NitriteBoundingBox(geometry);
        indexMap.add(boundingBox, nitriteId);
    }

    @Override
    public void removeIndex(NitriteMap<NitriteId, Document> collection, NitriteId nitriteId, String field, Object fieldValue) {
        if (fieldValue == null) return;
        NitriteRTree<BoundingBox, Geometry> indexMap = getIndexMap(collection.getName(), field);
        Geometry geometry = parseGeometry(field, fieldValue);
        BoundingBox boundingBox = new NitriteBoundingBox(geometry);
        indexMap.remove(boundingBox, nitriteId);
    }

    @Override
    public void updateIndex(NitriteMap<NitriteId, Document> collection, NitriteId nitriteId, String field, Object newValue, Object oldValue) {
        removeIndex(collection, nitriteId, field, oldValue);
        writeIndex(collection, nitriteId, field, newValue);
    }

    @Override
    public void dropIndex(NitriteMap<NitriteId, Document> collection, String field) {
        // no action required
    }

    @Override
    public void initialize(NitriteConfig nitriteConfig) {
        this.nitriteStore = nitriteConfig.getNitriteStore();
        this.nitriteMapper = nitriteConfig.nitriteMapper();
    }

    private NitriteRTree<BoundingBox, Geometry> getIndexMap(String collectionName, String field) {
        String mapName = getIndexMapName(collectionName, field);
        return nitriteStore.openRTree(mapName);
    }

    private Geometry parseGeometry(String field, Object fieldValue) {
        if (fieldValue == null) return null;
        if (fieldValue instanceof String) {
            return nitriteMapper.convert(fieldValue, Geometry.class);
        } else if (fieldValue instanceof Geometry) {
            return (Geometry) fieldValue;
        }
        throw new IndexingException("field " + field + " does not contain Geometry data");
    }
}
