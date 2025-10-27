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
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.util.GeometricShapeFactory;

/**
 * A spatial filter that finds geometries near a point within a specified distance.
 * 
 * <p>This filter automatically detects whether the coordinates are geographic (latitude/longitude)
 * or Cartesian, and applies the appropriate distance calculation:</p>
 * <ul>
 *   <li><strong>Geographic coordinates</strong> (within ±90° lat, ±180° lon): Uses geodesic distance
 *       on Earth's WGS84 ellipsoid, with distance specified in meters.</li>
 *   <li><strong>Cartesian coordinates</strong> (outside those bounds): Uses simple Euclidean distance,
 *       with distance in the same units as the coordinates.</li>
 * </ul>
 * 
 * <p><strong>Recommendation:</strong> For new code, use {@link GeoPoint} and {@link GeoNearFilter} 
 * for explicit geographic coordinate handling with better type safety and no auto-detection ambiguity.</p>
 * 
 * <p><strong>Note:</strong> Combined with two-pass query execution in {@link SpatialIndex}, 
 * this filter provides accurate results by eliminating false positives from bounding box approximation.</p>
 * 
 * @since 4.0
 * @author Anindya Chatterjee
 */
class NearFilter extends WithinFilter {
    NearFilter(String field, Coordinate point, Double distance) {
        super(field, createCircle(point, distance));
    }

    NearFilter(String field, Point point, Double distance) {
        super(field, createCircle(point.getCoordinate(), distance));
    }

    private static Geometry createCircle(Coordinate center, double radiusMeters) {
        GeometricShapeFactory shapeFactory = new GeometricShapeFactory();
        shapeFactory.setNumPoints(64);
        shapeFactory.setCentre(center);
        
        // Determine if we're dealing with geographic coordinates (lat/long)
        // or simple Cartesian coordinates
        double radiusInDegrees;
        if (GeodesicUtils.isGeographic(center)) {
            // Convert meters to degrees accounting for Earth's curvature
            radiusInDegrees = GeodesicUtils.metersToDegreesRadius(center, radiusMeters);
        } else {
            // For non-geographic coordinates, use the radius as-is
            // This maintains backward compatibility with existing tests
            radiusInDegrees = radiusMeters;
        }
        
        shapeFactory.setSize(radiusInDegrees * 2);
        return shapeFactory.createCircle();
    }

    @Override
    public String toString() {
        return "(" + getField() + " nears " + getValue() + ")";
    }
}
