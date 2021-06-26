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

import org.dizitart.no2.index.IndexMap;
import org.locationtech.jts.geom.Geometry;

import java.util.List;

/**
 * @since 4.0
 * @author Anindya Chatterjee
 */
class IntersectsFilter extends SpatialFilter {
    protected IntersectsFilter(String field, Geometry geometry) {
        super(field, geometry);
    }

    @Override
    public List<?> applyOnIndex(IndexMap indexMap) {
        // calculated from SpatialIndex
        return null;
    }

    @Override
    public String toString() {
        return "(" + getField() + " intersects " + getValue() + ")";
    }
}
