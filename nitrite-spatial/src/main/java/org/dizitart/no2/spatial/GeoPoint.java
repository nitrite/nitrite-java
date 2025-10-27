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
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.io.Serializable;

/**
 * Represents a geographic point with latitude and longitude coordinates
 * on Earth's surface (WGS84 ellipsoid).
 * 
 * <p>This class provides explicit type safety for geographic coordinates,
 * eliminating the ambiguity of auto-detection. It validates coordinates
 * on construction and provides clear latitude/longitude accessors.</p>
 * 
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * // Create a geographic point for Minneapolis
 * GeoPoint minneapolis = new GeoPoint(45.0, -93.2650);
 * 
 * // Use with GeoNearFilter
 * collection.find(where("location").geoNear(minneapolis, 5000.0));
 * }</pre>
 * 
 * <p><strong>Coordinate Order:</strong> Constructor takes (latitude, longitude)
 * which differs from JTS Point (x, y) = (longitude, latitude) to avoid confusion.</p>
 *
 * @since 4.3.3
 * @author Anindya Chatterjee
 */
public class GeoPoint implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final GeometryFactory FACTORY = new GeometryFactory(new PrecisionModel(), 4326);
    private final Point point;
    private final double latitude;
    private final double longitude;
    
    /**
     * Creates a new GeoPoint with the specified geographic coordinates.
     * 
     * @param latitude the latitude in degrees (-90 to 90)
     * @param longitude the longitude in degrees (-180 to 180)
     * @throws IllegalArgumentException if coordinates are out of valid range
     */
    public GeoPoint(double latitude, double longitude) {
        validateCoordinates(latitude, longitude);
        this.latitude = latitude;
        this.longitude = longitude;
        this.point = FACTORY.createPoint(new Coordinate(longitude, latitude));
    }
    
    /**
     * Creates a GeoPoint from a JTS Coordinate.
     * The coordinate's Y value is treated as latitude and X as longitude.
     * 
     * @param coordinate the coordinate (x=longitude, y=latitude)
     * @throws IllegalArgumentException if coordinates are out of valid range
     */
    public GeoPoint(Coordinate coordinate) {
        this(coordinate.getY(), coordinate.getX());
    }
    
    private void validateCoordinates(double latitude, double longitude) {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException(
                "Latitude must be between -90 and 90 degrees, got: " + latitude);
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException(
                "Longitude must be between -180 and 180 degrees, got: " + longitude);
        }
    }
    
    /**
     * Gets the latitude in degrees.
     * 
     * @return the latitude (-90 to 90)
     */
    public double getLatitude() {
        return latitude;
    }
    
    /**
     * Gets the longitude in degrees.
     * 
     * @return the longitude (-180 to 180)
     */
    public double getLongitude() {
        return longitude;
    }
    
    /**
     * Gets the underlying JTS Point.
     * 
     * @return the JTS Point representation
     */
    public Point getPoint() {
        return point;
    }
    
    /**
     * Gets the coordinate of this GeoPoint.
     * 
     * @return the coordinate (x=longitude, y=latitude)
     */
    public Coordinate getCoordinate() {
        return point.getCoordinate();
    }
    
    @Override
    public String toString() {
        return String.format("GeoPoint(lat=%.6f, lon=%.6f)", latitude, longitude);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GeoPoint other = (GeoPoint) obj;
        return Double.compare(latitude, other.latitude) == 0 
            && Double.compare(longitude, other.longitude) == 0;
    }
    
    @Override
    public int hashCode() {
        long latBits = Double.doubleToLongBits(latitude);
        long lonBits = Double.doubleToLongBits(longitude);
        return (int) (latBits ^ (latBits >>> 32) ^ lonBits ^ (lonBits >>> 32));
    }
}
