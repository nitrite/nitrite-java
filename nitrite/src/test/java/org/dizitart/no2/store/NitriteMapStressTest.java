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

package org.dizitart.no2.store;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.KeyValuePair;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import static org.dizitart.no2.DbTestOperations.getRandomTempDbFile;

/**
 * @author Anindya Chatterjee.
 */
public class NitriteMapStressTest {
    private String dbPath = getRandomTempDbFile();

    @Test
    public void testWithInsertReadUpdate() throws IOException {
        MVStore store = MVStore.open(dbPath);
        MVMap<String, Document> map = store.openMap("map-test");
        MVStoreConfig storeConfig = MVStoreConfig.create();
        NitriteStore nitriteStore = new NitriteMVStore();
        nitriteStore.openOrCreate(null, null, storeConfig);
        NitriteMap<String, Document> nitriteMap = new NitriteMVMap<>(map, nitriteStore);

        int count = 10000;
        for (int i = 0; i < count; i++) {
            Document record = Document.createDocument();
            record.put("firstName", UUID.randomUUID().toString());
            record.put("failed", false);
            record.put("lastName", UUID.randomUUID().toString());
            record.put("processed", false);

            nitriteMap.put(UUID.randomUUID().toString(), record);
        }

        for (KeyValuePair<String, Document> entry : nitriteMap.entries()) {
            String key = entry.getKey();
            Document record = entry.getValue();

            record.put("processed", true);

            nitriteMap.put(key, record);
        }

        store.close();

        Files.delete(Paths.get(dbPath));
    }
}
