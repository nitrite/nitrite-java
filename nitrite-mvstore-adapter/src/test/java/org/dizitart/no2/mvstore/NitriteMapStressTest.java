/*
 * Copyright (c) 2019-2020. Nitrite author or authors.
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

package org.dizitart.no2.mvstore;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.Retry;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import static org.dizitart.no2.DbTestOperations.getRandomTempDbFile;
import static org.dizitart.no2.TestUtil.createDb;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Anindya Chatterjee.
 */
public class NitriteMapStressTest {
    private final String dbPath = getRandomTempDbFile();
    private Nitrite db;

    @Rule
    public Retry retry = new Retry(3);

    @Test
    public void testWithInsertReadUpdate() {
        db = createDb(dbPath);

        NitriteStore<?> nitriteStore = db.getStore();
        NitriteMap<String, Document> nitriteMap = nitriteStore.openMap("testWithInsertReadUpdate",
            String.class, Document.class);

        int count = 10000;
        for (int i = 0; i < count; i++) {
            Document record = Document.createDocument();
            record.put("firstName", UUID.randomUUID().toString());
            record.put("failed", false);
            record.put("lastName", UUID.randomUUID().toString());
            record.put("processed", false);

            nitriteMap.put(UUID.randomUUID().toString(), record);
        }

        for (Pair<String, Document> entry : nitriteMap.entries()) {
            String key = entry.getFirst();
            Document record = entry.getSecond();

            record.put("processed", true);

            nitriteMap.put(key, record);
        }

        db.close();
    }

    @Test
    public void testNullKey() {
        db = createDb(dbPath);
        NitriteStore<?> nitriteStore = db.getStore();
        NitriteMap<String, Document> nitriteMap = nitriteStore.openMap("testNullKey",
            String.class, Document.class);
        nitriteMap.put(null, Document.createDocument());

        assertNotNull(nitriteMap.get(null));
        assertEquals(nitriteMap.size(), 1);

        nitriteMap.put(null, Document.createDocument("first", 1));
        assertNotNull(nitriteMap.get(null));
        assertEquals(nitriteMap.size(), 1);
    }

    @Test(expected = ValidationException.class)
    public void testNullValue() {
        db = createDb(dbPath);
        NitriteStore<?> nitriteStore = db.getStore();
        NitriteMap<String, Document> nitriteMap = nitriteStore.openMap("testNullValue",
            String.class, Document.class);
        nitriteMap.put(null, null);
    }

    @Test(expected = ValidationException.class)
    public void testNullPutIfAbsent() {
        db = createDb(dbPath);
        NitriteStore<?> nitriteStore = db.getStore();
        NitriteMap<String, Document> nitriteMap = nitriteStore.openMap("testNullPutIfAbsent",
            String.class, Document.class);
        nitriteMap.putIfAbsent(null, null);
    }

    @After
    public void tearDown() throws IOException {
        if (db != null && !db.isClosed()) {
            db.close();
        }
        Files.delete(Paths.get(dbPath));
    }
}
