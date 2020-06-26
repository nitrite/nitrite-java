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

package org.dizitart.no2;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.concurrent.ThreadPoolManager;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static org.dizitart.no2.DbTestOperations.getRandomTempDbFile;
import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Anindya Chatterjee.
 */
@RunWith(Parameterized.class)
public class MultiThreadedTest {
    private static final String fileName = getRandomTempDbFile();
    @Parameterized.Parameter
    public boolean inMemory = false;
    private NitriteCollection collection;
    private int threadCount = 20;
    private final CountDownLatch latch = new CountDownLatch(threadCount);
    private int iterationCount = 100;
    private Random generator = new Random();
    private AtomicInteger docCounter = new AtomicInteger(0);
    private ExecutorService executor = ThreadPoolManager.getThreadPool(threadCount, "MultiThreadedTest");

    @Parameterized.Parameters(name = "InMemory = {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {false},
            {true}
        });
    }

    @Test
    public void testOperations() throws InterruptedException {
        NitriteBuilder builder = NitriteBuilder.get()
            .compressed();

        if (!inMemory) {
            builder.filePath(fileName);
        }

        Nitrite db = builder.openOrCreate();

        collection = db.getCollection("test");
        collection.remove(Filter.ALL);
        collection.createIndex("unixTime", IndexOptions.indexOptions(IndexType.Unique));
        db.commit();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < iterationCount; j++) {
                    try {
                        Document document = generate();
                        collection.insert(document);

                        if (j == iterationCount / 2
                            && !collection.hasIndex("text")
                            && !collection.hasIndex("date")) {
                            collection.createIndex("text", IndexOptions.indexOptions(IndexType.Fulltext));
                            collection.createIndex("date", IndexOptions.indexOptions(IndexType.NonUnique));
                        }

                        long unixTime = (long) document.get("unixTime");
                        DocumentCursor cursor = collection.find(where("unixTime").eq(unixTime));
                        assertTrue(cursor.size() >= 0);

                        if (collection.hasIndex("text") && !collection.isIndexing("text")) {
                            String textData = (String) document.get("text");
                            cursor = collection.find(where("text").text(textData));
                            assertTrue(cursor.size() >= 0);
                        }

                        assertTrue(collection.hasIndex("unixTime"));
                    } catch (Throwable e) {
                        System.out.println("Exception at thread " +
                            Thread.currentThread().getName() + " with iteration " + j);
                        e.printStackTrace();
                    }
                }
                latch.countDown();
            });
        }

        latch.await();

        db.commit();

        assertTrue(collection.hasIndex("text"));
        assertTrue(collection.hasIndex("date"));

        DocumentCursor cursor = collection.find();
        assertEquals(cursor.size(), docCounter.get());

        cursor = collection.find(where("unixTime").gt(1));
        assertEquals(cursor.size(), docCounter.get());

        db.close();
    }

    @After
    public void cleanUp() {
        if (!inMemory) {
            File dbFile = new File(fileName);
            long fileSize = dbFile.length();
            assertTrue(fileSize > 0);
            dbFile.delete();
        }

        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            executor = null;
        }
    }

    private synchronized Document generate() {
        Document document = createDocument("unixTime", System.nanoTime() + docCounter.incrementAndGet());
        byte[] blob = new byte[1024];
        generator.nextBytes(blob);
        document.put("blob", blob);
        document.put("text", UUID.randomUUID().toString().replaceAll("-", " "));
        document.put("date", new Date());
        return document;
    }
}
