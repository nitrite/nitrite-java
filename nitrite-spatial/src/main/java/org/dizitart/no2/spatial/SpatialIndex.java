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
import org.dizitart.no2.store.NitriteMap;
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

        // Phase 1: R-tree bounding box search (fast but may include false positives)
        RecordStream<NitriteId> candidateKeys;
        NitriteRTree<BoundingBox, Geometry> indexMap = findIndexMap();

        SpatialFilter spatialFilter = (SpatialFilter) filter;
        Geometry searchGeometry = spatialFilter.getValue();
        BoundingBox boundingBox = fromGeometry(searchGeometry);

        if (filter instanceof WithinFilter || filter instanceof GeoNearFilter) {
            // For Within/Near filters, we want points that intersect the search geometry
            // Note: We use intersecting here because we're searching for points WITHIN a circle
            // The R-tree stores point bounding boxes, and we want those that overlap with
            // the circle's bbox
            candidateKeys = indexMap.findIntersectingKeys(boundingBox);
        } else if (filter instanceof IntersectsFilter) {
            candidateKeys = indexMap.findIntersectingKeys(boundingBox);
        } else {
            throw new FilterException("Unsupported spatial filter " + filter);
        }

        LinkedHashSet<NitriteId> results = new LinkedHashSet<>();
        if (candidateKeys == null) {
            return results;
        }

        // Phase 2: Geometry refinement (precise filtering using actual JTS operations)
        // This eliminates false positives from the bounding box approximation
        for (NitriteId nitriteId : candidateKeys) {
            if (matchesGeometryFilter(nitriteId, spatialFilter, searchGeometry)) {
                results.add(nitriteId);
            }
        }

        return results;
    }
    
    /**
     * Performs precise geometry matching using JTS operations.
     * This is the second pass that eliminates false positives from the R-tree bbox search.
     * 
     * @param nitriteId the document ID to check
     * @param filter the spatial filter being applied
     * @param searchGeometry the geometry to search with
     * @return true if the stored geometry matches the filter criteria
     */
    private boolean matchesGeometryFilter(NitriteId nitriteId, SpatialFilter filter, Geometry searchGeometry) {
        try {
            // Retrieve the stored geometry for this document
            Geometry storedGeometry = getStoredGeometry(nitriteId);
            
            if (storedGeometry == null) {
                // If geometry is null, it matches only if the search is for null/empty
                return searchGeometry == null;
            }
            
            // Apply the appropriate JTS geometric operation based on filter type
            if (filter instanceof WithinFilter || filter instanceof GeoNearFilter) {
                // For Within and Near filters: the search geometry should contain the stored geometry
                // OR the stored geometry should be within the search geometry
                // For point-in-circle queries, we want to check if the point is within the circle
                boolean result = searchGeometry.contains(storedGeometry) || searchGeometry.covers(storedGeometry);
                return result;
            } else if (filter instanceof IntersectsFilter) {
                // For Intersects filter: geometries must intersect
                return searchGeometry.intersects(storedGeometry);
            }
            
            return false;
        } catch (Exception e) {
            // If there's an error (e.g., invalid geometry), exclude this result
            return false;
        }
    }
    
    /**
     * Retrieves the stored geometry from the collection for a given document ID.
     * 
     * @param nitriteId the document ID
     * @return the stored geometry, or null if not found
     */
    private Geometry getStoredGeometry(NitriteId nitriteId) {
        try {
            // Get the collection map name from the index descriptor
            String collectionName = indexDescriptor.getCollectionName();
            
            // Open the collection's document map
            NitriteMap<NitriteId, Document> documentMap = 
                nitriteStore.openMap(collectionName, NitriteId.class, Document.class);
            
            // Retrieve the document
            Document document = documentMap.get(nitriteId);
            if (document == null) {
                return null;
            }
            
            // Get the field name from the index descriptor
            Fields fields = indexDescriptor.getFields();
            List<String> fieldNames = fields.getFieldNames();
            if (fieldNames.isEmpty()) {
                return null;
            }
            
            String fieldName = fieldNames.get(0);
            Object fieldValue = document.get(fieldName);
            
            // Parse the geometry from the field value
            return parseGeometry(fieldName, fieldValue);
        } catch (Exception e) {
            // If there's an error retrieving the geometry, return null
            return null;
        }
    }

    private NitriteRTree<BoundingBox, Geometry> findIndexMap() {
        String mapName = deriveIndexMapName(indexDescriptor);
        return nitriteStore.openRTree(mapName, BoundingBox.class, Geometry.class);
    }

    private Geometry parseGeometry(String field, Object fieldValue) {
        if (fieldValue == null) return null;
        if (fieldValue instanceof String) {
            return GeometryUtils.fromString((String) fieldValue);
        } else if (fieldValue instanceof GeoPoint) {
            // Handle GeoPoint - get its underlying Point geometry
            return ((GeoPoint) fieldValue).getPoint();
        } else if (fieldValue instanceof Geometry) {
            return (Geometry) fieldValue;
        } else if (fieldValue instanceof Document) {
            // in case of document, check if it contains geometry field or lat/lon fields
            // GeometryConverter convert a geometry to document with geometry field
            // GeoPointConverter converts to document with latitude/longitude fields
            Document document = (Document) fieldValue;
            if (document.containsField("geometry")) {
                return GeometryUtils.fromString(document.get("geometry", String.class));
            } else if (document.containsField("latitude") && document.containsField("longitude")) {
                // Reconstruct GeoPoint from lat/lon
                Double lat = document.get("latitude", Double.class);
                Double lon = document.get("longitude", Double.class);
                if (lat != null && lon != null) {
                    return new GeoPoint(lat, lon).getPoint();
                }
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
