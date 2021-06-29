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

import org.dizitart.no2.filters.Filter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

/**
 * Fluent filter api for spatial data
 *
 * @author Anindya Chatterjee
 * @since 4.0.0
 */
public class FluentFilter {
    private String field;

    private FluentFilter() {
    }

    /**
     * Where clause for fluent filter.
     *
     * @param field the field
     * @return the fluent filter
     */
    public static FluentFilter where(String field) {
        FluentFilter filter = new FluentFilter();
        filter.field = field;
        return filter;
    }

    /**
     * Creates an spatial filter which matches documents where the spatial data
     * of a field intersects the specified {@link Geometry} value.
     *
     * @param geometry the geometry
     * @return the filter
     */
    public Filter intersects(Geometry geometry) {
        return new IntersectsFilter(field, geometry);
    }

    /**
     * Creates a spatial filter which matches documents where the spatial data
     * of a field is within the specified {@link Geometry} value.
     *
     * @param geometry the geometry
     * @return the filter
     */
    public Filter within(Geometry geometry) {
        return new WithinFilter(field, geometry);
    }

    /**
     * Creates a spatial filter which matches documents where the spatial data
     * of a field is near the specified coordinate.
     *
     * @param point    the point
     * @param distance the distance
     * @return the filter
     */
    public Filter near(Coordinate point, Double distance) {
        return new NearFilter(field, point, distance);
    }

    /**
     * Creates a spatial filter which matches documents where the spatial data
     * of a field is near the specified point.
     *
     * @param point    the point
     * @param distance the distance
     * @return the filter
     */
    public Filter near(Point point, Double distance) {
        return new NearFilter(field, point, distance);
    }
}
