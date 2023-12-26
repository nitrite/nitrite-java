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

package org.dizitart.no2.spatial;

import lombok.Getter;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.FindPlan;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.FieldValues;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.filters.ComparableFilter;
import org.dizitart.no2.filters.IndexScanFilter;
import org.dizitart.no2.index.BoundingBox;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.NitriteIndex;
import org.dizitart.no2.store.NitriteRTree;
import org.dizitart.no2.store.NitriteStore;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import java.util.LinkedHashSet;
import java.util.List;

import static org.dizitart.no2.common.util.IndexUtils.deriveIndexMapName;

/**
 * Represents a spatial index in nitrite.
 *
 * @since 4.0
 * @author Anindya Chatterjee
 */
public class SpatialIndex implements NitriteIndex {
    @Getter
    private final IndexDescriptor indexDescriptor;
    private final NitriteStore<?> nitriteStore;

    /**
     * Instantiates a new {@link SpatialIndex}.
     *
     * @param indexDescriptor the index descriptor
     * @param nitriteConfig   the nitrite config
     */
    public SpatialIndex(IndexDescriptor indexDescriptor, NitriteConfig nitriteConfig) {
        this.indexDescriptor = indexDescriptor;
        this.nitriteStore = nitriteConfig.getNitriteStore();
    }

    @Override
    public void write(FieldValues fieldValues) {
        Fields fields = fieldValues.getFields();
        List<String> fieldNames = fields.getFieldNames();

        String firstField = fieldNames.get(0);
        Object element = fieldValues.get(firstField);

        NitriteRTree<BoundingBox, Geometry> indexMap = findIndexMap();
        if (element == null) {
            indexMap.add(BoundingBox.EMPTY, fieldValues.getNitriteId());
        } else {
            Geometry geometry = parseGeometry(firstField, element);
            if (geometry == null) {
                indexMap.add(BoundingBox.EMPTY, fieldValues.getNitriteId());
            } else {
                BoundingBox boundingBox = fromGeometry(geometry);
                indexMap.add(boundingBox, fieldValues.getNitriteId());
            }
        }
    }

    @Override
    public void remove(FieldValues fieldValues) {
        Fields fields = fieldValues.getFields();
        List<String> fieldNames = fields.getFieldNames();

        String firstField = fieldNames.get(0);
        Object element = fieldValues.get(firstField);

        NitriteRTree<BoundingBox, Geometry> indexMap = findIndexMap();
        if (element == null) {
            indexMap.remove(BoundingBox.EMPTY, fieldValues.getNitriteId());
        } else {
            Geometry geometry = parseGeometry(firstField, element);
            if (geometry == null) {
                indexMap.remove(BoundingBox.EMPTY, fieldValues.getNitriteId());
            } else {
                BoundingBox boundingBox = fromGeometry(geometry);
                indexMap.remove(boundingBox, fieldValues.getNitriteId());
            }
        }
    }

    @Override
    public void drop() {
        NitriteRTree<BoundingBox, Geometry> indexMap = findIndexMap();
        indexMap.clear();
        indexMap.drop();
    }

    @Override
    public LinkedHashSet<NitriteId> findNitriteIds(FindPlan findPlan) {
        IndexScanFilter indexScanFilter = findPlan.getIndexScanFilter();
        if (indexScanFilter == null
            || indexScanFilter.getFilters() == null
            || indexScanFilter.getFilters().isEmpty()) {
            throw new FilterException("No spatial filter found");
        }

        List<ComparableFilter> filters = indexScanFilter.getFilters();
        ComparableFilter filter = filters.get(0);

        if (!(filter instanceof SpatialFilter)) {
            throw new FilterException("Spatial filter must be the first filter for index scan");
        }

        RecordStream<NitriteId> keys;
        NitriteRTree<BoundingBox, Geometry> indexMap = findIndexMap();

        SpatialFilter spatialFilter = (SpatialFilter) filter;
        Geometry geometry = spatialFilter.getValue();
        BoundingBox boundingBox = fromGeometry(geometry);

        if (filter instanceof WithinFilter) {
            keys = indexMap.findContainedKeys(boundingBox);
        } else if (filter instanceof IntersectsFilter) {
            keys = indexMap.findIntersectingKeys(boundingBox);
        } else {
            throw new FilterException("Unsupported spatial filter " + filter);
        }

        LinkedHashSet<NitriteId> nitriteIds = new LinkedHashSet<>();
        if (keys != null) {
            for (NitriteId nitriteId : keys) {
                nitriteIds.add(nitriteId);
            }
        }

        return nitriteIds;
    }

    private NitriteRTree<BoundingBox, Geometry> findIndexMap() {
        String mapName = deriveIndexMapName(indexDescriptor);
        return nitriteStore.openRTree(mapName, BoundingBox.class, Geometry.class);
    }

    private Geometry parseGeometry(String field, Object fieldValue) {
        if (fieldValue == null) return null;
        if (fieldValue instanceof String) {
            return GeometryUtils.fromString((String) fieldValue);
        } else if (fieldValue instanceof Geometry) {
            return (Geometry) fieldValue;
        } else if (fieldValue instanceof Document) {
            // in case of document, check if it contains geometry field
            // GeometryConverter convert a geometry to document with geometry field
            Document document = (Document) fieldValue;
            if (document.containsField("geometry")) {
                return GeometryUtils.fromString(document.get("geometry", String.class));
            }
        }
        throw new IndexingException("Field " + field + " does not contain Geometry data");
    }

    private BoundingBox fromGeometry(Geometry geometry) {
        if (geometry == null) return null;
        Envelope env = geometry.getEnvelopeInternal();
        BoundingBox boundingBox = new BoundingBox();
        boundingBox.setMinX((float) env.getMinX());
        boundingBox.setMaxX((float) env.getMaxX());
        boundingBox.setMinY((float) env.getMinY());
        boundingBox.setMaxY((float) env.getMaxY());
        return boundingBox;
    }
}
