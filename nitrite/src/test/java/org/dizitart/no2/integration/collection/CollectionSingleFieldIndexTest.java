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

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.IndexType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.await;
import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.dizitart.no2.index.IndexOptions.indexOptions;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee.
 */
public class CollectionSingleFieldIndexTest extends BaseCollectionTest {

    @Test
    public void testCreateIndex() {
        collection.createIndex(indexOptions(IndexType.UNIQUE), "firstName");
        assertTrue(collection.hasIndex("firstName"));

        collection.createIndex(indexOptions(IndexType.NON_UNIQUE), "lastName");
        assertTrue(collection.hasIndex("lastName"));

        collection.createIndex(indexOptions(IndexType.FULL_TEXT), "body");
        assertTrue(collection.hasIndex("body"));

        collection.createIndex("birthDay");
        assertTrue(collection.hasIndex("birthDay"));

        insert();
    }

    @Test
    public void testListIndexes() {
        assertEquals(collection.listIndices().size(), 0);

        collection.createIndex("firstName");
        assertTrue(collection.hasIndex("firstName"));

        collection.createIndex(indexOptions(IndexType.NON_UNIQUE), "lastName");
        assertTrue(collection.hasIndex("lastName"));

        collection.createIndex(indexOptions(IndexType.FULL_TEXT), "body");
        assertTrue(collection.hasIndex("body"));

        assertEquals(collection.listIndices().size(), 3);
    }

    @Test
    public void testDropIndex() {
        collection.createIndex("firstName");
        assertTrue(collection.hasIndex("firstName"));

        collection.dropIndex("firstName");
        assertFalse(collection.hasIndex("firstName"));
    }

    @Test
    public void testDropAllIndexes() {
        collection.dropAllIndices();

        testCreateIndex();
        assertEquals(collection.listIndices().size(), 4);

        collection.dropAllIndices();
        assertEquals(collection.listIndices().size(), 0);
    }

    @Test
    public void testHasIndex() {
        assertFalse(collection.hasIndex("lastName"));
        collection.createIndex(indexOptions(IndexType.NON_UNIQUE), "lastName");
        assertTrue(collection.hasIndex("lastName"));

        assertFalse(collection.hasIndex("body"));
        collection.createIndex(indexOptions(IndexType.FULL_TEXT), "body");
        assertTrue(collection.hasIndex("body"));
    }

    @Test
    public void testDeleteWithIndex() {
        collection.createIndex(indexOptions(IndexType.UNIQUE), "firstName");
        collection.createIndex(indexOptions(IndexType.FULL_TEXT), "body");

        insert();

        WriteResult result = collection.remove(where("firstName").eq("fn1"));
        assertEquals(result.getAffectedCount(), 1);

        DocumentCursor cursor = collection.find();
        assertEquals(cursor.size(), 2);

        result = collection.remove(where("body").text("Lorem"));
        assertEquals(result.getAffectedCount(), 1);

        cursor = collection.find();
        assertEquals(cursor.size(), 1);
    }

    @Test
    public void testRebuildIndex() {
        collection.createIndex(indexOptions(IndexType.FULL_TEXT), "body");
        insert();
        Collection<IndexDescriptor> indices = collection.listIndices();
        for (IndexDescriptor idx : indices) {
            collection.rebuildIndex(idx.getFields().getFieldNames().toArray(new String[0]));
        }
    }

    @Test
    public void testRebuildIndexOnRunningIndex() {
        collection.createIndex(indexOptions(IndexType.FULL_TEXT), "body");
        insert();
        collection.rebuildIndex("body");

        boolean error = false;
        try {
            collection.rebuildIndex("body");
        } catch (IndexingException ie) {
            error = true;
        } finally {
            assertFalse(error);
            await().until(bodyIndexingCompleted());
        }
    }

    private Callable<Boolean> bodyIndexingCompleted() {
        return () -> !collection.isIndexing("body");
    }

    @Test
    public void testNullValueInIndexedField() {
        collection.createIndex(indexOptions(IndexType.UNIQUE), "firstName");
        collection.createIndex(indexOptions(IndexType.NON_UNIQUE), "birthDay");
        insert();

        Document document = createDocument("firstName", null)
            .put("lastName", "ln1")
            .put("birthDay", null)
            .put("data", new byte[]{1, 2, 3})
            .put("list", new ArrayList<String>() {{
                add("one");
                add("two");
                add("three");
            }})
            .put("body", "a quick brown fox jump over the lazy dog");

        collection.insert(document);
    }

    @Test
    public void testDropAllAndCreateIndex() {
        collection.createIndex("firstName");
        assertTrue(collection.hasIndex("firstName"));
        collection.dropAllIndices();
        assertFalse(collection.hasIndex("firstName"));

        collection.createIndex("firstName");
        assertTrue(collection.hasIndex("firstName"));

        collection = db.getCollection("test");
        assertTrue(collection.hasIndex("firstName"));
    }

    @Test
    public void testIssue178() {
        collection.dropAllIndices();
        collection.remove(Filter.ALL);

        Document doc1 = createDocument("field", 5);
        Document doc2 = createDocument("field", 4.3);
        Document doc3 = createDocument("field", 0.03);
        Document doc4 = createDocument("field", 4);
        Document doc5 = createDocument("field", 5.0);

        collection.insert(doc1, doc2, doc3, doc4, doc5);

        // With cross-type numeric equality, both 5 (Integer) and 5.0 (Double) match
        DocumentCursor cursor = collection.find(where("field").eq(5));
        assertEquals(cursor.size(), 2);

        collection.createIndex(indexOptions(IndexType.NON_UNIQUE), "field");

        // Same behavior with index
        cursor = collection.find(where("field").eq(5));
        assertEquals(cursor.size(), 2);
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

        collection.createIndex(indexOptions(IndexType.NON_UNIQUE), "first");
        assertEquals(collection.find().size(), 10000);

        collection.createIndex(indexOptions(IndexType.NON_UNIQUE), "second");
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

        collection.createIndex(indexOptions(IndexType.UNIQUE), "first");
        assertEquals(collection.find(where("first").eq(null)).size(), 1);

        collection.createIndex(indexOptions(IndexType.NON_UNIQUE), "third");
        assertEquals(collection.find(where("third").eq(null)).size(), 2);
    }

    @Test
    public void testCreateCompoundAndSingleFieldIndexOnSameField() {
        collection.createIndex(indexOptions(IndexType.NON_UNIQUE), "lastName");
        collection.createIndex(indexOptions(IndexType.UNIQUE), "firstName");
        collection.createIndex(indexOptions(IndexType.NON_UNIQUE), "lastName", "firstName");
    }
}
