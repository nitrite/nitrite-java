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

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.FindPlan;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.await;
import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.filters.Filter.and;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.dizitart.no2.index.IndexOptions.indexOptions;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class CollectionCompoundIndexTest extends BaseCollectionTest {
    @Test
    public void testCreateAndCheckIndex() {
        collection.createIndex(indexOptions(IndexType.UNIQUE), "firstName", "lastName");
        assertTrue(collection.hasIndex("firstName"));
        assertTrue(collection.hasIndex("firstName", "lastName"));
        assertFalse(collection.hasIndex("firstName", "lastName", "birthDay"));
        assertFalse(collection.hasIndex("lastName"));

        collection.createIndex(indexOptions(IndexType.NON_UNIQUE), "firstName");
        assertTrue(collection.hasIndex("firstName"));

        collection.createIndex(indexOptions(IndexType.NON_UNIQUE), "lastName");
        assertTrue(collection.hasIndex("lastName"));

        insert();
    }

    @Test
    public void testCreateMultiKeyIndexFirstField() {
        collection.createIndex(indexOptions(IndexType.NON_UNIQUE), "data", "lastName");
        assertTrue(collection.hasIndex("data"));
        assertTrue(collection.hasIndex("data", "lastName"));
        assertFalse(collection.hasIndex("lastName"));

        insert();
    }

    @Test
    public void testListIndexes() {
        assertEquals(collection.listIndices().size(), 0);
        collection.createIndex(indexOptions(IndexType.UNIQUE), "firstName", "lastName");
        assertEquals(collection.listIndices().size(), 1);

        collection.createIndex(indexOptions(IndexType.NON_UNIQUE), "firstName");
        assertEquals(collection.listIndices().size(), 2);
    }

    @Test
    public void testDropIndex() {
        collection.createIndex("firstName", "lastName");
        assertTrue(collection.hasIndex("firstName", "lastName"));
        assertTrue(collection.hasIndex("firstName"));

        collection.createIndex("firstName");
        assertTrue(collection.hasIndex("firstName"));

        collection.dropIndex("firstName");
        assertTrue(collection.hasIndex("firstName", "lastName"));
        assertTrue(collection.hasIndex("firstName"));

        collection.createIndex("firstName");
        collection.dropIndex("firstName", "lastName");
        assertFalse(collection.hasIndex("firstName", "lastName"));
        assertTrue(collection.hasIndex("firstName"));

        collection.dropIndex("firstName");
        assertFalse(collection.hasIndex("firstName"));
        assertEquals(collection.listIndices().size(), 0);
    }

    @Test
    public void testHasIndex() {
        assertFalse(collection.hasIndex("lastName"));
        collection.createIndex(indexOptions(IndexType.NON_UNIQUE), "lastName", "firstName");
        assertTrue(collection.hasIndex("lastName"));
    }

    @Test
    public void testDropAllIndexes() {
        collection.dropAllIndices();

        collection.createIndex("firstName", "lastName");
        collection.createIndex("firstName");
        assertEquals(collection.listIndices().size(), 2);

        collection.dropAllIndices();
        assertEquals(collection.listIndices().size(), 0);
    }

    @Test
    public void testRebuildIndex() {
        collection.createIndex("firstName", "lastName");
        assertTrue(collection.hasIndex("firstName", "lastName"));
        assertTrue(collection.hasIndex("firstName"));

        insert();
        collection.rebuildIndex("firstName", "lastName");
        assertTrue(collection.hasIndex("firstName", "lastName"));
        assertTrue(collection.hasIndex("firstName"));
    }

    @Test
    public void testDeleteWithIndex() {
        collection.createIndex("firstName", "lastName");

        insert();

        WriteResult result = collection.remove(and(where("firstName").eq("fn1"),
            where("lastName").eq("ln1")));
        assertEquals(result.getAffectedCount(), 1);

        result = collection.remove(and(where("firstName").eq("fn2"),
            where("birthDay").gte(new Date())));
        assertEquals(result.getAffectedCount(), 0);
    }

    @Test
    public void testRebuildIndexOnRunningIndex() {
        insert();
        db.getStore().subscribe((m) -> log.info(m.getEvent().name()));

        collection.createIndex("firstName", "lastName");
        collection.rebuildIndex("firstName", "lastName");

        assertTrue(collection.hasIndex("firstName", "lastName"));
    }

    @Test
    public void testNullValuesInIndexedFields() {
        collection.createIndex("firstName", "lastName");
        collection.createIndex("birthDay", "lastName");
        Document document = createDocument("firstName", null)
            .put("lastName", "ln1")
            .put("birthDay", new Date())
            .put("data", new byte[]{1, 2, 3})
            .put("list", new ArrayList<String>() {{
                add("one");
                add("two");
                add("three");
            }})
            .put("body", "a quick brown fox jump over the lazy dog");

        insert();
        collection.insert(document);

        DocumentCursor cursor = collection.find(where("firstName").eq(null));
        assertEquals("ln1", cursor.firstOrNull().get("lastName", String.class));
        assertNull(cursor.firstOrNull().get("firstName", String.class));

        document = createDocument("firstName", "fn4")
            .put("lastName", null)
            .put("birthDay", null)
            .put("data", new byte[]{1, 2, 3})
            .put("list", new ArrayList<String>() {{
                add("one");
                add("two");
                add("three");
            }})
            .put("body", "a quick brown fox jump over the lazy dog");
        collection.insert(document);

        cursor = collection.find(where("lastName").eq(null));
        assertEquals("fn4", cursor.firstOrNull().get("firstName", String.class));
        assertNull(cursor.firstOrNull().get("lastName", String.class));

        cursor = collection.find(and(where("lastName").eq(null), where("birthDay").eq(null)));
        assertNull(cursor.firstOrNull().get("lastName", String.class));
    }

    @Test
    public void testDropAllAndCreateIndex() {
        collection.createIndex("firstName", "lastName");
        assertTrue(collection.hasIndex("firstName"));

        DocumentCursor cursor = collection.find(and(where("firstName").eq("fn1"),
            where("lastName").eq("ln1")));
        FindPlan findPlan = cursor.getFindPlan();
        assertNotNull(findPlan.getIndexScanFilter());
        assertNull(findPlan.getCollectionScanFilter());

        collection.dropAllIndices();
        cursor = collection.find(and(where("firstName").eq("fn1"),
            where("lastName").eq("ln1")));
        findPlan = cursor.getFindPlan();
        assertNull(findPlan.getIndexScanFilter());
        assertNotNull(findPlan.getCollectionScanFilter());

        collection.createIndex("firstName", "lastName");
        cursor = collection.find(and(where("firstName").eq("fn1"),
            where("lastName").eq("ln1")));
        findPlan = cursor.getFindPlan();
        assertNotNull(findPlan.getIndexScanFilter());
        assertNull(findPlan.getCollectionScanFilter());
    }

    @Test
    public void testIssue178() {
        collection.dropAllIndices();
        collection.remove(Filter.ALL);

        Document doc1 = createDocument("field1", 5);
        Document doc2 = createDocument("field1", 4.3).put("field2", 3.5);
        Document doc3 = createDocument("field1", 0.03).put("field2", 5);
        Document doc4 = createDocument("field1", 4).put("field2", 4.5);
        Document doc5 = createDocument("field1", 5.0).put("field2", 5.0);

        collection.insert(doc1, doc2, doc3, doc4, doc5);

        DocumentCursor cursor = collection.find(and(where("field1").eq(0.03),
            where("field2").eq(5)));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(and(where("field1").eq(5),
            where("field2").eq(null)));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(where("field1").eq(5));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(where("field1").eq(5.0));
        assertEquals(cursor.size(), 1);

        collection.createIndex("field1", "field2");
        cursor = collection.find(and(where("field1").eq(0.03),
            where("field2").eq(5)));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(and(where("field1").eq(5),
            where("field2").eq(null)));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(where("field1").eq(5));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(where("field1").eq(5.0));
        assertEquals(cursor.size(), 1);
    }

    @Test
    public void testIndexEvent() {
        NitriteCollection collection = db.getCollection("index-test");
        Random random = new Random();
        for (int i = 0; i < 10000; i++) {
            Document document = createDocument("first", random.nextInt())
                .put("second", random.nextDouble());
            collection.insert(document);
        }

        AtomicBoolean failed = new AtomicBoolean(false);
        AtomicBoolean completed = new AtomicBoolean(false);
        collection.subscribe(eventInfo -> {
            switch (eventInfo.getEventType()) {
                case Insert:
                case Remove:
                case Update:
                    failed.set(true);
                    break;
                case IndexStart:
                case IndexEnd:
                    completed.set(true);
                    break;
            }
        });

        collection.createIndex(indexOptions(IndexType.NON_UNIQUE), "first", "second");
        assertEquals(collection.find().size(), 10000);

        await().until(completed::get);
        assertFalse(failed.get());
    }

    @Test
    public void testIndexAndSearchOnNullValues() {
        NitriteCollection collection = db.getCollection("index-on-null");
        collection.insert(createDocument("first", null).put("second", 123).put("third", new Integer[]{1, 2, null}));
        collection.insert(createDocument("first", "abcd").put("second", 456).put("third", new int[]{3, 1}));
        collection.insert(createDocument("first", "xyz").put("second", 789).put("third", null));

        collection.createIndex("third", "first");
        assertEquals(collection.find(where("first").eq(null)).size(), 1);

        assertEquals(collection.find(where("third").eq(null)).size(), 2);
    }
}
