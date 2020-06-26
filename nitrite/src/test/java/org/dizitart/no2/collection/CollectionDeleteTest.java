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

import org.dizitart.no2.BaseCollectionTest;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.junit.Test;

import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CollectionDeleteTest extends BaseCollectionTest {

    @Test
    public void testDelete() {
        insert();

        WriteResult writeResult = collection.remove(where("lastName").eq(null).not());
        assertEquals(writeResult.getAffectedCount(), 3);

        DocumentCursor cursor = collection.find();
        assertEquals(cursor.size(), 0);
    }

    @Test
    public void testDeleteWithOptions() {
        insert();

        WriteResult writeResult = collection.remove(where("lastName").eq(null).not(), true);
        assertEquals(writeResult.getAffectedCount(), 1);

        DocumentCursor cursor = collection.find();
        assertEquals(cursor.size(), 2);
    }

    @Test
    public void testDeleteWithNonMatchingFilter() {
        insert();

        DocumentCursor cursor = collection.find();
        assertEquals(cursor.size(), 3);

        WriteResult writeResult = collection.remove(where("lastName").eq("a"));
        assertEquals(writeResult.getAffectedCount(), 0);
    }

    @Test
    public void testDeleteInEmptyCollection() {
        DocumentCursor cursor = collection.find();
        assertEquals(cursor.size(), 0);

        WriteResult writeResult = collection.remove(where("lastName").eq(null).not());
        assertEquals(writeResult.getAffectedCount(), 0);
    }

    @Test
    public void testClear() {
        collection.createIndex("firstName", IndexOptions.indexOptions(IndexType.Unique));
        insert();

        DocumentCursor cursor = collection.find();
        assertEquals(cursor.size(), 3);
        assertTrue(collection.hasIndex("firstName"));

        boolean uniqueError = false;
        try {
            collection.insert(doc1);
        } catch (Exception e) {
            uniqueError = true;
        } finally {
            assertTrue(uniqueError);
        }

        collection.remove(Filter.ALL);

        cursor = collection.find();
        assertEquals(cursor.size(), 0);
        assertTrue(collection.hasIndex("firstName"));

        collection.insert(doc1);
        cursor = collection.find();
        assertEquals(cursor.size(), 1);
        assertTrue(collection.hasIndex("firstName"));
    }

    @Test
    public void testRemoveAll() {
        insert();
        WriteResult writeResult = collection.remove((Filter) null);
        assertEquals(writeResult.getAffectedCount(), 3);
    }

    @Test
    public void testRemoveDocument() {
        insert();

        WriteResult writeResult = collection.remove(where("firstName").eq("fn1"));
        assertEquals(writeResult.getAffectedCount(), 1);
        assertEquals(collection.size(), 2);

        writeResult = collection.remove(where("firstName").eq("fn2"));
        assertEquals(writeResult.getAffectedCount(), 1);
        assertEquals(collection.size(), 1);

        assertEquals(collection.find(where("firstName").eq("fn1")).size(), 0);
        assertEquals(collection.find(where("firstName").eq("fn2")).size(), 0);
        assertEquals(collection.find(where("firstName").eq("fn3")).size(), 1);
    }
}
