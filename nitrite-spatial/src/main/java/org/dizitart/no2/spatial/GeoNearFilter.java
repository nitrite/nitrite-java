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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.util.GeometricShapeFactory;

/**
 * Spatial filter for finding geometries near a geographic point,
 * using geodesic distance on Earth's surface (WGS84 ellipsoid).
 * 
 * <p>This filter is specifically designed for geographic coordinates (lat/long).
 * It always uses geodesic distance calculations, eliminating the ambiguity
 * of {@link NearFilter}'s auto-detection.</p>
 * 
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * GeoPoint center = new GeoPoint(45.0, -93.2650); // Minneapolis
 * collection.find(where("location").geoNear(center, 5000.0)); // 5km radius
 * }</pre>
 * 
 * <p><strong>Distance Units:</strong> The distance parameter must be in meters.</p>
 * 
 * <p><strong>Accuracy:</strong> This filter uses two-pass query execution for accurate results:
 * Phase 1 performs a fast R-tree bounding box search, and Phase 2 refines results using
 * precise JTS geometric operations to eliminate false positives.</p>
 * 
 * @since 4.3.3
 * @author Anindya Chatterjee
 * @see GeoPoint
 * @see NearFilter
 */
class GeoNearFilter extends WithinFilter {
    
    /**
     * Creates a filter to find geometries near a GeoPoint.
     * 
     * @param field the field to filter on
     * @param point the geographic point to check proximity to
     * @param distanceMeters the maximum distance in meters
     */
    GeoNearFilter(String field, GeoPoint point, Double distanceMeters) {
        super(field, createGeodesicCircle(point.getCoordinate(), distanceMeters));
    }
    
    /**
     * Creates a filter to find geometries near a coordinate.
     * The coordinate is validated to ensure it represents a valid geographic point.
     * 
     * @param field the field to filter on
     * @param point the coordinate to check proximity to (x=longitude, y=latitude)
     * @param distanceMeters the maximum distance in meters
     * @throws IllegalArgumentException if coordinates are not valid geographic coordinates
     */
    GeoNearFilter(String field, Coordinate point, Double distanceMeters) {
        super(field, createGeodesicCircle(validateAndGetCoordinate(point), distanceMeters));
    }
    
    private static Coordinate validateAndGetCoordinate(Coordinate coord) {
        double lat = coord.getY();
        double lon = coord.getX();
        
        if (lat < -90.0 || lat > 90.0) {
            throw new IllegalArgumentException(
                "GeoNearFilter requires valid latitude (-90 to 90), got: " + lat);
        }
        if (lon < -180.0 || lon > 180.0) {
            throw new IllegalArgumentException(
                "GeoNearFilter requires valid longitude (-180 to 180), got: " + lon);
        }
        
        return coord;
    }
    
    private static Geometry createGeodesicCircle(Coordinate center, double radiusMeters) {
        GeometricShapeFactory shapeFactory = new GeometricShapeFactory();
        shapeFactory.setNumPoints(64);
        shapeFactory.setCentre(center);
        
        // Always use geodesic calculations for GeoNearFilter
        double radiusInDegrees = GeodesicUtils.metersToDegreesRadius(center, radiusMeters);
        shapeFactory.setSize(radiusInDegrees * 2);
        return shapeFactory.createCircle();
    }

    @Override
    public String toString() {
        return "(" + getField() + " geoNear " + getValue() + ")";
    }
}
