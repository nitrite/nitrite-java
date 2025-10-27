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
 * A fluent filter api for spatial queries.
 * 
 * @since 4.0
 * @author Anindya Chatterjee
 */
public class SpatialFluentFilter {
    private String field;

    private SpatialFluentFilter() {
    }

    /**
     * Creates a new {@link SpatialFluentFilter} instance with the specified field.
     *
     * @param field the field to filter on
     * @return the new {@link SpatialFluentFilter} instance
     */
    public static SpatialFluentFilter where(String field) {
        SpatialFluentFilter filter = new SpatialFluentFilter();
        filter.field = field;
        return filter;
    }

    /**
     * Creates a spatial filter which matches documents where the spatial data
     * of a field intersects the specified {@link Geometry} value.
     *
     * @param geometry the geometry to intersect with
     * @return the new {@link Filter} instance
     */
    public Filter intersects(Geometry geometry) {
        return new IntersectsFilter(field, geometry);
    }

    /**
     * Creates a spatial filter which matches documents where the spatial data
     * of a field is within the specified {@link Geometry} value.
     *
     * @param geometry the geometry to check for containment within
     * @return the new {@link Filter} instance
     */
    public Filter within(Geometry geometry) {
        return new WithinFilter(field, geometry);
    }

    /**
     * Creates a spatial filter which matches documents where the spatial data
     * of a field is near the specified coordinate.
     *
     * @param point    the coordinate to check proximity to
     * @param distance the maximum distance to consider
     * @return the new {@link Filter} instance
     */
    public Filter near(Coordinate point, Double distance) {
        return new NearFilter(field, point, distance);
    }

    /**
     * Creates a spatial filter which matches documents where the spatial data
     * of a field is near the specified point.
     *
     * @param point    the point to check proximity to
     * @param distance the maximum distance to consider
     * @return the new {@link Filter} instance
     */
    public Filter near(Point point, Double distance) {
        return new NearFilter(field, point, distance);
    }
    
    /**
     * Creates a spatial filter for geographic coordinates that matches documents 
     * where the spatial data is near the specified point, using geodesic distance.
     * 
     * <p>This method is specifically for geographic coordinates (latitude/longitude)
     * and always uses geodesic distance calculations on Earth's WGS84 ellipsoid.</p>
     * 
     * <p><strong>Usage Example:</strong></p>
     * <pre>{@code
     * GeoPoint minneapolis = new GeoPoint(45.0, -93.2650);
     * collection.find(where("location").geoNear(minneapolis, 5000.0)); // 5km radius
     * }</pre>
     *
     * @param point the geographic point to check proximity to
     * @param distanceMeters the maximum distance in meters
     * @return the new {@link Filter} instance
     */
    public Filter geoNear(GeoPoint point, Double distanceMeters) {
        return new GeoNearFilter(field, point, distanceMeters);
    }
    
    /**
     * Creates a spatial filter for geographic coordinates that matches documents 
     * where the spatial data is near the specified coordinate, using geodesic distance.
     * 
     * <p>The coordinate is validated to ensure it represents valid geographic coordinates.</p>
     *
     * @param point the coordinate to check proximity to (x=longitude, y=latitude)
     * @param distanceMeters the maximum distance in meters
     * @return the new {@link Filter} instance
     * @throws IllegalArgumentException if coordinates are not valid geographic coordinates
     */
    public Filter geoNear(Coordinate point, Double distanceMeters) {
        return new GeoNearFilter(field, point, distanceMeters);
    }
}
