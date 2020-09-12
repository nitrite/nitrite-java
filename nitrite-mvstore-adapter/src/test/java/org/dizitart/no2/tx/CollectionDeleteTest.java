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

package org.dizitart.no2.tx;

import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.junit.Test;

import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.*;

public class CollectionDeleteTest extends BaseTransactionTest {

    @Test
    public void testDelete() {
        insert();

        WriteResult writeResult = txCollection.remove(where("lastName").notEq(null));
        assertEquals(writeResult.getAffectedCount(), 3);

        txCollection.commit();

        DocumentCursor cursor = collection.find();
        assertEquals(cursor.size(), 0);
    }

    @Test
    public void testDeleteWithOptions() {
        insert();

        WriteResult writeResult = txCollection.remove(where("lastName").notEq(null), true);
        assertEquals(writeResult.getAffectedCount(), 1);

        txCollection.commit();

        DocumentCursor cursor = collection.find();
        assertEquals(cursor.size(), 2);
    }

    @Test
    public void testDeleteWithNonMatchingFilter() {
        insert();

        DocumentCursor cursor = txCollection.find();
        assertEquals(cursor.size(), 3);

        txCollection.commit();
        WriteResult writeResult = collection.remove(where("lastName").eq("a"));
        assertEquals(writeResult.getAffectedCount(), 0);
    }

    @Test
    public void testDeleteInEmptyCollection() {
        DocumentCursor cursor = txCollection.find();
        assertEquals(cursor.size(), 0);

        txCollection.commit();
        WriteResult writeResult = collection.remove(where("lastName").notEq(null));
        assertEquals(writeResult.getAffectedCount(), 0);
    }

    @Test
    public void testClear() {
        txCollection.createIndex("firstName", IndexOptions.indexOptions(IndexType.Unique));
        insert();

        DocumentCursor cursor = txCollection.find();
        assertEquals(cursor.size(), 3);
        assertFalse(collection.hasIndex("firstName"));
        assertTrue(txCollection.hasIndex("firstName"));

        boolean uniqueError = false;
        try {
            txCollection.insert(doc1);
        } catch (Exception e) {
            uniqueError = true;
        } finally {
            assertTrue(uniqueError);
        }

        txCollection.remove(Filter.ALL);

        cursor = txCollection.find();
        assertEquals(cursor.size(), 0);
        assertTrue(txCollection.hasIndex("firstName"));

        txCollection.commit();

        collection.insert(doc1);
        cursor = collection.find();
        assertEquals(cursor.size(), 1);
        assertTrue(collection.hasIndex("firstName"));
    }

    @Test
    public void testRemoveAll() {
        insert();
        WriteResult writeResult = txCollection.remove((Filter) null);
        assertEquals(writeResult.getAffectedCount(), 3);
        txCollection.commit();

        assertEquals(collection.size(), 0);
    }

    @Test
    public void testRemoveDocument() {
        insert();

        WriteResult writeResult = txCollection.remove(where("firstName").eq("fn1"));
        assertEquals(writeResult.getAffectedCount(), 1);
        assertEquals(txCollection.size(), 2);
        assertEquals(collection.size(), 0);

        writeResult = txCollection.remove(where("firstName").eq("fn2"));
        assertEquals(writeResult.getAffectedCount(), 1);
        assertEquals(txCollection.size(), 1);
        assertEquals(collection.size(), 0);

        txCollection.commit();
        assertEquals(collection.find(where("firstName").eq("fn1")).size(), 0);
        assertEquals(collection.find(where("firstName").eq("fn2")).size(), 0);
        assertEquals(collection.find(where("firstName").eq("fn3")).size(), 1);
    }
}
