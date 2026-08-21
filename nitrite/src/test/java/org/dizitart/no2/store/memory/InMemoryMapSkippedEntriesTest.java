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

package org.dizitart.no2.store.memory;

import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.store.NitriteStore;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Tests the default {@link org.dizitart.no2.store.NitriteMap#entries(long)}
 * contract via {@link InMemoryMap}.
 */
public class InMemoryMapSkippedEntriesTest {
    private InMemoryMap<Integer, String> map;

    @Before
    public void setUp() {
        map = new InMemoryMap<>("test", mock(NitriteStore.class));
        for (int i = 0; i < 10; i++) {
            map.put(i, "value-" + i);
        }
    }

    private static List<Integer> keysOf(RecordStream<Pair<Integer, String>> stream) {
        List<Integer> keys = new ArrayList<>();
        for (Pair<Integer, String> pair : stream) {
            keys.add(pair.getFirst());
        }
        return keys;
    }

    @Test
    public void testSkipZeroReturnsAllEntriesInOrder() {
        assertEquals(keysOf(map.entries()), keysOf(map.entries(0)));
        assertEquals(10, keysOf(map.entries(0)).size());
    }

    @Test
    public void testSkipReturnsSuffixInNaturalOrder() {
        List<Integer> keys = keysOf(map.entries(6));
        assertEquals(List.of(6, 7, 8, 9), keys);
    }

    @Test
    public void testSkipEqualToSizeReturnsEmpty() {
        assertTrue(keysOf(map.entries(10)).isEmpty());
    }

    @Test
    public void testSkipBeyondSizeReturnsEmpty() {
        assertTrue(keysOf(map.entries(100)).isEmpty());
    }

    @Test(expected = ValidationException.class)
    public void testNegativeSkipThrows() {
        map.entries(-1);
    }

    @Test
    public void testStreamIsReIterable() {
        RecordStream<Pair<Integer, String>> stream = map.entries(8);
        assertEquals(List.of(8, 9), keysOf(stream));
        assertEquals(List.of(8, 9), keysOf(stream));
    }

    @Test
    public void testSkipOnEmptyMapReturnsEmpty() {
        InMemoryMap<Integer, String> emptyMap = new InMemoryMap<>("empty", mock(NitriteStore.class));
        assertTrue(keysOf(emptyMap.entries(5)).isEmpty());
    }

    @Test
    public void testValuesArePairedWithCorrectKeys() {
        for (Pair<Integer, String> pair : map.entries(4)) {
            assertEquals("value-" + pair.getFirst(), pair.getSecond());
        }
    }
}
