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

import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import org.locationtech.jts.geom.Coordinate;

/**
 * Utility class for geodesic distance calculations on Earth's surface.
 * This class handles the conversion between meters and degrees of latitude/longitude,
 * accounting for the curvature of the Earth using the WGS84 ellipsoid model.
 * 
 * <p>This class is used internally by {@link NearFilter} for backward compatibility
 * with auto-detection. For new code, use {@link GeoPoint} and {@link GeoNearFilter}
 * for explicit geographic coordinate handling.</p>
 *
 * @since 4.0
 * @author Anindya Chatterjee
 */
class GeodesicUtils {
    private static final Geodesic WGS84 = Geodesic.WGS84;
    
    /**
     * Determines if coordinates appear to be geographic (lat/long) rather than Cartesian.
     * This is a heuristic check based on valid lat/long ranges:
     * - Latitude: -90 to 90
     * - Longitude: -180 to 180
     * 
     * <p><strong>Limitation:</strong> This heuristic may incorrectly classify Cartesian 
     * coordinates that happen to fall within ±90°/±180° range (e.g., game world coordinates).</p>
     * 
     * <p><strong>Recommendation:</strong> For new code, use {@link GeoPoint} and 
     * {@link GeoNearFilter} to explicitly indicate geographic coordinates and avoid 
     * auto-detection ambiguity.</p>
     *
     * @param center the coordinate to check
     * @return true if the coordinate appears to be geographic, false otherwise
     */
    static boolean isGeographic(Coordinate center) {
        double x = center.getX();
        double y = center.getY();
        
        // Check if coordinates fall within valid lat/long ranges
        // We use slightly relaxed bounds to be conservative
        return Math.abs(y) <= 90.0 && Math.abs(x) <= 180.0;
    }
    
    /**
     * Calculates the approximate radius in degrees for a given distance in meters
     * at a specific geographic coordinate. This accounts for the fact that one degree
     * of longitude varies with latitude.
     * 
     * <p>This method calculates geodesic distances in both E-W and N-S directions and 
     * returns the maximum to ensure complete circular coverage. Combined with the 
     * two-pass query execution in {@link SpatialIndex}, this provides accurate results 
     * while maintaining performance.</p>
     *
     * @param center the center coordinate (longitude, latitude)
     * @param radiusMeters the radius in meters
     * @return the approximate radius in degrees
     */
    static double metersToDegreesRadius(Coordinate center, double radiusMeters) {
        double lat = center.getY();
        double lon = center.getX();
        
        // Calculate how many degrees we need to go in different directions
        // to cover the specified radius in meters
        
        // East-West: Calculate a point at the given distance east
        GeodesicData eastPoint = WGS84.Direct(lat, lon, 90.0, radiusMeters);
        double lonDiff = Math.abs(eastPoint.lon2 - lon);
        
        // North-South: Calculate a point at the given distance north
        GeodesicData northPoint = WGS84.Direct(lat, lon, 0.0, radiusMeters);
        double latDiff = Math.abs(northPoint.lat2 - lat);
        
        // Use the maximum of the two to ensure we cover the full circle
        // This creates a slightly larger search area but ensures we don't miss points
        return Math.max(lonDiff, latDiff);
    }
}
