/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
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

import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.filters.FluentFilter;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.repository.Cursor;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.dizitart.no2.filters.Filter.and;
import static org.dizitart.no2.spatial.SpatialFluentFilter.where;
import static org.dizitart.no2.spatial.SpatialIndexer.SPATIAL_INDEX;
import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee
 */
public class SpatialIndexNegativeTest extends BaseSpatialTest {

    @Test(expected = FilterException.class)
    public void testNoIndex() throws ParseException {
        WKTReader reader = new WKTReader();
        Geometry search = reader.read("POLYGON ((490 490, 536 490, 536 515, 490 515, 490 490))");

        DocumentCursor cursor = collection.find(where("location").intersects(search));
        assertEquals(cursor.size(), 2);
        assertEquals(cursor.toList(), Arrays.asList(doc1, doc2));
    }

    @Test(expected = IndexingException.class)
    public void testIndexExists() {
        collection.createIndex(IndexOptions.indexOptions(SPATIAL_INDEX), "location");
        collection.createIndex(IndexOptions.indexOptions(SPATIAL_INDEX), "location");
    }

    @Test(expected = FilterException.class)
    public void testDropIndex() throws ParseException {
        repository.dropIndex("geometry");
        WKTReader reader = new WKTReader();
        Geometry search = reader.read("POLYGON ((490 490, 536 490, 536 515, 490 515, 490 490))");
        Cursor<SpatialData> cursor = repository.find(where("geometry").within(search));
        assertEquals(cursor.size(), 1);
    }

    @Test(expected = FilterException.class)
    public void testFindEqual() throws ParseException {
        WKTReader reader = new WKTReader();
        Geometry search = reader.read("POINT(500 505)");

        Cursor<SpatialData> cursor = repository.find(FluentFilter.where("geometry").eq(search));
        assertEquals(cursor.size(), 2);
        assertEquals(cursor.toList(), Collections.singletonList(object1));
    }

    @Test(expected = IndexingException.class)
    public void testCompoundIndex() throws ParseException {
        WKTReader reader = new WKTReader();
        Geometry search = reader.read("POLYGON ((490 490, 536 490, 536 515, 490 515, 490 490))");

        collection.createIndex(IndexOptions.indexOptions(SPATIAL_INDEX), "location", "key");
        DocumentCursor cursor1 = collection.find(where("location").intersects(search));
        assertEquals(cursor1.size(), 2);
        assertEquals(cursor1.toList()
            .stream()
            .map(this::trimMeta)
            .collect(Collectors.toList()), Arrays.asList(doc1, doc2));
    }

    @Test(expected = FilterException.class)
    public void testMultipleSpatialIndexOnMultipleFields() throws ParseException {
        WKTReader reader = new WKTReader();
        Geometry search = reader.read("POLYGON ((490 490, 536 490, 536 515, 490 515, 490 490))");

        collection.createIndex(IndexOptions.indexOptions(SPATIAL_INDEX), "location");
        collection.createIndex(IndexOptions.indexOptions(SPATIAL_INDEX), "area");

        DocumentCursor cursor = collection.find(
            and(
                where("location").intersects(search),
                where("area").within(search)
            )
        );
        assertEquals(0, cursor.size());
    }
}
