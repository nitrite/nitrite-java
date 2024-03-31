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

package org.dizitart.no2.integration;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.index.IndexType;
import org.junit.Rule;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.collection.FindOptions.orderBy;
import static org.dizitart.no2.filters.NitriteCommonFilters.*;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.dizitart.no2.filters.NitriteCommonFilters.and;
import static org.dizitart.no2.filters.NitriteCommonFilters.or;
import static org.dizitart.no2.index.IndexOptions.indexOptions;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee.
 */
public class DbWriteCloseReadTest {
    private final String fileName = TestUtil.getRandomTempDbFile();
    private Nitrite db;
    private volatile boolean writeCompleted = false;

    @Rule
    public Retry retry = new Retry(3);

    @Test
    public void testWriteCloseRead() throws Exception {
        try {
            createDb();
            writeCollection();
            writeIndex();
            insertInCollection();
        } catch (ParseException pe) {
            // ignore
        } finally {
            writeCompleted = true;
        }

        try {
            assertTrue(writeCompleted);
            readCollection();
        } catch (Exception e) {
            fail("collection read failed - " + e.getMessage());
        } finally {
            deleteDb();
        }
    }

    void createDb() {
        db = TestUtil.createDb(fileName);
        db.close();
    }

    void writeCollection() {
        NitriteCollection collection;

        db = TestUtil.createDb(fileName);

        collection = db.getCollection("test");
        collection.remove(ALL);
        db.close();
    }

    void writeIndex() {
        NitriteCollection collection;

        db = TestUtil.createDb(fileName);

        collection = db.getCollection("test");
        collection.remove(ALL);
        collection.createIndex(indexOptions(IndexType.FULL_TEXT), "body");
        collection.createIndex("firstName");
        collection.createIndex(indexOptions(IndexType.NON_UNIQUE), "lastName");
        db.close();
    }

    void insertInCollection() throws Exception {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);

        Document doc1 = createDocument("firstName", "fn1")
            .put("lastName", "ln1")
            .put("birthDay", simpleDateFormat.parse("2012-07-01T16:02:48.440Z"))
            .put("data", new byte[]{1, 2, 3})
            .put("body", "a quick brown fox jump over the lazy dog");
        Document doc2 = createDocument("firstName", "fn2")
            .put("lastName", "ln2")
            .put("birthDay", simpleDateFormat.parse("2010-06-12T16:02:48.440Z"))
            .put("data", new byte[]{3, 4, 3})
            .put("body", "quick hello world from nitrite");
        Document doc3 = createDocument("firstName", "fn3")
            .put("lastName", "ln2")
            .put("birthDay", simpleDateFormat.parse("2014-04-17T16:02:48.440Z"))
            .put("data", new byte[]{9, 4, 8})
            .put("body", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                "Sed nunc mi, mattis ullamcorper dignissim vitae, condimentum non lorem.");

        NitriteCollection collection;

        db = TestUtil.createDb(fileName);

        collection = db.getCollection("test");

        WriteResult result = collection.insert(doc1, doc2, doc3);
        assertEquals(result.getAffectedCount(), 3);

        db.commit();
        db.close();
    }

    void readCollection() throws Exception {
        NitriteCollection collection;

        db = TestUtil.createDb(fileName);

        collection = db.getCollection("test");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);

        DocumentCursor cursor = collection.find();
        assertEquals(cursor.size(), 3);

        cursor = collection.find(where("birthDay").gt(simpleDateFormat.parse("2012-07-01T16:02:48.440Z")));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(where("birthDay").gte(simpleDateFormat.parse("2012-07-01T16:02:48.440Z")));
        assertEquals(cursor.size(), 2);

        cursor = collection.find(where("birthDay").lt(simpleDateFormat.parse("2012-07-01T16:02:48.440Z")));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(where("birthDay").lte(simpleDateFormat.parse("2012-07-01T16:02:48.440Z")));
        assertEquals(cursor.size(), 2);

        cursor = collection.find(where("birthDay").lte(new Date()));
        assertEquals(cursor.size(), 3);

        cursor = collection.find(where("birthDay").lt(new Date()));
        assertEquals(cursor.size(), 3);

        cursor = collection.find(where("birthDay").gt(new Date()));
        assertEquals(cursor.size(), 0);

        cursor = collection.find(where("birthDay").gte(new Date()));
        assertEquals(cursor.size(), 0);

        cursor = collection.find(where("birthDay").lte(new Date()).and(where("firstName").eq("fn1")));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(where("birthDay").lte(new Date()).or(where("firstName").eq("fn12")));
        assertEquals(cursor.size(), 3);

        cursor = collection.find(
            and(
                or(
                    where("birthDay").lte(new Date()),
                    where("firstName").eq("fn12")
                ),
                where("lastName").eq("ln1")
            ));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(
            and(
                or(
                    where("birthDay").lte(new Date()),
                    where("firstName").eq("fn12")
                ),
                where("lastName").eq("ln1")
            ).not());
        assertEquals(cursor.size(), 2);

        cursor = collection.find(where("data.1").eq((byte) 4));
        assertEquals(cursor.size(), 2);

        cursor = collection.find(where("data.1").lt(4));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(where("lastName").in("ln1", "ln2", "ln10"));
        assertEquals(cursor.size(), 3);

        cursor = collection.find(where("firstName").notIn("fn1", "fn2"));
        assertEquals(cursor.size(), 1);

        collection.createIndex("birthDay");
        cursor = collection.find(orderBy("birthDay", SortOrder.Descending).skip(1).limit(2));
        assertEquals(cursor.size(), 2);
        List<Date> dateList = new ArrayList<>();
        for (Document document : cursor) {
            dateList.add(document.get("birthDay", Date.class));
        }
        assertTrue(TestUtil.isSorted(dateList, false));

        cursor = collection.find(where("body").text("Lorem"));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(where("body").text("quick"));
        assertEquals(cursor.size(), 2);

        cursor = collection.find(where("body").text("nosql"));
        assertEquals(cursor.size(), 0);

        db.close();
    }

    void deleteDb() {
        if (db != null && !db.isClosed()) {
            db.close();
        }
        TestUtil.deleteDb(fileName);
    }
}
