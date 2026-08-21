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

import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.exceptions.ValidationException;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the MVStore-backed override of
 * {@link org.dizitart.no2.store.NitriteMap#entries(long)} which must seek via
 * {@link MVMap#getKey(long)} instead of iterating over the skipped entries.
 */
public class NitriteMVMapSkippedEntriesTest {
    private static final int SIZE = 1000;

    private MVStore mvStore;
    private NitriteMVMap<Long, String> nitriteMVMap;

    @Before
    public void setUp() {
        mvStore = MVStore.open(null); // in-memory store
        MVMap<Long, String> mvMap = mvStore.openMap("test");
        // populate the backing map directly, out of order, to prove natural key order
        for (long i = SIZE - 1; i >= 0; i--) {
            mvMap.put(i, "value-" + i);
        }
        nitriteMVMap = new NitriteMVMap<>(mvMap, null);
    }

    @After
    public void tearDown() {
        mvStore.close();
    }

    private static List<Long> keysOf(RecordStream<Pair<Long, String>> stream) {
        List<Long> keys = new ArrayList<>();
        for (Pair<Long, String> pair : stream) {
            keys.add(pair.getFirst());
        }
        return keys;
    }

    private static List<Long> range(long fromInclusive, long toExclusive) {
        return LongStream.range(fromInclusive, toExclusive).boxed().collect(Collectors.toList());
    }

    @Test
    public void testSkipZeroReturnsAllEntriesInOrder() {
        assertEquals(range(0, SIZE), keysOf(nitriteMVMap.entries(0)));
        assertEquals(keysOf(nitriteMVMap.entries()), keysOf(nitriteMVMap.entries(0)));
    }

    @Test
    public void testSkipReturnsSuffixInNaturalOrder() {
        assertEquals(range(750, SIZE), keysOf(nitriteMVMap.entries(750)));
    }

    @Test
    public void testSkipOneEntryShort() {
        assertEquals(List.of((long) (SIZE - 1)), keysOf(nitriteMVMap.entries(SIZE - 1)));
    }

    @Test
    public void testSkipEqualToSizeReturnsEmpty() {
        assertTrue(keysOf(nitriteMVMap.entries(SIZE)).isEmpty());
    }

    @Test
    public void testSkipBeyondSizeReturnsEmpty() {
        assertTrue(keysOf(nitriteMVMap.entries(SIZE + 500)).isEmpty());
    }

    @Test(expected = ValidationException.class)
    public void testNegativeSkipThrows() {
        nitriteMVMap.entries(-1);
    }

    @Test
    public void testStreamIsReIterable() {
        RecordStream<Pair<Long, String>> stream = nitriteMVMap.entries(998);
        assertEquals(List.of(998L, 999L), keysOf(stream));
        assertEquals(List.of(998L, 999L), keysOf(stream));
    }

    @Test
    public void testStreamReflectsRemovalsOnReIteration() {
        RecordStream<Pair<Long, String>> stream = nitriteMVMap.entries(SIZE - 2);
        assertEquals(List.of((long) (SIZE - 2), (long) (SIZE - 1)), keysOf(stream));

        // removing an entry shifts the suffix; a fresh iteration must see the new state
        mvStore.<Long, String>openMap("test").remove(0L);
        assertEquals(List.of((long) (SIZE - 1)), keysOf(stream));
    }

    @Test
    public void testSkipOnEmptyMapReturnsEmpty() {
        MVMap<Long, String> emptyMap = mvStore.openMap("empty");
        NitriteMVMap<Long, String> emptyNitriteMap = new NitriteMVMap<>(emptyMap, null);
        assertTrue(keysOf(emptyNitriteMap.entries(5)).isEmpty());
        assertTrue(keysOf(emptyNitriteMap.entries(0)).isEmpty());
    }

    @Test
    public void testValuesArePairedWithCorrectKeys() {
        for (Pair<Long, String> pair : nitriteMVMap.entries(123)) {
            assertEquals("value-" + pair.getFirst(), pair.getSecond());
        }
    }

    @Test
    public void testEveryOffsetMatchesFullScanSuffix() {
        List<Long> full = keysOf(nitriteMVMap.entries());
        // covers both branches of MVStore's positional seek (small and large offsets)
        for (long skip : new long[]{1, 5, 9, 10, 63, 64, 65, 500, 999}) {
            assertEquals("skip=" + skip, full.subList((int) skip, SIZE),
                keysOf(nitriteMVMap.entries(skip)));
        }
    }

    /**
     * A page past the end must be empty. MVStore's own {@code Cursor.skip(long)}
     * resets to the first entry instead of exhausting the cursor when it skips
     * past the end, which would turn a page past the end into the whole
     * collection and make a "read pages until one comes back empty" loop
     * never terminate.
     */
    @Test
    public void testSkipPastEndNeverRestartsFromTheBeginning() {
        for (long skip : new long[]{SIZE, SIZE + 1, SIZE + 10, 2L * SIZE, Integer.MAX_VALUE}) {
            assertTrue("skip=" + skip, keysOf(nitriteMVMap.entries(skip)).isEmpty());
        }
    }

    /**
     * The scan must read one consistent snapshot: entries committed after the
     * iterator was created must not appear part-way through it.
     */
    @Test
    public void testIterationReadsASingleSnapshot() {
        MVMap<Long, String> mvMap = mvStore.openMap("test");

        Iterator<Pair<Long, String>> iterator = nitriteMVMap.entries(SIZE - 4).iterator();
        assertEquals(Long.valueOf(SIZE - 4), iterator.next().getFirst());

        // committed after the iterator pinned its snapshot, inside the range still to be read
        mvMap.put((long) SIZE - 3, "changed");
        mvMap.put((long) SIZE + 1, "appended");

        List<Long> remaining = new ArrayList<>();
        while (iterator.hasNext()) {
            remaining.add(iterator.next().getFirst());
        }
        assertEquals(List.of((long) SIZE - 3, (long) SIZE - 2, (long) SIZE - 1), remaining);
    }

    /**
     * Under a writer that repeatedly shrinks the map below the paged offset, a
     * page must never come back starting at the first key: that is the
     * signature of a seek that fell back to the beginning of the tree, and it
     * would feed a pager the same rows forever.
     */
    @Test
    public void testConcurrentShrinkNeverYieldsTheFirstKey() throws Exception {
        MVMap<Long, String> mvMap = mvStore.openMap("test");
        long skip = SIZE / 2;

        AtomicBoolean stopped = new AtomicBoolean(false);
        AtomicReference<Throwable> writerFailure = new AtomicReference<>();
        Thread writer = new Thread(() -> {
            try {
                while (!stopped.get()) {
                    // never touches key 0, so the minimum key is stable
                    for (long i = 1; i < SIZE - 1; i++) {
                        mvMap.remove(i);
                    }
                    for (long i = 1; i < SIZE - 1; i++) {
                        mvMap.put(i, "value-" + i);
                    }
                }
            } catch (Throwable t) {
                writerFailure.set(t);
            }
        });

        writer.start();
        try {
            for (int i = 0; i < 500; i++) {
                List<Long> page = keysOf(nitriteMVMap.entries(skip));
                if (!page.isEmpty()) {
                    assertNotEquals("page restarted at the first key", Long.valueOf(0), page.get(0));
                    assertTrue("page starts before the requested offset", page.get(0) >= skip);
                    // a window stitched together from two snapshots would repeat or
                    // go backwards here
                    for (int k = 1; k < page.size(); k++) {
                        assertTrue("page keys are not strictly increasing at " + k,
                            page.get(k) > page.get(k - 1));
                    }
                }
            }
        } finally {
            stopped.set(true);
            writer.join(30_000);
        }

        if (writerFailure.get() != null) {
            throw new AssertionError("writer failed", writerFailure.get());
        }
    }
}
