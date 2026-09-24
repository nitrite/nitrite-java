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

package org.dizitart.no2.integration.collection;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.filters.FluentFilter;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;

import static org.junit.Assert.assertEquals;

/**
 * Regression test for <a href="https://github.com/nitrite/nitrite-java/issues/1315">Issue 1315</a>.
 * <p>
 * On 5.3.0 every close left an empty legacy index map behind, and the first finds on a
 * reopened file database raced to migrate and drop it, failing with a
 * {@code NullPointerException} in {@code NitriteMVStore.removeMap}. Fixed by #1295 and #1309.
 */
public class Issue1315Test {

    private static Nitrite open(File file) {
        return Nitrite.builder()
            .loadModule(MVStoreModule.withConfig().filePath(file.getPath()).build())
            .openOrCreate();
    }

    @Test
    public void firstUseOfANonUniqueIndexFromSeveralThreads() throws Exception {
        File file = File.createTempFile("nitrite-legacy-race", ".db");
        file.delete();
        try {
            Nitrite db = open(file);
            NitriteCollection items = db.getCollection("items");
            items.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "state");
            items.insert(Document.createDocument("state", "new"));
            db.close();

            int threads = 4, openings = 100;
            List<Throwable> failures = new CopyOnWriteArrayList<>();
            for (int opening = 0; opening < openings; opening++) {
                Nitrite reopened = open(file);
                NitriteCollection collection = reopened.getCollection("items");
                CyclicBarrier start = new CyclicBarrier(threads);
                Thread[] workers = new Thread[threads];
                for (int i = 0; i < threads; i++) {
                    workers[i] = new Thread(() -> {
                        try {
                            start.await();
                            assertEquals(1, collection.find(FluentFilter.where("state").eq("new")).toList().size());
                        } catch (Throwable t) {
                            failures.add(t);
                        }
                    });
                    workers[i].start();
                }
                for (Thread worker : workers) worker.join();
                reopened.close();
            }
            assertEquals(failures.toString(), 0, failures.size());
        } finally {
            file.delete();
        }
    }
}
