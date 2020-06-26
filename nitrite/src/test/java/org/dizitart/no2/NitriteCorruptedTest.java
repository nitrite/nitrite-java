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
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.concurrent.ThreadPoolManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;

import static org.dizitart.no2.DbTestOperations.getRandomTempDbFile;
import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.filters.Filter.ALL;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee
 */
public class NitriteCorruptedTest {
    private Nitrite db;
    private NitriteCollection collection;
    private String fileName = getRandomTempDbFile();
    private Thread thread;
    private ExecutorService dbPool = ThreadPoolManager.getThreadPool(Runtime.getRuntime().availableProcessors(),
        "NitriteCorruptedTest");

    @Before
    public void setUp() {
        db = NitriteBuilder.get()
            .filePath(fileName)
            .compressed()
            .openOrCreate();

        collection = db.getCollection("test");
        collection.remove(ALL);

        thread = new Thread(() -> {
            for (int i = 0; i < 50000; i++) {

                // Interruption Guard
                if (Thread.interrupted()) {
                    break;
                }

                Document doc1 = createDocument(String.valueOf(System.currentTimeMillis()), "fn1")
                    .put("lastName", "ln1")
                    .put("data", new byte[]{1, 2, 3})
                    .put("body", "a quick brown fox jump over the lazy dog");

                // Separate user thread from Db write thread
                dbPool.submit(() -> collection.insert(doc1));
            }
        });
    }

    @After
    public void tearDown() throws IOException {
        if (collection.isOpen()) {
            collection.remove(ALL);
            collection.close();
        }
        db.close();
        Files.delete(Paths.get(fileName));
    }

    @Test(timeout = 10000)
    public void issue118() throws InterruptedException {
        thread.start();
        Thread.sleep(10);
        thread.interrupt();
        Thread.sleep(500);
        assertTrue(collection.isOpen());
        assertFalse(db.isClosed());
    }
}
