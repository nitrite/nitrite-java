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
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.filters.Filters;
import org.junit.Assert;
import org.junit.Test;

import static org.dizitart.no2.Document.createDocument;
import static org.dizitart.no2.filters.Filters.eq;
import static org.dizitart.no2.filters.Filters.not;
import static org.junit.Assert.*;

public class CollectionUpdateTest extends BaseCollectionTest {

    @Test
    public void testUpdate() {
        insert();

        Cursor cursor = collection.find(eq("firstName", "fn1"));
        assertEquals(cursor.size(), 1);
        for (Document document : cursor) {
            assertEquals(document.get("lastName"), "ln1");
        }

        WriteResult updateResult = collection.update(eq("firstName", "fn1"),
                createDocument("lastName", "new-last-name"));
        assertEquals(updateResult.getAffectedCount(), 1);

        cursor = collection.find(eq("firstName", "fn1"));
        assertEquals(cursor.size(), 1);
        for (Document document : cursor) {
            assertEquals(document.get("lastName"), "new-last-name");
        }
    }

    @Test
    public void testUpsert() {
        insert();
        Document update = createDocument("lastName", "ln4");
        WriteResult writeResult = collection.update(update, false);
        assertEquals(writeResult.getAffectedCount(), 0);
        assertEquals(collection.size(), 3);

        writeResult = collection.update(update, true);
        assertEquals(writeResult.getAffectedCount(), 1);
        assertEquals(collection.size(), 4);

        Document document = collection.find(eq("lastName", "ln4"))
                .firstOrDefault();
        assertEquals(document, update);
    }

    @Test
    public void testOptionUpsert() {
        Cursor cursor = collection.find(eq("firstName", "fn1"));
        assertEquals(cursor.size(), 0);

        WriteResult updateResult = collection.update(eq("firstName", "fn1"),
                doc1, UpdateOptions.updateOptions(true));
        assertEquals(updateResult.getAffectedCount(), 1);

        cursor = collection.find(eq("firstName", "fn1"));
        assertEquals(cursor.size(), 1);
        for (Document document : cursor) {
            Assert.assertEquals(document, doc1);
        }
    }

    @Test
    public void testUpdateMultiple() {
        Cursor cursor = collection.find(eq("firstName", "fn1"));
        assertEquals(cursor.size(), 0);

        insert();

        Document document = createDocument("lastName", "newLastName1");
        WriteResult updateResult = collection.update(not(eq("firstName", "fn1")),
                document);
        assertEquals(updateResult.getAffectedCount(), 2);

        cursor = collection.find(eq("lastName", "newLastName1"));
        assertEquals(cursor.size(), 2);
    }

    @Test
    public void testUpdateWithOptionsUpsertFalse() {
        Cursor cursor = collection.find(eq("firstName", "fn1"));
        assertEquals(cursor.size(), 0);

        UpdateOptions updateOptions = new UpdateOptions();
        updateOptions.setUpsert(false);

        WriteResult updateResult = collection.update(eq("firstName", "fn1"),
                doc1, updateOptions);
        assertEquals(updateResult.getAffectedCount(), 0);

        cursor = collection.find(eq("firstName", "fn1"));
        assertEquals(cursor.size(), 0);
    }

    @Test
    public void testUpdateMultipleWithJustOnceFalse() {
        Cursor cursor = collection.find(eq("firstName", "fn1"));
        assertEquals(cursor.size(), 0);

        insert();

        UpdateOptions updateOptions = new UpdateOptions();
        updateOptions.setJustOnce(false);

        Document document = createDocument("lastName", "newLastName1");
        WriteResult updateResult = collection.update(not(eq("firstName", "fn1")),
                document, updateOptions);
        assertEquals(updateResult.getAffectedCount(), 2);

        cursor = collection.find(eq("lastName", "newLastName1"));
        assertEquals(cursor.size(), 2);
    }

    @Test(expected = InvalidOperationException.class)
    public void testUpdateMultipleWithJustOnceTrue() {
        Cursor cursor = collection.find(eq("firstName", "fn1"));
        assertEquals(cursor.size(), 0);

        insert();

        UpdateOptions updateOptions = new UpdateOptions();
        updateOptions.setJustOnce(true);

        Document document = createDocument("lastName", "newLastName1");
        collection.update(not(eq("firstName", "fn1")),
            document, updateOptions);
    }

    @Test
    public void testUpdateWithNewField() {
        insert();

        Cursor cursor = collection.find(eq("firstName", "fn1"));
        assertEquals(cursor.size(), 1);
        for (Document document : cursor) {
            assertEquals(document.get("lastName"), "ln1");
        }

        WriteResult updateResult = collection.update(eq("firstName", "fn1"),
                createDocument("new-value", "new-value-value"));
        assertEquals(updateResult.getAffectedCount(), 1);

        cursor = collection.find(eq("firstName", "fn1"));
        assertEquals(cursor.size(), 1);
        for (Document document : cursor) {
            assertEquals(document.get("new-value"), "new-value-value");
        }
    }

    @Test
    public void testUpdateInvalidFilter() {
        insert();

        Cursor cursor = collection.find(eq("lastName", "ln1"));
        assertEquals(cursor.size(), 1);
        for (Document document : cursor) {
            assertEquals(document.get("firstName"), "fn1");
        }

        // to check if NitriteId is valid.
        WriteResult updateResult = collection.update(eq("some-value", "some-value"),
                createDocument("lastName", "new-last-name"));
        assertEquals(updateResult.getAffectedCount(), 0);
    }

    @Test
    public void updateAfterAttributeRemoval() {
        NitriteCollection coll = db.getCollection("test_updateAfterAttributeRemoval");
        coll.remove(Filters.ALL);

        Document doc = new Document().put("id", "test-1").put("group", "groupA");
        assertEquals(1, coll.insert(doc).getAffectedCount());

        Document savedDoc1 = coll.find().firstOrDefault();
        assertNotNull(savedDoc1);

        Document clonedDoc1 = new Document(savedDoc1);
        assertEquals(savedDoc1, clonedDoc1);

        clonedDoc1.put("group", null);
//        clonedDoc1.remove("group");
        assertEquals(1, coll.update(clonedDoc1).getAffectedCount());

        Document savedDoc2 = coll.find(Filters.ALL).firstOrDefault();
        assertNotNull(savedDoc2);
        assertNull(savedDoc2.get("group"));
    }
}
