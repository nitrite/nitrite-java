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

import org.dizitart.no2.collection.Cursor;
import org.dizitart.no2.collection.IndexOptions;
import org.dizitart.no2.collection.IndexType;
import org.dizitart.no2.collection.NitriteCollection;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static org.dizitart.no2.DbTestOperations.getRandomTempDbFile;
import static org.junit.Assert.assertNotNull;

/**
 * @author Anindya Chatterjee
 */
@Ignore
public class StressTest {
    private String fileName = getRandomTempDbFile();
    private Nitrite db;
    private NitriteCollection collection;

    @Before
    public void before() {
        db = Nitrite
                .builder()
                .compressed()
                .filePath(fileName)
                .openOrCreate();
        collection = db.getCollection("test");
        System.out.println(fileName);
    }

    @Test
    public void testIssue41() {
        collection.createIndex("number", IndexOptions.indexOptions(IndexType.NonUnique));
        collection.createIndex("name", IndexOptions.indexOptions(IndexType.NonUnique));
        collection.createIndex("counter", IndexOptions.indexOptions(IndexType.Unique));

        Random random = new Random();
        AtomicLong counter = new AtomicLong(System.currentTimeMillis());
        PodamFactory factory = new PodamFactoryImpl();

        long start= System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            Document doc = new Document();
            doc.put("number", random.nextDouble());
            doc.put("name", factory.manufacturePojo(String.class));
            doc.put("counter", counter.getAndIncrement());
            collection.insert(doc);
            if (i % 10000 == 0) {
                System.out.println(i + " entries written");
            }
        }
        System.out.println("Records inserted in " + ((System.currentTimeMillis() - start) / (1000 * 60)) + " minutes");

        if (db.hasUnsavedChanges()) {
            db.commit();
        }

        start= System.currentTimeMillis();
        Cursor cursor = collection.find();
        System.out.println("Size ->" + cursor.size());
        System.out.println("Records size calculated in " + ((System.currentTimeMillis() - start) / (1000)) + " seconds");

        int i = 0;
        for (Document element : cursor) {
            assertNotNull(element);
            i++;
            if (i % 10000 == 0) {
                System.out.println(i + " entries processed");
            }
        }
    }

    @After
    public void clear() throws IOException {
        if (db != null && !db.isClosed()) {
            long start = System.currentTimeMillis();
            db.close();
            System.out.println("Time to compact and close - " + (System.currentTimeMillis() - start) / 1000 + " seconds");
        }
        Files.delete(Paths.get(fileName));
    }
}
