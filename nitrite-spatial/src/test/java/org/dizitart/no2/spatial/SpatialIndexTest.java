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

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.FindPlan;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.filters.EqualsFilter;
import org.dizitart.no2.filters.FluentFilter;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.dizitart.no2.repository.Cursor;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.filters.Filter.and;
import static org.dizitart.no2.index.IndexType.UNIQUE;
import static org.dizitart.no2.spatial.FluentFilter.where;
import static org.dizitart.no2.spatial.SpatialIndexer.SPATIAL_INDEX;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee
 */
public class SpatialIndexTest extends BaseSpatialTest {

    @Test
    public void testIntersect() throws ParseException {
        WKTReader reader = new WKTReader();
        Geometry search = reader.read("POLYGON ((490 490, 536 490, 536 515, 490 515, 490 490))");

        Cursor<SpatialData> cursor = repository.find(where("geometry").intersects(search));
        assertEquals(cursor.size(), 2);
        assertEquals(cursor.toList(), Arrays.asList(object1, object2));

        collection.createIndex(IndexOptions.indexOptions(SPATIAL_INDEX), "location");
        DocumentCursor cursor1 = collection.find(where("location").intersects(search));
        assertEquals(cursor1.size(), 2);
        assertEquals(cursor1.toList().stream().map(this::trimMeta).collect(Collectors.toList()), Arrays.asList(doc1, doc2));
    }

    @Test
    public void testWithin() throws ParseException {
        WKTReader reader = new WKTReader();
        Geometry search = reader.read("POLYGON ((490 490, 536 490, 536 515, 490 515, 490 490))");

        Cursor<SpatialData> cursor = repository.find(where("geometry").within(search));
        assertEquals(cursor.size(), 1);
        assertEquals(cursor.toList(), Collections.singletonList(object1));

        collection.createIndex(IndexOptions.indexOptions(SPATIAL_INDEX), "location");
        DocumentCursor cursor1 = collection.find(where("location").within(search));
        assertEquals(cursor1.size(), 1);
        assertEquals(cursor1.toList().stream().map(this::trimMeta).collect(Collectors.toList()), Collections.singletonList(doc1));
    }

    @Test
    public void testNearPoint() throws ParseException {
        WKTReader reader = new WKTReader();
        Point search = (Point) reader.read("POINT (490 490)");

        Cursor<SpatialData> cursor = repository.find(where("geometry").near(search, 20.0));
        assertEquals(cursor.size(), 1);
        assertEquals(cursor.toList(), Collections.singletonList(object1));

        collection.createIndex(IndexOptions.indexOptions(SPATIAL_INDEX), "location");
        DocumentCursor cursor1 = collection.find(where("location").near(search, 20.0));
        assertEquals(cursor1.size(), 1);
        assertEquals(cursor1.toList().stream().map(this::trimMeta).collect(Collectors.toList()), Collections.singletonList(doc1));
    }

    @Test
    public void testNearCoordinate() throws ParseException {
        WKTReader reader = new WKTReader();
        Point search = (Point) reader.read("POINT (490 490)");
        Coordinate coordinate = search.getCoordinate();

        Cursor<SpatialData> cursor = repository.find(where("geometry").near(coordinate, 20.0));
        assertEquals(cursor.size(), 1);
        assertEquals(cursor.toList(), Collections.singletonList(object1));

        collection.createIndex(IndexOptions.indexOptions(SPATIAL_INDEX), "location");
        DocumentCursor cursor1 = collection.find(where("location").near(coordinate, 20.0));
        assertEquals(cursor1.size(), 1);
        assertEquals(cursor1.toList().stream().map(this::trimMeta).collect(Collectors.toList()), Collections.singletonList(doc1));
    }

    @Test
    public void testRemoveIndexEntry() throws ParseException {
        WKTReader reader = new WKTReader();
        Geometry search = reader.read("POLYGON ((490 490, 536 490, 536 515, 490 515, 490 490))");
        WriteResult result = repository.remove(where("geometry").within(search));
        assertEquals(result.getAffectedCount(), 1);
    }

    @Test
    public void testUpdateIndex() throws ParseException {
        WKTReader reader = new WKTReader();
        Geometry search = reader.read("POLYGON ((490 490, 536 490, 536 515, 490 515, 490 490))");
        SpatialData update = new SpatialData();
        update.setId(3L);
        update.setGeometry(search);

        WriteResult result = repository.update(update);
        assertEquals(result.getAffectedCount(), 1);
    }

    @Test
    public void testDropAllIndex() {
        repository.dropAllIndices();

        assertFalse(repository.hasIndex("geometry"));
    }

    @Test
    public void testParseGeometry() throws ParseException {
        MVStoreModule storeModule = MVStoreModule.withConfig()
            .filePath((String) null)
            .build();

        db = Nitrite.builder()
            .loadModule(storeModule)
            .loadModule(new SpatialModule())
            .fieldSeparator(".")
            .openOrCreate();

        WKTReader reader = new WKTReader();
        Geometry point = reader.read("POINT(500 505)");
        Document document = createDocument("geom", point);

        NitriteCollection collection = db.getCollection("test");
        collection.insert(document);
        collection.createIndex(IndexOptions.indexOptions(SPATIAL_INDEX), "geom");
        Document doc = collection.find().firstOrNull();

        Document update = doc.clone();
        update.put("geom", reader.read("POINT(0 0)"));
        collection.update(update);
    }

    @Test
    public void testAndMixedQuery() throws ParseException {
        WKTReader reader = new WKTReader();
        Geometry search = reader.read("POLYGON ((490 490, 536 490, 536 515, 490 515, 490 490))");

        collection.createIndex(IndexOptions.indexOptions(UNIQUE), "key");
        collection.createIndex(IndexOptions.indexOptions(SPATIAL_INDEX), "location");
        DocumentCursor cursor = collection.find(
            and(
                where("location").intersects(search),
                FluentFilter.where("key").eq(2L)
            )
        );

        FindPlan findPlan = cursor.getFindPlan();
        assertEquals(1, findPlan.getIndexScanFilter().getFilters().size());
        assertTrue(findPlan.getIndexScanFilter().getFilters().get(0) instanceof IntersectsFilter);
        assertTrue(findPlan.getCollectionScanFilter() instanceof EqualsFilter);

        assertEquals(cursor.size(), 1);
        assertEquals(cursor.toList()
            .stream()
            .map(this::trimMeta)
            .collect(Collectors.toList()), Collections.singletonList(doc2));
    }
    @Test
    public void testAndSpatialQuery() throws ParseException {
        WKTReader reader = new WKTReader();
        Geometry search = reader.read("POLYGON ((490 490, 536 490, 536 515, 490 515, 490 490))");

        collection.createIndex(IndexOptions.indexOptions(SPATIAL_INDEX), "location");
        DocumentCursor cursor = collection.find(
            and(
                where("location").intersects(search),
                where("location").within(search)
            )
        );

        assertEquals(cursor.size(), 2);
        assertEquals(cursor.toList()
            .stream()
            .map(this::trimMeta)
            .collect(Collectors.toList()), Arrays.asList(doc1, doc2));
    }
}
