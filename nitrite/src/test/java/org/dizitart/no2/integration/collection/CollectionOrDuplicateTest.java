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

package org.dizitart.no2.integration.collection;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.dizitart.no2.integration.TestUtil.createDb;
import static org.junit.Assert.assertEquals;

/**
 * A document satisfying more than one branch of an `or` must still be reported once.
 *
 * @author Anindya Chatterjee
 */
public class CollectionOrDuplicateTest {
    private Nitrite db;
    private NitriteCollection collection;

    @Before
    public void setUp() {
        db = createDb();
        collection = db.getCollection("test-or-duplicate");

        // "a" satisfies both branches of the filter below, "b" only the second one
        collection.insert(createDocument("name", "a").put("x", 1).put("y", 2));
        collection.insert(createDocument("name", "b").put("x", 9).put("y", 2));
    }

    @After
    public void tearDown() {
        if (db != null && !db.isClosed()) {
            db.close();
        }
    }

    private static Filter orFilter() {
        return where("x").eq(1).or(where("y").eq(2));
    }

    private List<String> names(org.dizitart.no2.common.RecordStream<Document> cursor) {
        List<String> names = new ArrayList<>();
        for (Document document : cursor) {
            names.add(document.get("name", String.class));
        }
        return names;
    }

    @Test
    public void testOrWithoutIndex() {
        assertEquals(List.of("a", "b"), names(collection.find(orFilter())));
        assertEquals(2, collection.find(orFilter()).size());
    }

    @Test
    public void testOrWithOneIndexedBranch() {
        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "x");

        assertEquals(List.of("a", "b"), names(collection.find(orFilter())));
        assertEquals(2, collection.find(orFilter()).size());
    }

    @Test
    public void testOrWithAllBranchesIndexed() {
        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "x");
        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "y");

        assertEquals(List.of("a", "b"), names(collection.find(orFilter())));
        assertEquals(2, collection.find(orFilter()).size());
    }
}
