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

package org.dizitart.no2.collection;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.common.SortOrder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.dizitart.no2.collection.FindOptions.orderBy;
import static org.dizitart.no2.collection.FindOptions.skipBy;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.dizitart.no2.index.IndexOptions.indexOptions;
import static org.dizitart.no2.index.IndexType.NON_UNIQUE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Equality and two-sided range queries on a non-unique index now stream their ids from the
 * index instead of materializing every match first. The results, their order, their count and
 * paging over them must be exactly what the materialized scan produced.
 */
public class LazyIndexScanTest {
    private Nitrite db;
    private NitriteCollection collection;

    @Before
    public void setUp() {
        db = Nitrite.builder().openOrCreate();
        collection = db.getCollection("lazy");
        collection.createIndex(indexOptions(NON_UNIQUE), "k");
        collection.createIndex(indexOptions(NON_UNIQUE), "tags");
        for (int i = 0; i < 200; i++) {
            collection.insert(Document.createDocument("n", i).put("k", i % 10).put("tags", new String[]{"t" + (i % 3), "x"}));
        }
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void testEqualityResultsCountAndPaging() {
        DocumentCursor cursor = collection.find(where("k").eq(3));
        assertEquals(20, cursor.size());
        assertEquals(20, cursor.toList().size());
        assertNotNull(collection.find(where("k").eq(3)).firstOrNull());
        assertEquals(3, collection.find(where("k").eq(3), skipBy(5).limit(3)).toList().size());
        assertEquals(0, collection.find(where("k").eq(42)).size());
    }

    @Test
    public void testRangeResultsInIndexOrderBothWays() {
        List<Document> ascending = collection.find(where("k").between(2, 4)).toList();
        assertEquals(60, ascending.size());
        assertEquals(2, ascending.get(0).get("k", Integer.class).intValue());
        assertEquals(4, ascending.get(ascending.size() - 1).get("k", Integer.class).intValue());

        List<Document> descending = collection.find(where("k").between(2, 4), orderBy("k", SortOrder.Descending)).toList();
        assertEquals(60, descending.size());
        assertEquals(4, descending.get(0).get("k", Integer.class).intValue());
        assertEquals(2, descending.get(descending.size() - 1).get("k", Integer.class).intValue());
    }

    @Test
    public void testMultiValuedFieldReturnsEachDocumentOnce() {
        assertEquals(200, collection.find(where("tags").eq("x")).size());
        assertEquals(200, collection.find(where("tags").eq("x")).toList().size());
        assertEquals(200, collection.find(where("tags").between("t0", "t9")).size());
    }

    @Test
    public void testCountFollowsRemovals() {
        collection.remove(where("n").eq(3));
        assertEquals(19, collection.find(where("k").eq(3)).size());
        assertEquals(19, collection.find(where("k").eq(3)).toList().size());
    }

    @Test
    public void testShapesOutsideTheLazyPathAreUnchanged() {
        assertEquals(40, collection.find(where("k").in(1, 2)).size());
        assertEquals(40, collection.find(where("k").gt(7)).size());
        assertEquals(20, collection.find(where("k").eq(3).and(where("n").lt(200))).size());
    }
}
