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

package org.dizitart.no2.mvstore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.dizitart.no2.common.util.IndexUtils.deriveIndexMapName;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.FindPlan;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.DBValue;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.filters.ComparableFilter;
import org.dizitart.no2.filters.IndexScanFilter;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.index.NonUniqueIndexer;
import org.dizitart.no2.store.NitriteMap;
import org.h2.mvstore.MVStore;
import org.junit.Test;

public class NitriteMVStoreTest {

    @Test
    public void testConstructor() {
        final NitriteMVStore actualNitriteMVStore = new NitriteMVStore();
        assertNull(actualNitriteMVStore.getStoreConfig());
        assertTrue(actualNitriteMVStore.isClosed());
        assertFalse(actualNitriteMVStore.hasUnsavedChanges());
        assertNotNull(actualNitriteMVStore.getStoreVersion());
    }

    @Test
    public void testOpenOrCreate() {
        final NitriteMVStore nitriteMVStore = new NitriteMVStore();
        nitriteMVStore.setStoreConfig(new MVStoreConfig());
        nitriteMVStore.openOrCreate();
        assertFalse(nitriteMVStore.isReadOnly());
        assertFalse(nitriteMVStore.isClosed());
        assertFalse(nitriteMVStore.hasUnsavedChanges());
    }

    @Test
    public void testIsClosed() {
        assertTrue((new NitriteMVStore()).isClosed());
    }

    @Test
    public void testHasUnsavedChanges() {
        assertFalse((new NitriteMVStore()).hasUnsavedChanges());
    }

    @Test
    public void testGetStoreVersion() {
        assertNotNull((new NitriteMVStore()).getStoreVersion());
    }

    @Test
    public void testIteratorCannotReadAfterStoreClose() throws Exception {

        final Path storeFile = Files.createTempFile("nitrite-lifecycle-", ".db");
        Files.delete(storeFile);
        final NitriteMVStore nitriteMVStore = new NitriteMVStore();
        final MVStoreConfig config = new MVStoreConfig();
        config.filePath(storeFile.toString());
        config.autoCompact(true);
        nitriteMVStore.setStoreConfig(config);

        try {
            nitriteMVStore.openOrCreate();
            final NitriteMap<Integer, String> map = nitriteMVStore.openMap("test", Integer.class, String.class);
            for (int i = 0; i < 100; i++) {
                map.put(i, "value-" + i);
            }

            final Iterator<String> iterator = map.values().iterator();
            assertTrue(iterator.hasNext());
            iterator.next();

            nitriteMVStore.close();

            final NitriteIOException exception = assertThrows(NitriteIOException.class, iterator::hasNext);
            assertEquals("MVStore is closed", exception.getMessage());
        } finally {
            if (!nitriteMVStore.isClosed()) {
                nitriteMVStore.close();
            }
            Files.deleteIfExists(storeFile);
        }
    }

    @Test
    public void testCompactingClosesAreSerialized() throws Exception {

        final String originalCompactThreads = System.getProperty("h2.compactThreads");
        final CountDownLatch firstCloseStarted = new CountDownLatch(1);
        final CountDownLatch releaseFirstClose = new CountDownLatch(1);
        final CountDownLatch secondCloseAttempted = new CountDownLatch(1);
        final CountDownLatch secondCloseStarted = new CountDownLatch(1);
        final ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            System.setProperty("h2.compactThreads", "4");
            final MVStore firstMVStore = mock(MVStore.class);
            doAnswer(invocation -> {
                assertEquals("1", System.getProperty("h2.compactThreads"));
                firstCloseStarted.countDown();
                assertTrue(releaseFirstClose.await(5, TimeUnit.SECONDS));
                return null;
            }).when(firstMVStore).close(anyInt());

            final MVStore secondMVStore = mock(MVStore.class);
            doAnswer(invocation -> {
                secondCloseStarted.countDown();
                assertEquals("1", System.getProperty("h2.compactThreads"));
                return null;
            }).when(secondMVStore).close(anyInt());

            final NitriteMVStore firstStore = createCompactingStore(firstMVStore);
            final NitriteMVStore secondStore = createCompactingStore(secondMVStore);
            final Future<?> firstClose = executorService.submit(firstStore::close);
            assertTrue(firstCloseStarted.await(5, TimeUnit.SECONDS));

            final Future<?> secondClose = executorService.submit(() -> {
                secondCloseAttempted.countDown();
                secondStore.close();
            });
            assertTrue(secondCloseAttempted.await(5, TimeUnit.SECONDS));
            assertFalse(secondCloseStarted.await(200, TimeUnit.MILLISECONDS));

            releaseFirstClose.countDown();
            firstClose.get(5, TimeUnit.SECONDS);
            secondClose.get(5, TimeUnit.SECONDS);
            // the second close restores what the first one saved, not the "1" it saw in flight
            assertEquals("4", System.getProperty("h2.compactThreads"));
        } finally {
            releaseFirstClose.countDown();
            executorService.shutdownNow();
            if (originalCompactThreads == null) {
                System.clearProperty("h2.compactThreads");
            } else {
                System.setProperty("h2.compactThreads", originalCompactThreads);
            }
        }
    }

    @Test
    public void testDropThroughAWrapperWhoseMapIsAlreadyGone() {
        // MVMap.getName() answers null once the map is removed, so a second holder's drop()
        // used to hand removeMap a null name and fail
        final NitriteMVStore store = openInMemoryStore();
        try {
            NitriteMap<String, String> stale = store.openMap("m", String.class, String.class);
            stale.close();
            NitriteMap<String, String> current = store.openMap("m", String.class, String.class);
            current.drop();

            stale.drop();
            assertEquals("m", stale.getName());
            assertFalse(store.hasMap("m"));
        } finally {
            store.close();
        }
    }

    @Test
    public void testConcurrentFirstReadOfALegacyIndexMigratesOnce() throws Exception {
        // the first multi-threaded read after a reopen used to build one index instance per
        // thread, each migrating and dropping the legacy map; the second drop failed
        final int threads = 16;
        final ExecutorService executorService = Executors.newFixedThreadPool(threads);
        try {
            for (int round = 0; round < 50; round++) {
                final NitriteMVStore store = openInMemoryStore();
                try {
                    final IndexDescriptor desc = new IndexDescriptor(IndexType.NON_UNIQUE,
                        Fields.withNames("a"), "c");
                    // two ids, so the migration's second put is the composite map's first key
                    // comparison: H2's ObjectDataType settles its key type on first use without
                    // any synchronization, and sixteen readers making that first comparison at
                    // once can fail inside H2 with "Can not compare" (h2 2.4.240 and 2.5.250)
                    final Set<NitriteId> ids = new LinkedHashSet<>(Arrays.asList(
                        NitriteId.createId(1L), NitriteId.createId(2L)));
                    store.openMap(deriveIndexMapName(desc), DBValue.class, ArrayList.class)
                        .put(new DBValue("k"), new ArrayList<>(ids));

                    final NitriteConfig config = mock(NitriteConfig.class);
                    doReturn(store).when(config).getNitriteStore();
                    final FindPlan plan = new FindPlan();
                    plan.setIndexDescriptor(desc);
                    plan.setIndexScanFilter(new IndexScanFilter(Collections.singletonList(
                        (ComparableFilter) where("a").eq("k"))));

                    final NonUniqueIndexer indexer = new NonUniqueIndexer();
                    final CountDownLatch start = new CountDownLatch(1);
                    final List<Future<Set<NitriteId>>> reads = new ArrayList<>();
                    for (int t = 0; t < threads; t++) {
                        reads.add(executorService.submit(() -> {
                            start.await();
                            return indexer.findByFilter(plan, config);
                        }));
                    }
                    start.countDown();
                    for (Future<Set<NitriteId>> read : reads) {
                        assertEquals(ids, read.get(5, TimeUnit.SECONDS));
                    }
                    assertFalse(store.hasMap(deriveIndexMapName(desc)));
                } finally {
                    store.close();
                }
            }
        } finally {
            executorService.shutdownNow();
        }
    }

    private NitriteMVStore openInMemoryStore() {
        final NitriteMVStore store = new NitriteMVStore();
        store.setStoreConfig(new MVStoreConfig());
        store.openOrCreate();
        return store;
    }

    private NitriteMVStore createCompactingStore(final MVStore mvStore) throws Exception {
        final NitriteMVStore nitriteMVStore = new NitriteMVStore();
        final MVStoreConfig config = new MVStoreConfig();
        config.autoCompact(true);
        nitriteMVStore.setStoreConfig(config);

        final Field mvStoreField = NitriteMVStore.class.getDeclaredField("mvStore");
        mvStoreField.setAccessible(true);
        mvStoreField.set(nitriteMVStore, mvStore);
        return nitriteMVStore;
    }
}
