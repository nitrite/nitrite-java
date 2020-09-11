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

package org.dizitart.no2.rocksdb.tx;

import org.dizitart.no2.collection.*;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.NotIdentifiableException;
import org.dizitart.no2.exceptions.TransactionException;
import org.dizitart.no2.exceptions.UniqueConstraintException;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexType;
import org.junit.Test;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.common.util.DocumentUtils.isSimilar;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.dizitart.no2.index.IndexOptions.indexOptions;
import static org.junit.Assert.*;

public class CollectionUpdateTest extends BaseTransactionTest {

    @Test
    public void testUpdate() {
        insert();

        DocumentCursor cursor = collection.find(where("firstName").eq("fn1"));
        assertEquals(cursor.size(), 0);

        txCollection.commit();

        cursor = collection.find(where("firstName").eq("fn1"));
        assertEquals(cursor.size(), 1);

        for (Document document : cursor) {
            assertEquals(document.get("lastName"), "ln1");
        }

        txCollection = collection.beginTransaction();

        WriteResult updateResult = txCollection.update(where("firstName").eq("fn1"),
            createDocument("lastName", "new-last-name"));
        assertEquals(updateResult.getAffectedCount(), 1);

        txCollection.commit();

        cursor = collection.find(where("firstName").eq("fn1"));
        assertEquals(cursor.size(), 1);
        for (Document document : cursor) {
            assertEquals(document.get("lastName"), "new-last-name");
        }
    }

    @Test(expected = NotIdentifiableException.class)
    public void testUpsertWithoutId() {
        insert();
        Document update = createDocument("lastName", "ln4");
        WriteResult writeResult = txCollection.update(update, false);
        assertEquals(writeResult.getAffectedCount(), 0);
        assertEquals(collection.size(), 0);
    }

    @Test
    public void testUpsert() {
        insert();
        assertEquals(collection.size(), 0);
        assertEquals(txCollection.size(), 3);

        Document update = createDocument("lastName", "ln4");
        WriteResult writeResult = txCollection.update(update, true);
        assertEquals(writeResult.getAffectedCount(), 1);

        txCollection.commit();

        assertEquals(collection.size(), 4);

        Document document = collection.find(where("lastName").eq("ln4"))
            .firstOrNull();
        assertTrue(isSimilar(document, update, "lastName"));
    }

    @Test
    public void testOptionUpsert() {
        DocumentCursor cursor = collection.find(where("firstName").eq("fn1"));
        assertEquals(cursor.size(), 0);

        WriteResult updateResult = txCollection.update(where("firstName").eq("fn1"),
            doc1, UpdateOptions.updateOptions(true));
        assertEquals(updateResult.getAffectedCount(), 1);

        cursor = collection.find(where("firstName").eq("fn1"));
        assertEquals(cursor.size(), 0);

        txCollection.commit();

        cursor = collection.find(where("firstName").eq("fn1"));
        assertEquals(cursor.size(), 1);

        for (Document document : cursor) {
            assertTrue(isSimilar(document, doc1, "firstName", "lastName", "birthDay", "data", "list", "body"));
        }
    }

    @Test
    public void testUpdateMultiple() {
        DocumentCursor cursor = collection.find(where("firstName").eq("fn1"));
        assertEquals(cursor.size(), 0);

        insert();

        Document document = createDocument("lastName", "newLastName1");
        WriteResult updateResult = txCollection.update(where("firstName").eq("fn1").not(),
            document);
        assertEquals(updateResult.getAffectedCount(), 2);

        txCollection.commit();

        cursor = collection.find(where("lastName").eq("newLastName1"));
        assertEquals(cursor.size(), 2);
    }

    @Test
    public void testUpdateWithOptionsUpsertFalse() {
        DocumentCursor cursor = collection.find(where("firstName").eq("fn1"));
        assertEquals(cursor.size(), 0);

        UpdateOptions updateOptions = new UpdateOptions();
        updateOptions.setInsertIfAbsent(false);

        WriteResult updateResult = txCollection.update(where("firstName").eq("fn1"),
            doc1, updateOptions);
        assertEquals(updateResult.getAffectedCount(), 0);

        txCollection.commit();
        cursor = collection.find(where("firstName").eq("fn1"));
        assertEquals(cursor.size(), 0);
    }

    @Test
    public void testUpdateMultipleWithJustOnceFalse() {
        DocumentCursor cursor = collection.find(where("firstName").eq("fn1"));
        assertEquals(cursor.size(), 0);

        insert();

        UpdateOptions updateOptions = new UpdateOptions();
        updateOptions.setJustOnce(false);

        Document document = createDocument("lastName", "newLastName1");
        WriteResult updateResult = txCollection.update(where("firstName").eq("fn1").not(),
            document, updateOptions);
        assertEquals(updateResult.getAffectedCount(), 2);

        cursor = collection.find(where("lastName").eq("newLastName1"));
        assertEquals(cursor.size(), 0);

        txCollection.commit();
        cursor = collection.find(where("lastName").eq("newLastName1"));
        assertEquals(cursor.size(), 2);
    }

    @Test
    public void testUpdateMultipleWithJustOnceTrue() {
        DocumentCursor cursor = collection.find(where("firstName").eq("fn1"));
        assertEquals(cursor.size(), 0);

        insert();

        UpdateOptions updateOptions = new UpdateOptions();
        updateOptions.setJustOnce(true);

        Document document = createDocument("lastName", "newLastName1");
        txCollection.update(where("firstName").eq("fn1").not(),
            document, updateOptions);

        assertEquals(collection.size(), 0);
        txCollection.commit();
        assertEquals(collection.size(), 3);
    }

    @Test
    public void testUpdateWithNewField() {
        insert();

        DocumentCursor cursor = txCollection.find(where("firstName").eq("fn1"));
        assertEquals(cursor.size(), 1);
        for (Document document : cursor) {
            assertEquals(document.get("lastName"), "ln1");
        }

        WriteResult updateResult = txCollection.update(where("firstName").eq("fn1"),
            createDocument("new-value", "new-value-value"));
        assertEquals(updateResult.getAffectedCount(), 1);

        cursor = collection.find(where("firstName").eq("fn1"));
        assertEquals(cursor.size(), 0);

        txCollection.commit();

        cursor = collection.find(where("firstName").eq("fn1"));
        assertEquals(cursor.size(), 1);

        for (Document document : cursor) {
            assertEquals(document.get("new-value"), "new-value-value");
        }
    }

    @Test
    public void testUpdateInvalidFilter() {
        insert();

        DocumentCursor cursor = txCollection.find(where("lastName").eq("ln1"));
        assertEquals(cursor.size(), 1);
        for (Document document : cursor) {
            assertEquals(document.get("firstName"), "fn1");
        }

        // to check if NitriteId is valid.
        WriteResult updateResult = txCollection.update(where("some-value").eq("some-value"),
            createDocument("lastName", "new-last-name"));
        assertEquals(updateResult.getAffectedCount(), 0);

        txCollection.commit();
        assertEquals(collection.find(where("some-value").eq("some-value")).size(), 0);
    }

    @Test
    public void updateAfterAttributeRemoval() {
        NitriteCollection collection = db.getCollection("test_updateAfterAttributeRemoval");
        collection.remove(Filter.ALL);

        TransactionalCollection coll = collection.beginTransaction();

        Document doc = createDocument().put("id", "test-1").put("group", "groupA");
        assertEquals(1, coll.insert(doc).getAffectedCount());

        Document savedDoc1 = coll.find().firstOrNull();
        assertNotNull(savedDoc1);

        Document clonedDoc1 = savedDoc1.clone();
        assertEquals(savedDoc1, clonedDoc1);

        clonedDoc1.put("group", null);
        assertEquals(1, coll.update(clonedDoc1).getAffectedCount());

        Document savedDoc2 = coll.find().firstOrNull();
        assertNotNull(savedDoc2);
        assertNull(savedDoc2.get("group"));

        coll.commit();

        savedDoc2 = collection.find().firstOrNull();
        assertNotNull(savedDoc2);
        assertNull(savedDoc2.get("group"));
    }

    @Test(expected = NotIdentifiableException.class)
    public void testUpdateWithoutId() {
        TransactionalCollection collection = db.getCollection("test").beginTransaction();
        Document document = createDocument("test", "test123");
        collection.update(document);
    }

    @Test(expected = NotIdentifiableException.class)
    public void testRemoveWithoutId() {
        TransactionalCollection collection = db.getCollection("test").beginTransaction();
        Document document = createDocument("test", "test123");
        collection.remove(document);
    }

    @Test(expected = NitriteIOException.class)
    public void testRegisterListenerAfterDrop() {
        TransactionalCollection collection = db.getCollection("test").beginTransaction();
        collection.drop();
        collection.subscribe(changeInfo -> fail("should not happen"));
    }

    @Test(expected = TransactionException.class)
    public void testRegisterListenerAfterClose() {
        TransactionalCollection collection = db.getCollection("test").beginTransaction();
        collection.close();
        collection.subscribe(changeInfo -> fail("should not happen"));
    }

    @Test(expected = UniqueConstraintException.class)
    public void testIssue151() {
        Document doc1 = createDocument().put("id", "test-1").put("fruit", "Apple");
        Document doc2 = createDocument().put("id", "test-2").put("fruit", "Ôrange");
        TransactionalCollection coll = db.getCollection("test").beginTransaction();
        coll.insert(doc1, doc2);

        coll.createIndex("fruit", indexOptions(IndexType.Unique));

        assertEquals(coll.find(where("fruit").eq("Apple")).size(), 1);

        Document doc3 = coll.find(where("id").eq("test-2")).firstOrNull();

        doc3.put("fruit", "Apple");
        coll.update(doc3);

        assertEquals(coll.find(where("fruit").eq("Apple")).size(), 1);
    }
}
