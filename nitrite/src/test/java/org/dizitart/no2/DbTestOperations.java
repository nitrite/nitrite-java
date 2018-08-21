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

package org.dizitart.no2;

import org.dizitart.no2.collection.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.dizitart.no2.Document.createDocument;
import static org.dizitart.no2.collection.FindOptions.sort;
import static org.dizitart.no2.filters.Filters.*;
import static org.dizitart.no2.util.Iterables.isSorted;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee.
 */
public class DbTestOperations {
    private static final String fileName = getRandomTempDbFile();

    void createDb() {
        Nitrite db = new NitriteBuilder()
                .filePath(fileName)
                .compressed()
                .openOrCreate();
        db.close();
    }

    void writeCollection() {
        Nitrite db;
        NitriteCollection collection;
        db = new NitriteBuilder()
                .filePath(fileName)
                .compressed()
                .openOrCreate();
        collection = db.getCollection("test");
        collection.remove(ALL);
        db.close();
    }

    void writeIndex() {
        Nitrite db;
        NitriteCollection collection;
        db = new NitriteBuilder()
                .filePath(fileName)
                .compressed()
                .openOrCreate();
        collection = db.getCollection("test");
        collection.remove(ALL);
        collection.createIndex("body", IndexOptions.indexOptions(IndexType.Fulltext));
        collection.createIndex("firstName", IndexOptions.indexOptions(IndexType.Unique));
        collection.createIndex("lastName", IndexOptions.indexOptions(IndexType.NonUnique));
        db.close();
    }

    void insertInCollection() throws ParseException {
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

        Nitrite db;
        NitriteCollection collection;
        db = new NitriteBuilder()
                .filePath(fileName)
                .compressed()
                .openOrCreate();

        collection = db.getCollection("test");

        WriteResult result = collection.insert(doc1, doc2, doc3);
        assertEquals(result.getAffectedCount(), 3);

        db.commit();
        db.close();
    }

    void readCollection() throws ParseException {
        Nitrite db;
        NitriteCollection collection;
        db = new NitriteBuilder()
                .filePath(fileName)
                .compressed()
                .openOrCreate();

        collection = db.getCollection("test");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);

        Cursor cursor = collection.find();
        assertEquals(cursor.size(), 3);

        cursor = collection.find(gt("birthDay",
                simpleDateFormat.parse("2012-07-01T16:02:48.440Z")));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(gte("birthDay",
                simpleDateFormat.parse("2012-07-01T16:02:48.440Z")));
        assertEquals(cursor.size(), 2);

        cursor = collection.find(lt("birthDay",
                simpleDateFormat.parse("2012-07-01T16:02:48.440Z")));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(lte("birthDay",
                simpleDateFormat.parse("2012-07-01T16:02:48.440Z")));
        assertEquals(cursor.size(), 2);

        cursor = collection.find(lte("birthDay",
                new Date()));
        assertEquals(cursor.size(), 3);

        cursor = collection.find(lt("birthDay",
                new Date()));
        assertEquals(cursor.size(), 3);

        cursor = collection.find(gt("birthDay",
                new Date()));
        assertEquals(cursor.size(), 0);

        cursor = collection.find(gte("birthDay",
                new Date()));
        assertEquals(cursor.size(), 0);

        cursor = collection.find(
                and(
                        lte("birthDay", new Date()),
                        eq("firstName", "fn1")
                ));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(
                or(
                        lte("birthDay", new Date()),
                        eq("firstName", "fn12")
                ));
        assertEquals(cursor.size(), 3);

        cursor = collection.find(
                and(
                        or(
                                lte("birthDay", new Date()),
                                eq("firstName", "fn12")
                        ),
                        eq("lastName", "ln1")
                ));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(
                not(
                        and(
                                or(
                                        lte("birthDay", new Date()),
                                        eq("firstName", "fn12")
                                ),
                                eq("lastName", "ln1")
                        )
                ));
        assertEquals(cursor.size(), 2);

        cursor = collection.find(eq("data.1", 4));
        assertEquals(cursor.size(), 2);

        cursor = collection.find(lt("data.1", 4));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(in("lastName", "ln1", "ln2", "ln10"));
        assertEquals(cursor.size(), 3);

        cursor = collection.find(not(in("firstName", "fn1", "fn2")));
        assertEquals(cursor.size(), 1);

        collection.createIndex("birthDay", IndexOptions.indexOptions(IndexType.Unique));
        cursor = collection.find(
                sort("birthDay", SortOrder.Descending).thenLimit(1, 2));
        assertEquals(cursor.size(), 2);
        List<Date> dateList = new ArrayList<>();
        for (Document document : cursor) {
            dateList.add(document.get("birthDay", Date.class));
        }
        assertTrue(isSorted(dateList, false));

        cursor = collection.find(text("body", "Lorem"));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(text("body", "quick"));
        assertEquals(cursor.size(), 2);

        cursor = collection.find(text("body", "nosql"));
        assertEquals(cursor.size(), 0);

        db.close();
    }

    void deleteDb() throws IOException {
        Files.delete(Paths.get(fileName));
    }

    public static String getRandomTempDbFile() {
        String dataDir = System.getProperty("java.io.tmpdir") + File.separator + "nitrite" + File.separator + "data";
        File file = new File(dataDir);
        if (!file.exists()) {
            assertTrue(file.mkdirs());
        }
        return file.getPath() + File.separator + UUID.randomUUID().toString() + ".db";
    }
}
