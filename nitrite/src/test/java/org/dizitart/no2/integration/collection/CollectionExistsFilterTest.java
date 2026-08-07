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
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee
 */
public class CollectionExistsFilterTest {
    private Nitrite db;
    private NitriteCollection collection;

    @Before
    public void setUp() {
        db = createDb();
        collection = db.getCollection("test-exists");

        // all four fields
        collection.insert(createDocument("name", "a")
            .put("nick", "aa")
            .put("age", 30)
            .put("address", createDocument("city", "kolkata")));

        // no nick
        collection.insert(createDocument("name", "b")
            .put("age", 40)
            .put("address", createDocument("city", "delhi")));

        // nick present but null
        collection.insert(createDocument("name", "c")
            .put("nick", null)
            .put("age", 50));

        // neither nick nor address
        collection.insert(createDocument("name", "d")
            .put("age", 60));
    }

    @After
    public void tearDown() {
        if (db != null && !db.isClosed()) {
            db.close();
        }
    }

    private List<String> names(org.dizitart.no2.common.RecordStream<Document> cursor) {
        List<String> names = new ArrayList<>();
        for (Document document : cursor) {
            names.add(document.get("name", String.class));
        }
        return names;
    }

    @Test
    public void testExists() {
        assertEquals(List.of("a", "c"), names(collection.find(where("nick").exists())));
    }

    @Test
    public void testExistsIncludesNullValue() {
        // document "c" has nick = null; `notEq(null)` cannot express this, which
        // is exactly why the filter exists
        assertTrue(names(collection.find(where("nick").exists())).contains("c"));
        assertFalse(names(collection.find(where("nick").notEq(null))).contains("c"));
    }

    @Test
    public void testNotExists() {
        assertEquals(List.of("b", "d"), names(collection.find(where("nick").exists().not())));
    }

    @Test
    public void testExistsOnEveryDocument() {
        assertEquals(List.of("a", "b", "c", "d"), names(collection.find(where("name").exists())));
        assertEquals(4, collection.find(where("age").exists()).size());
    }

    @Test
    public void testExistsOnUnknownField() {
        assertEquals(0, collection.find(where("unknown").exists()).size());
        assertEquals(4, collection.find(where("unknown").exists().not()).size());
    }

    @Test
    public void testExistsOnEmbeddedField() {
        assertEquals(List.of("a", "b"), names(collection.find(where("address").exists())));
        assertEquals(List.of("a", "b"), names(collection.find(where("address.city").exists())));
        assertEquals(0, collection.find(where("address.pin").exists()).size());
    }

    @Test
    public void testIndexedFieldGivesSameResult() {
        List<String> beforeIndex = names(collection.find(where("nick").exists()));
        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "nick");
        assertTrue(collection.hasIndex("nick"));

        // an index stores a missing field and an explicit null under the same
        // null key, so the filter must stay a collection scan and keep answering
        // the same way once the field is indexed
        assertEquals(beforeIndex, names(collection.find(where("nick").exists())));
        assertEquals(List.of("b", "d"), names(collection.find(where("nick").exists().not())));
    }

    @Test
    public void testExistsCombinedWithIndexedFilter() {
        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "age");

        assertEquals(List.of("a"),
            names(collection.find(where("age").lt(45).and(where("nick").exists()))));
        assertEquals(List.of("b"),
            names(collection.find(where("age").lt(45).and(where("nick").exists().not()))));
    }

    @Test
    public void testExistsWithOr() {
        assertEquals(List.of("a", "b", "c"),
            names(collection.find(where("nick").exists().or(where("address").exists()))));
    }

    @Test
    public void testFindPlanUsesCollectionScan() {
        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "nick");
        var findPlan = collection.find(where("nick").exists()).getFindPlan();

        assertNull(findPlan.getIndexDescriptor());
        assertNull(findPlan.getIndexScanFilter());
        assertEquals("(nick exists)", findPlan.getCollectionScanFilter().toString());
    }

    @Test
    public void testRemoveMakesFieldDisappear() {
        collection.remove(where("name").eq("a"));
        assertEquals(List.of("c"), names(collection.find(where("nick").exists())));
    }

    @Test
    public void testUpdateAddsField() {
        collection.update(where("name").eq("d"), createDocument("nick", "dd"));
        assertEquals(List.of("a", "c", "d"), names(collection.find(where("nick").exists())));
    }
}
