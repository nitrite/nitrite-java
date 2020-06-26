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

import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.ReadableStream;
import org.dizitart.no2.exceptions.FilterException;
import org.locationtech.jts.geom.Geometry;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
class WithinFilter extends SpatialFilter {
    protected WithinFilter(String field, Geometry geometry) {
        super(field, geometry);
    }

    @Override
    protected Set<NitriteId> findIndexedIdSet() {
        if (getIsFieldIndexed()) {
            if (getIndexer() instanceof SpatialIndexer && getValue() != null) {
                SpatialIndexer spatialIndexer = (SpatialIndexer) getIndexer();
                ReadableStream<NitriteId> idSet = spatialIndexer.findWithin(getCollectionName(), getField(), getValue());
                return idSet.toSet();
            } else {
                throw new FilterException(getValue() + " is not a Geometry");
            }
        }
        return new LinkedHashSet<>();
    }
}
