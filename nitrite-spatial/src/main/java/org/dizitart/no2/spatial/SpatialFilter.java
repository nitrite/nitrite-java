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

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.filters.IndexOnlyFilter;
import org.locationtech.jts.geom.Geometry;

/**
 * Represents a spatial filter.
 *
 * @since 4.0.0
 * @author Anindya Chatterjee
 */
public abstract class SpatialFilter extends IndexOnlyFilter {
    private final Geometry geometry;

    /**
     * Instantiates a new {@link SpatialFilter}.
     *
     * @param field    the field
     * @param geometry the geometry
     */
    protected SpatialFilter(String field, Geometry geometry) {
        super(field, geometry);
        this.geometry = geometry;
    }

    @Override
    public Geometry getValue() {
        return geometry;
    }

    @Override
    public boolean apply(Pair<NitriteId, Document> element) {
        return false;
    }

    @Override
    public String supportedIndexType() {
        return SpatialIndexer.SPATIAL_INDEX;
    }

    @Override
    public boolean canBeGrouped(IndexOnlyFilter other) {
        return other instanceof SpatialFilter
            && other.getField().equals(getField());
    }
}
