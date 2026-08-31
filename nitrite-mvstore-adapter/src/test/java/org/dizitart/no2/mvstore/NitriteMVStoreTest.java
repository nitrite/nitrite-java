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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.dizitart.no2.exceptions.NitriteIOException;
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
