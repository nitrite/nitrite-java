/*
 * Copyright (c) 2017-2024 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.dizitart.no2.spatial;

import net.sf.geographiclib.GeodesicData;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.repository.Cursor;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;

import java.util.Random;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static net.sf.geographiclib.Geodesic.WGS84;
import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.collection.FindOptions.orderBy;
import static org.dizitart.no2.common.SortOrder.Ascending;
import static org.dizitart.no2.common.util.Iterables.setOf;
import static org.dizitart.no2.spatial.GeoSpatialTestBase.TestLocations.*;
import static org.dizitart.no2.spatial.SpatialFluentFilter.where;
import static org.dizitart.no2.spatial.SpatialIndexer.SPATIAL_INDEX;
import static org.junit.Assert.assertEquals;

/**
 * Test cases to check whether the Spatial distance search is properly converting meters to/from degrees and
 * accounting for the curvature of the Earth.
 * <p>
 * From the <a href="https://nitrite.dizitart.com/java-sdk/filter/index.html#near-filter">"near filter"
 * documentation</a>, it is clear that the intended use is for the arguments to represent (1) a geo-coordinate
 * as a lat/long pair; and (2) a distance in meters.
 * <p>
 * This appears to disagree with the example provided in <pre>nitrite-spatial/README.md</pre>, which makes no
 * mention of distance units uses a coordinate value outside the space of possible lat/long coordinates.
 **/
public class GeoSpatialFindNearTest extends GeoSpatialTestBase {

    /** 2.5 kilometers, in meters */
    private static final double DIST_2500_M = 2_500.0d;
    /** 20 millimeters, in meters */
    private static final double DIST_20_MM = (20.0d / 1000.0d);

    /** Just sorting by "id" field so that JUnit's "Expected" vs. "Actual" output is easier to visually inspect. */
    public static final FindOptions ORDER_BY_ID = orderBy("id", Ascending);
    public static final Random RANDOM = new Random();

    @Test
    public void testNearFilter_ObjectRepository_FindsCorrectSubset() {
        repository.createIndex(IndexOptions.indexOptions(SPATIAL_INDEX), "geometry");
        Cursor<SpatialData> cursor = repository.find(
            where("geometry").near(centerPt.getCoordinate(), DIST_2500_M),
            ORDER_BY_ID);

        assertEquals("Near filter should only find two locations within 2.5km",
            setOf(obj2_950m_ESE, obj3_930m_W),
            cursor.toSet());
    }

    @Test
    public void testNearFilter_NitriteCollection_FindsCorrectSubset() {
        collection.createIndex(IndexOptions.indexOptions(SPATIAL_INDEX), "location");

        DocumentCursor cursor = collection.find(
            where("location").near(centerPt.getCoordinate(), DIST_2500_M),
            ORDER_BY_ID);

        assertEquals("Near filter should only find two locations within 2.5km",
            setOf(doc2_950m_ESE, doc3_930m_W),
            cursor.toList().stream().map(this::trimMeta).collect(Collectors.toSet()));
    }

    @Test
    public void testNearFilter_ObjectRepository_SimpleEquatorDistances() {

        repository.insert(new SpatialData(randLong(), readPoint("POINT(0.0 1.00)"))); // 111km from (0,0)
        repository.insert(new SpatialData(randLong(), readPoint("POINT(0.0 0.01)"))); // 1.11km from (0,0)

        repository.createIndex(IndexOptions.indexOptions(SPATIAL_INDEX), "geometry");

        Cursor<SpatialData> cursor = repository.find(where("geometry").near(new Coordinate(0.0, 0.0), 1120.0));

        assertEquals("Near filter should find 1 location within 1.12km", 1, cursor.toList().size());
    }

    @Test
    public void testNearFilter_NitriteCollection_SimpleEquatorDistances() {
        collection.insert(createDocument("key", randLong()).put("location", readPoint("POINT(0.0 1.00)")));
        collection.insert(createDocument("key", randLong()).put("location", readPoint("POINT(0.0 0.01)")));
        collection.createIndex(IndexOptions.indexOptions(SPATIAL_INDEX), "location");

        DocumentCursor cursor = collection.find(where("location").near(new Coordinate(0.0, 0.0), 1120.0));

        assertEquals("Near filter should find 1 location within 1.12km", 1, cursor.toList().size());
    }

    @Test
    public void testNearFilter_ObjectRepository_Simple60NDistances() {

        repository.insert(new SpatialData(randLong(), readPoint("POINT(60.0 1.00)"))); // ~56km from (60,0)
        repository.insert(new SpatialData(randLong(), readPoint("POINT(60.0 2.00)"))); // ~111km from (60,0)
        repository.insert(new SpatialData(randLong(), readPoint("POINT(61.0 0.00)"))); // ~111km from (60,0)
        repository.insert(new SpatialData(randLong(), readPoint("POINT(62.0 0.00)"))); // ~222km from (60,0)

        repository.createIndex(IndexOptions.indexOptions(SPATIAL_INDEX), "geometry");

        Cursor<SpatialData> cursor = repository.find(
            where("geometry").near(new Coordinate(60.0, 0.0), 112_000.0));

        assertEquals("Near filter should find 3 locations within 1.12km", 3, cursor.toList().size());
    }

    @Test
    public void testNearFilter_NitriteCollection_Simple60NDistances() {
        collection.insert(createDocument("key", randLong()).put("location", readPoint("POINT(60.0 1.00)")));  // ~56km from (60,0)
        collection.insert(createDocument("key", randLong()).put("location", readPoint("POINT(60.0 2.00)")));  // ~111km from (60,0)
        collection.insert(createDocument("key", randLong()).put("location", readPoint("POINT(61.0 0.00)")));  // ~111km from (60,0)
        collection.insert(createDocument("key", randLong()).put("location", readPoint("POINT(62.0 0.00)")));  // ~222km from (60,0)

        // This point is outside the circle but inside the bounding box
        collection.insert(createDocument("key", randLong()).put("location", readPoint("POINT(60.75 1.75)")));  // ~127km from (60,0)

        collection.createIndex(IndexOptions.indexOptions(SPATIAL_INDEX), "location");

        DocumentCursor cursor = collection.find(where("location").near(new Coordinate(60.0, 0.0), 112_000.0));

        assertEquals("Near filter should find 3 locations within 1.12km", 3, cursor.toList().size());
    }

    private static long randLong() {
        return RANDOM.nextLong();
    }

    @Test
    public void testNearFilter_ObjectRepository_TinyDistance_ReturnsNoResults() {
        Coordinate coordinate = centerPt.getCoordinate();

        Cursor<SpatialData> cursor = repository.find(where("geometry").near(coordinate, DIST_20_MM));
        assertEquals("Near filter should return no results within 20mm distance", emptySet(), cursor.toSet());
    }

    @Test
    public void testNearFilter_NitriteCollection_TinyDistance_ReturnsNoResults() {
        Coordinate coordinate = centerPt.getCoordinate();

        collection.createIndex(IndexOptions.indexOptions(SPATIAL_INDEX), "location");
        DocumentCursor cursor = collection.find(where("location").near(coordinate, DIST_20_MM));
        assertEquals("Near filter should return no results within 20mm distance", emptySet(), cursor.toSet());
    }

    @Test
    public void testGeoLibrary_Distance_SanityCheck() {
        final double EPSILON_10M = 10.0;
        // All of these distances should be within 10 metres of what was estimated on Google Earth.

        GeodesicData s12 = WGS84.Inverse(centerPt.getX(), centerPt.getY(), pt2_950m_ESE.getX(), pt2_950m_ESE.getY());
        assertEquals(s12.s12, 950.0, EPSILON_10M);

        GeodesicData s13 = WGS84.Inverse(centerPt.getX(), centerPt.getY(), pt3_930m_W.getX(), pt3_930m_W.getY());
        assertEquals(s13.s12, 930.0, EPSILON_10M);

        GeodesicData s14 = WGS84.Inverse(centerPt.getX(), centerPt.getY(), pt4_2750m_WSW.getX(), pt4_2750m_WSW.getY());
        assertEquals(s14.s12, 2750.0, EPSILON_10M);
    }
}
