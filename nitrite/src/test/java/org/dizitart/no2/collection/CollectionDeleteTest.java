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
import org.junit.Test;

import static org.dizitart.no2.filters.Filters.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CollectionDeleteTest extends BaseCollectionTest {

    @Test
    public void testDelete() {
        insert();

        WriteResult writeResult = collection.remove(not(eq("lastName", null)));
        assertEquals(writeResult.getAffectedCount(), 3);

        Cursor cursor = collection.find();
        assertEquals(cursor.size(), 0);
    }

    @Test
    public void testDeleteWithOptions() {
        insert();

        RemoveOptions removeOptions = new RemoveOptions();
        removeOptions.setJustOne(true);

        WriteResult writeResult = collection.remove(not(eq("lastName", null)), removeOptions);
        assertEquals(writeResult.getAffectedCount(), 1);

        Cursor cursor = collection.find();
        assertEquals(cursor.size(), 2);
    }

    @Test
    public void testDeleteWithNonMatchingFilter() {
        insert();

        Cursor cursor = collection.find();
        assertEquals(cursor.size(), 3);

        WriteResult writeResult = collection.remove(eq("lastName", "a"));
        assertEquals(writeResult.getAffectedCount(), 0);
    }

    @Test
    public void testDeleteInEmptyCollection() {
        Cursor cursor = collection.find();
        assertEquals(cursor.size(), 0);

        WriteResult writeResult = collection.remove(not(eq("lastName", null)));
        assertEquals(writeResult.getAffectedCount(), 0);
    }

    @Test
    public void testClear() {
        collection.createIndex("firstName", IndexOptions.indexOptions(IndexType.Unique));
        insert();

        Cursor cursor = collection.find();
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

        collection.remove(ALL);

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

        WriteResult writeResult = collection.remove(doc1);
        assertEquals(writeResult.getAffectedCount(), 1);
        assertEquals(collection.size(), 2);

        writeResult = collection.remove(doc2);
        assertEquals(writeResult.getAffectedCount(), 1);
        assertEquals(collection.size(), 1);

        assertEquals(collection.find(eq("firstName", "fn1")).size(), 0);
        assertEquals(collection.find(eq("firstName", "fn2")).size(), 0);
        assertEquals(collection.find(eq("firstName", "fn3")).size(), 1);
    }
}
