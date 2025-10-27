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
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.filters.FluentFilter;
import org.dizitart.no2.index.IndexOptions;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.spatial.SpatialFluentFilter.where;
import static org.dizitart.no2.spatial.SpatialIndexer.SPATIAL_INDEX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test cases for NearFilter with real-world geodesic coordinates.
 * These tests verify that the NearFilter properly handles lat/long coordinates
 * on Earth's surface and correctly converts meters to degrees.
 *
 * @author Anindya Chatterjee
 */
public class GeodesicNearFilterTest extends BaseSpatialTest {

    @Test
    public void testNearFilterAtEquator() throws ParseException {
        WKTReader reader = new WKTReader();
        
        // Center point at (0°, 0°) - Atlantic Ocean at the equator
        Point centerPoint = (Point) reader.read("POINT (0 0)");
        
        // Point approximately 1km east: at equator, 1 degree ≈ 111km
        // So 0.01 degrees ≈ 1.11km
        Point point1kmEast = (Point) reader.read("POINT (0.01 0)");
        
        // Point approximately 111km east (1 degree at equator)
        Point point111kmEast = (Point) reader.read("POINT (1 0)");
        
        // Point approximately 222km east (2 degrees at equator)
        Point point222kmEast = (Point) reader.read("POINT (2 0)");
        
        NitriteCollection testCollection = db.getCollection("geodesic_test");
        testCollection.createIndex(IndexOptions.indexOptions(SPATIAL_INDEX), "location");
        
        Document docCenter = createDocument("name", "center")
            .put("location", centerPoint);
        Document doc1km = createDocument("name", "1km_east")
            .put("location", point1kmEast);
        Document doc111km = createDocument("name", "111km_east")
            .put("location", point111kmEast);
        Document doc222km = createDocument("name", "222km_east")
            .put("location", point222kmEast);
        
        testCollection.insert(docCenter, doc1km, doc111km, doc222km);
        
        System.err.println("DEBUG: Inserted documents:");
        for (Document doc : testCollection.find().toList()) {
            System.err.println("  - " + doc.get("name") + " at " + doc.get("location"));
        }
        
        // Test 1: Within 2km should return center and 1km_east only
        DocumentCursor within2km = testCollection.find(where("location").near(centerPoint, 2000.0));
        int count = 0;
        System.err.println("DEBUG: Iterating results...");
        for (Document doc : within2km.toList()) {
            count++;
            System.err.println("  Result " + count + ": " + doc.get("name") + " at " + doc.get("location"));
        }
        System.err.println("DEBUG: Found " + count + " results within 2km");
        assertEquals("Should find 2 points within 2km", 2, count);
        
        // Test 2: Within 20cm should return only center
        DocumentCursor within20cm = testCollection.find(where("location").near(centerPoint, 0.2));
        assertEquals("Should find only center within 20cm", 1, within20cm.size());
        
        // Test 3: Within 150km should return center, 1km, and 111km
        DocumentCursor within150km = testCollection.find(where("location").near(centerPoint, 150000.0));
        assertEquals("Should find 3 points within 150km", 3, within150km.size());
        
        testCollection.remove(FluentFilter.where("name").eq("center"));
        testCollection.remove(FluentFilter.where("name").eq("1km_east"));
        testCollection.remove(FluentFilter.where("name").eq("111km_east"));
        testCollection.remove(FluentFilter.where("name").eq("222km_east"));
    }
    
    @Test
    public void testNearFilterWithCoordinate() throws ParseException {
        WKTReader reader = new WKTReader();
        
        Point centerPoint = (Point) reader.read("POINT (0 0)");
        Coordinate centerCoord = centerPoint.getCoordinate();
        
        Point point500m = (Point) reader.read("POINT (0.005 0)");
        Point point5km = (Point) reader.read("POINT (0.05 0)");
        
        NitriteCollection testCollection = db.getCollection("geodesic_coord_test");
        testCollection.createIndex(IndexOptions.indexOptions(SPATIAL_INDEX), "location");
        
        Document docCenter = createDocument("name", "center")
            .put("location", centerPoint);
        Document doc500m = createDocument("name", "500m_east")
            .put("location", point500m);
        Document doc5km = createDocument("name", "5km_east")
            .put("location", point5km);
        
        testCollection.insert(docCenter, doc500m, doc5km);
        
        // Test with Coordinate instead of Point
        DocumentCursor within1km = testCollection.find(where("location").near(centerCoord, 1000.0));
        assertEquals("Should find 2 points within 1km", 2, within1km.size());
        
        DocumentCursor within10km = testCollection.find(where("location").near(centerCoord, 10000.0));
        assertEquals("Should find all 3 points within 10km", 3, within10km.size());
        
        testCollection.remove(FluentFilter.where("name").eq("center"));
        testCollection.remove(FluentFilter.where("name").eq("500m_east"));
        testCollection.remove(FluentFilter.where("name").eq("5km_east"));
    }
    
    @Test
    public void testNearFilterAtMidLatitude() throws ParseException {
        WKTReader reader = new WKTReader();
        
        // Center point at 45°N (e.g., near Minneapolis, MN)
        // At 45°N, longitude degrees are shorter: ~78.8km per degree
        Point centerPoint = (Point) reader.read("POINT (-93.2650 45.0000)");
        
        // Point approximately 1km east at 45°N
        Point point1kmEast = (Point) reader.read("POINT (-93.2523 45.0000)");
        
        // Point approximately 80km east
        Point point80kmEast = (Point) reader.read("POINT (-92.2650 45.0000)");
        
        NitriteCollection testCollection = db.getCollection("geodesic_midlat_test");
        testCollection.createIndex(IndexOptions.indexOptions(SPATIAL_INDEX), "location");
        
        Document docCenter = createDocument("name", "center")
            .put("location", centerPoint);
        Document doc1km = createDocument("name", "1km_east")
            .put("location", point1kmEast);
        Document doc80km = createDocument("name", "80km_east")
            .put("location", point80kmEast);
        
        testCollection.insert(docCenter, doc1km, doc80km);
        
        // Within 2km should find center and 1km_east
        DocumentCursor within2km = testCollection.find(where("location").near(centerPoint, 2000.0));
        assertEquals("Should find 2 points within 2km at 45°N", 2, within2km.size());
        
        // Within 100km should find all points
        DocumentCursor within100km = testCollection.find(where("location").near(centerPoint, 100000.0));
        assertEquals("Should find all 3 points within 100km", 3, within100km.size());
        
        testCollection.remove(FluentFilter.where("name").eq("center"));
        testCollection.remove(FluentFilter.where("name").eq("1km_east"));
        testCollection.remove(FluentFilter.where("name").eq("80km_east"));
    }
    
    @Test
    public void testNearFilterNorthSouth() throws ParseException {
        WKTReader reader = new WKTReader();
        
        // Test north-south distances (latitude changes)
        // These are consistent across all longitudes: ~111km per degree
        Point centerPoint = (Point) reader.read("POINT (0 0)");
        
        // Point approximately 1km north
        Point point1kmNorth = (Point) reader.read("POINT (0 0.009)");
        
        // Point approximately 111km north (1 degree)
        Point point111kmNorth = (Point) reader.read("POINT (0 1)");
        
        NitriteCollection testCollection = db.getCollection("geodesic_ns_test");
        testCollection.createIndex(IndexOptions.indexOptions(SPATIAL_INDEX), "location");
        
        Document docCenter = createDocument("name", "center")
            .put("location", centerPoint);
        Document doc1km = createDocument("name", "1km_north")
            .put("location", point1kmNorth);
        Document doc111km = createDocument("name", "111km_north")
            .put("location", point111kmNorth);
        
        testCollection.insert(docCenter, doc1km, doc111km);
        
        // Within 2km should find center and 1km_north
        DocumentCursor within2km = testCollection.find(where("location").near(centerPoint, 2000.0));
        assertEquals("Should find 2 points within 2km", 2, within2km.size());
        
        // Within 200km should find all points
        DocumentCursor within200km = testCollection.find(where("location").near(centerPoint, 200000.0));
        assertEquals("Should find all 3 points within 200km", 3, within200km.size());
        
        testCollection.remove(FluentFilter.where("name").eq("center"));
        testCollection.remove(FluentFilter.where("name").eq("1km_north"));
        testCollection.remove(FluentFilter.where("name").eq("111km_north"));
    }
    
    @Test
    public void testNearFilterSmallDistances() throws ParseException {
        WKTReader reader = new WKTReader();
        
        Point centerPoint = (Point) reader.read("POINT (0 0)");
        
        // Very small distances
        Point point10m = (Point) reader.read("POINT (0.00009 0)");  // ~10m
        Point point100m = (Point) reader.read("POINT (0.0009 0)");   // ~100m
        
        NitriteCollection testCollection = db.getCollection("geodesic_small_test");
        testCollection.createIndex(IndexOptions.indexOptions(SPATIAL_INDEX), "location");
        
        Document docCenter = createDocument("name", "center")
            .put("location", centerPoint);
        Document doc10m = createDocument("name", "10m_east")
            .put("location", point10m);
        Document doc100m = createDocument("name", "100m_east")
            .put("location", point100m);
        
        testCollection.insert(docCenter, doc10m, doc100m);
        
        // Within 50m should find center and 10m only
        DocumentCursor within50m = testCollection.find(where("location").near(centerPoint, 50.0));
        assertEquals("Should find 2 points within 50m", 2, within50m.size());
        
        // Within 5m should find only center
        DocumentCursor within5m = testCollection.find(where("location").near(centerPoint, 5.0));
        assertEquals("Should find only center within 5m", 1, within5m.size());
        
        testCollection.remove(FluentFilter.where("name").eq("center"));
        testCollection.remove(FluentFilter.where("name").eq("10m_east"));
        testCollection.remove(FluentFilter.where("name").eq("100m_east"));
    }
}
