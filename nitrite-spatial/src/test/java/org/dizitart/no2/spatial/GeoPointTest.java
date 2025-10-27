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

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.spatial.SpatialFluentFilter.where;
import static org.dizitart.no2.spatial.SpatialIndexer.SPATIAL_INDEX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test cases for GeoPoint and GeoNearFilter functionality.
 * 
 * @author Anindya Chatterjee
 */
public class GeoPointTest extends BaseSpatialTest {
    
    @Test
    public void testGeoPointCreation() {
        GeoPoint point = new GeoPoint(45.0, -93.2650);
        assertEquals(45.0, point.getLatitude(), 0.0001);
        assertEquals(-93.2650, point.getLongitude(), 0.0001);
    }
    
    @Test
    public void testGeoPointFromCoordinate() {
        Coordinate coord = new Coordinate(-93.2650, 45.0); // x=lon, y=lat
        GeoPoint point = new GeoPoint(coord);
        assertEquals(45.0, point.getLatitude(), 0.0001);
        assertEquals(-93.2650, point.getLongitude(), 0.0001);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGeoPointInvalidLatitudeTooHigh() {
        new GeoPoint(91.0, 0.0);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGeoPointInvalidLatitudeTooLow() {
        new GeoPoint(-91.0, 0.0);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGeoPointInvalidLongitudeTooHigh() {
        new GeoPoint(0.0, 181.0);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGeoPointInvalidLongitudeTooLow() {
        new GeoPoint(0.0, -181.0);
    }
    
    @Test
    public void testGeoPointBoundaryValues() {
        // Test boundary values
        GeoPoint northPole = new GeoPoint(90.0, 0.0);
        assertEquals(90.0, northPole.getLatitude(), 0.0001);
        
        GeoPoint southPole = new GeoPoint(-90.0, 0.0);
        assertEquals(-90.0, southPole.getLatitude(), 0.0001);
        
        GeoPoint dateLine = new GeoPoint(0.0, 180.0);
        assertEquals(180.0, dateLine.getLongitude(), 0.0001);
        
        GeoPoint antiMeridian = new GeoPoint(0.0, -180.0);
        assertEquals(-180.0, antiMeridian.getLongitude(), 0.0001);
    }
    
    @Test
    public void testGeoPointSerialization() {
        NitriteCollection testCollection = db.getCollection("geo_point_test");
        
        GeoPoint minneapolis = new GeoPoint(45.0, -93.2650);
        Document doc = createDocument("name", "Minneapolis")
                .put("location", minneapolis);
        
        testCollection.insert(doc);
        
        Document retrieved = testCollection.find().firstOrNull();
        assertNotNull(retrieved);
        
        GeoPoint retrievedPoint = retrieved.get("location", GeoPoint.class);
        assertNotNull(retrievedPoint);
        assertEquals(45.0, retrievedPoint.getLatitude(), 0.0001);
        assertEquals(-93.2650, retrievedPoint.getLongitude(), 0.0001);
        
        testCollection.remove(FluentFilter.where("name").eq("Minneapolis"));
    }
    
    @Test
    public void testGeoNearFilterWithGeoPoint() {
        NitriteCollection testCollection = db.getCollection("geo_near_test");
        testCollection.createIndex(IndexOptions.indexOptions(SPATIAL_INDEX), "location");
        
        GeoPoint center = new GeoPoint(0.001, 0.001);
        GeoPoint point1km = new GeoPoint(0.001, 0.011);  // ~1.1km east
        GeoPoint point5km = new GeoPoint(0.001, 0.051);  // ~5.7km east
        GeoPoint point100km = new GeoPoint(0.001, 1.001); // ~111km east
        
        Document docCenter = createDocument("name", "center").put("location", center);
        Document doc1km = createDocument("name", "1km").put("location", point1km);
        Document doc5km = createDocument("name", "5km").put("location", point5km);
        Document doc100km = createDocument("name", "100km").put("location", point100km);
        
        testCollection.insert(docCenter, doc1km, doc5km, doc100km);
        
        // Test: Within 2km should find center and 1km
        DocumentCursor within2km = testCollection.find(where("location").geoNear(center, 2000.0));
        assertEquals("Should find 2 points within 2km", 2, within2km.size());
        
        // Test: Within 10km should find center, 1km, and 5km
        DocumentCursor within10km = testCollection.find(where("location").geoNear(center, 10000.0));
        assertEquals("Should find 3 points within 10km", 3, within10km.size());
        
        // Test: Within 200km should find all points
        DocumentCursor within200km = testCollection.find(where("location").geoNear(center, 200000.0));
        assertEquals("Should find all 4 points within 200km", 4, within200km.size());
        
        testCollection.remove(FluentFilter.where("name").eq("center"));
        testCollection.remove(FluentFilter.where("name").eq("1km"));
        testCollection.remove(FluentFilter.where("name").eq("5km"));
        testCollection.remove(FluentFilter.where("name").eq("100km"));
    }
    
    @Test
    public void testGeoNearFilterWithCoordinate() {
        NitriteCollection testCollection = db.getCollection("geo_near_coord_test");
        testCollection.createIndex(IndexOptions.indexOptions(SPATIAL_INDEX), "location");
        
        GeoPoint center = new GeoPoint(0.001, 0.001);
        GeoPoint nearby = new GeoPoint(0.010, 0.001);  // ~1km north
        
        testCollection.insert(
            createDocument("name", "center").put("location", center),
            createDocument("name", "nearby").put("location", nearby)
        );
        
        // Test with Coordinate instead of GeoPoint
        Coordinate centerCoord = new Coordinate(0.001, 0.001); // lon, lat
        DocumentCursor results = testCollection.find(where("location").geoNear(centerCoord, 2000.0));
        
        assertEquals("Should find 2 points within 2km", 2, results.size());
        
        testCollection.remove(FluentFilter.where("name").eq("center"));
        testCollection.remove(FluentFilter.where("name").eq("nearby"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGeoNearFilterInvalidCoordinate() {
        NitriteCollection testCollection = db.getCollection("invalid_test");
        testCollection.createIndex(IndexOptions.indexOptions(SPATIAL_INDEX), "location");
        
        // Invalid latitude > 90
        Coordinate invalid = new Coordinate(0.0, 100.0);
        testCollection.find(where("location").geoNear(invalid, 1000.0));
    }
    
    @Test
    public void testGeoNearFilterMidLatitude() {
        NitriteCollection testCollection = db.getCollection("geo_near_midlat_test");
        testCollection.createIndex(IndexOptions.indexOptions(SPATIAL_INDEX), "location");
        
        // Test at 45°N where longitude degrees are shorter
        GeoPoint center = new GeoPoint(45.0, -93.2650);
        GeoPoint nearby = new GeoPoint(45.01, -93.2650);  // ~1.1km north
        GeoPoint faraway = new GeoPoint(45.0, -92.2650);  // ~80km east
        
        testCollection.insert(
            createDocument("name", "center").put("location", center),
            createDocument("name", "nearby").put("location", nearby),
            createDocument("name", "faraway").put("location", faraway)
        );
        
        DocumentCursor within2km = testCollection.find(where("location").geoNear(center, 2000.0));
        assertEquals("Should find 2 points within 2km at 45°N", 2, within2km.size());
        
        DocumentCursor within100km = testCollection.find(where("location").geoNear(center, 100000.0));
        assertEquals("Should find all 3 points within 100km", 3, within100km.size());
        
        testCollection.remove(FluentFilter.where("name").eq("center"));
        testCollection.remove(FluentFilter.where("name").eq("nearby"));
        testCollection.remove(FluentFilter.where("name").eq("faraway"));
    }
    
    @Test
    public void testGeoPointToString() {
        GeoPoint point = new GeoPoint(45.123456, -93.654321);
        String str = point.toString();
        assertNotNull(str);
        // Should contain formatted lat/lon
        assert(str.contains("45.123456"));
        assert(str.contains("-93.654321"));
    }
}
