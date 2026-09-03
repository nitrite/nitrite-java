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

package org.dizitart.no2.collection;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.integration.Retry;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * A long write on one collection must not stall {@code getCollection} for every other
 * collection.
 * <p>
 * {@link CollectionFactory#getCollection} decides whether a registered collection is still
 * usable by calling {@code isDropped()} and {@code isOpen()}, which take that collection's
 * read lock. While a write holds the collection's write lock, a caller asking for that
 * collection waits on it, which is expected. The factory used to make that check while
 * holding its own factory-wide lock, so that one waiting caller also blocked every caller
 * asking for any other collection until the write finished.
 */
public class CollectionFactoryConvoyTest {
    @Rule
    public Retry retry = new Retry(3);

    private Nitrite db;
    private ExecutorService executor;

    @Before
    public void setUp() {
        db = Nitrite.builder().openOrCreate();
        executor = Executors.newCachedThreadPool();
    }

    @After
    public void tearDown() {
        executor.shutdownNow();
        db.close();
    }

    @Test
    public void testWriteOnOneCollectionDoesNotBlockGetCollectionOfAnother() throws Exception {
        NitriteCollection busy = db.getCollection("busy");
        busy.insert(Document.createDocument("key", 1));
        NitriteCollection other = db.getCollection("other");

        // a remove(filter) evaluates the filter under the collection's write lock; park it there
        CountDownLatch inFilter = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Filter parked = element -> {
            inFilter.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return false;
        };
        Future<?> writer = executor.submit(() -> busy.remove(parked));
        assertTrue("writer never entered the filter", inFilter.await(5, TimeUnit.SECONDS));

        // a caller asking for the busy collection has to wait for that write; that is expected
        Future<NitriteCollection> waitingForBusy = executor.submit(() -> db.getCollection("busy"));
        Thread.sleep(200);
        assertTrue("caller for the busy collection should be waiting", !waitingForBusy.isDone());

        // ... but callers asking for other collections, registered or new, must not queue behind it
        Future<NitriteCollection> registered = executor.submit(() -> db.getCollection("other"));
        Future<NitriteCollection> created = executor.submit(() -> db.getCollection("fresh"));
        try {
            assertSame(other, registered.get(2, TimeUnit.SECONDS));
            assertTrue(created.get(2, TimeUnit.SECONDS).isOpen());
        } finally {
            release.countDown();
        }

        writer.get(5, TimeUnit.SECONDS);
        assertSame(busy, waitingForBusy.get(5, TimeUnit.SECONDS));
    }
}
