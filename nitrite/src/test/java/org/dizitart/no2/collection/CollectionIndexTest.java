/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.collection;

import org.dizitart.no2.BaseCollectionTest;
import org.dizitart.no2.Document;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.index.Index;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

import static org.awaitility.Awaitility.await;
import static org.dizitart.no2.Document.createDocument;
import static org.dizitart.no2.collection.IndexOptions.indexOptions;
import static org.dizitart.no2.filters.Filters.eq;
import static org.dizitart.no2.filters.Filters.text;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee.
 */
public class CollectionIndexTest extends BaseCollectionTest {

    @Test
    public void testCreateIndex() {
        collection.createIndex("firstName", indexOptions(IndexType.Unique));
        assertTrue(collection.hasIndex("firstName"));

        collection.createIndex("lastName", indexOptions(IndexType.NonUnique));
        assertTrue(collection.hasIndex("lastName"));

        collection.createIndex("body", indexOptions(IndexType.Fulltext));
        assertTrue(collection.hasIndex("body"));

        collection.createIndex("birthDay", null);
        assertTrue(collection.hasIndex("birthDay"));

        insert();
    }

    @Test
    public void testListIndexes() {
        assertEquals(collection.listIndices().size(), 0);

        collection.createIndex("firstName", indexOptions(IndexType.Unique));
        assertTrue(collection.hasIndex("firstName"));

        collection.createIndex("lastName", indexOptions(IndexType.NonUnique));
        assertTrue(collection.hasIndex("lastName"));

        collection.createIndex("body", indexOptions(IndexType.Fulltext));
        assertTrue(collection.hasIndex("body"));

        assertEquals(collection.listIndices().size(), 3);
    }

    @Test
    public void testDropIndex() {
        collection.createIndex("firstName", indexOptions(IndexType.Unique));
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
        collection.createIndex("lastName", indexOptions(IndexType.NonUnique));
        assertTrue(collection.hasIndex("lastName"));

        assertFalse(collection.hasIndex("body"));
        collection.createIndex("body", indexOptions(IndexType.Fulltext));
        assertTrue(collection.hasIndex("body"));
    }

    @Test
    public void testDeleteWithIndex() {
        collection.createIndex("firstName", indexOptions(IndexType.Unique));
        collection.createIndex("body", indexOptions(IndexType.Fulltext));

        insert();

        WriteResult result = collection.remove(eq("firstName", "fn1"));
        assertEquals(result.getAffectedCount(), 1);

        Cursor cursor = collection.find();
        assertEquals(cursor.size(), 2);

        result = collection.remove(text("body", "Lorem"));
        assertEquals(result.getAffectedCount(), 1);

        cursor = collection.find();
        assertEquals(cursor.size(), 1);
    }

    @Test
    public void testCreateIndexAsync() throws InterruptedException {
        insert();
        collection.createIndex("body", indexOptions(IndexType.Fulltext, true));
        assertTrue(collection.isIndexing("body"));

        await().until(bodyIndexingCompleted());
    }

    @Test
    public void testRebuildIndex() {
        collection.createIndex("body", indexOptions(IndexType.Fulltext, false));
        insert();
        Collection<Index> indices = collection.listIndices();
        for (Index idx : indices) {
            collection.rebuildIndex(idx.getField(), false);
        }
    }

    @Test
    public void testRebuildIndexAsync() {
        collection.createIndex("body", indexOptions(IndexType.Fulltext, true));
        insert();
        await().until(bodyIndexingCompleted());

        Collection<Index> indices = collection.listIndices();
        for (Index idx : indices) {
            collection.rebuildIndex(idx.getField(), true);
            await().until(bodyIndexingCompleted());
        }
    }

    @Test
    public void testRebuildIndexOnRunningIndex() {
        collection.createIndex("body", indexOptions(IndexType.Fulltext, false));
        Collection<Index> indices = collection.listIndices();
        Index idx = indices.iterator().next();
        insert();
        collection.rebuildIndex(idx.getField(), true);

        boolean error = false;
        try {
            collection.rebuildIndex(idx.getField(), true);
        } catch (IndexingException ie) {
            error = true;
        } finally {
            assertTrue(error);
            await().until(bodyIndexingCompleted());
        }
    }

    private Callable<Boolean> bodyIndexingCompleted() {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return !collection.isIndexing("body");
            }
        };
    }

    @Test
    public void testNullValueInIndexedField() {
        collection.createIndex("firstName", indexOptions(IndexType.Unique));
        collection.createIndex("birthDay", indexOptions(IndexType.NonUnique));
        insert();

        Document document = createDocument("firstName", null)
                .put("lastName", "ln1")
                .put("birthDay", null)
                .put("data", new byte[] {1, 2, 3})
                .put("list", new ArrayList<String>() {{ add("one"); add("two"); add("three"); }})
                .put("body", "a quick brown fox jump over the lazy dog");

        collection.insert(document);
    }

    @Test
    public void testDropAllAndCreateIndex() {
        collection.createIndex("firstName", indexOptions(IndexType.Unique));
        assertTrue(collection.hasIndex("firstName"));
        collection.dropAllIndices();
        assertFalse(collection.hasIndex("firstName"));

        collection.createIndex("firstName", indexOptions(IndexType.Unique));
        assertTrue(collection.hasIndex("firstName"));

        collection = db.getCollection("test");
        assertTrue(collection.hasIndex("firstName"));
    }
}
