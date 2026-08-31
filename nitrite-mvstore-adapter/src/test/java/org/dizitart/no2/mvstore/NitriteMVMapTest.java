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
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.store.NitriteStore;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class NitriteMVMapTest {
    private MVMap<Object, Object> mvMap;
    private MVStore mvStore;
    private MVStore.TxCounter txCounter;
    private NitriteMVMap<Object, Object> nitriteMVMap;

    @Before
    public void setUp() {
        //noinspection unchecked
        mvMap = (MVMap<Object, Object>) mock(MVMap.class);
        mvStore = mock(MVStore.class);
        txCounter = mock(MVStore.TxCounter.class);
        NitriteStore<?> nitriteStore = mock(NitriteStore.class);
        when(mvMap.getStore()).thenReturn(mvStore);
        when(mvStore.registerVersionUsage()).thenReturn(txCounter);
        nitriteMVMap = new NitriteMVMap<>(mvMap, nitriteStore);
    }

    @Test
    public void testValues() {
        when(mvMap.values()).thenReturn(Collections.emptyList());
        assertTrue(nitriteMVMap.values().toList().isEmpty());

        InOrder inOrder = inOrder(mvStore, mvMap);
        inOrder.verify(mvStore).registerVersionUsage();
        //noinspection ResultOfMethodCallIgnored
        inOrder.verify(mvMap).values();
        inOrder.verify(mvStore).deregisterVersionUsage(txCounter);
        assertFalse(nitriteMVMap.isEmpty());
    }

    @Test
    public void testKeys() {
        when(mvMap.keySet()).thenReturn(new HashSet<>());
        assertTrue(nitriteMVMap.keys().toList().isEmpty());

        verify(mvMap).keySet();
        verify(mvStore).deregisterVersionUsage(txCounter);
        assertFalse(nitriteMVMap.isEmpty());
    }

    @Test
    public void testEntries() {
        when(mvMap.entrySet()).thenReturn(Collections.<Object, Object>singletonMap("key", "value").entrySet());
        assertEquals(1, nitriteMVMap.entries().toList().size());

        verify(mvStore).registerVersionUsage();
        verify(mvStore).deregisterVersionUsage(txCounter);
    }

    @Test
    public void testReversedEntries() {
        when(mvMap.getVersion()).thenReturn(1L);
        when(mvMap.openVersion(1L)).thenReturn(mvMap);
        when(mvMap.lastKey()).thenReturn(1);
        when(mvMap.floorKey(1)).thenReturn(1);

        assertEquals(1, nitriteMVMap.reversedEntries().toList().size());
        verify(mvStore).registerVersionUsage();
        verify(mvStore).deregisterVersionUsage(txCounter);
    }

    @Test
    public void testIteratorCreationFailureReleasesVersion() {
        when(mvMap.values()).thenThrow(new IllegalStateException());

        assertThrows(IllegalStateException.class, () -> nitriteMVMap.values().iterator());
        verify(mvStore).registerVersionUsage();
        verify(mvStore).deregisterVersionUsage(txCounter);
    }

    @Test
    public void testExhaustedIteratorRemainsExhausted() {
        when(mvMap.values()).thenReturn(Collections.singletonList("value"));
        Iterator<Object> iterator = nitriteMVMap.values().iterator();

        assertTrue(iterator.hasNext());
        assertEquals("value", iterator.next());
        assertFalse(iterator.hasNext());
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    public void testAbandonedIteratorReleasesVersionOnClose() {
        when(mvMap.values()).thenReturn(Arrays.asList("first", "second"));
        Iterator<Object> iterator = nitriteMVMap.values().iterator();

        assertEquals("first", iterator.next());
        verify(mvStore, never()).deregisterVersionUsage(txCounter);

        nitriteMVMap.close();
        verify(mvStore).deregisterVersionUsage(txCounter);
        assertThrows(NitriteIOException.class, iterator::hasNext);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructor() {
        NitriteMVMap<Object, Object> actualNitriteMVMap = new NitriteMVMap<>(mvMap, null);
        actualNitriteMVMap.close();
        assertFalse(actualNitriteMVMap.isEmpty());
    }

    @Test
    public void testIsEmpty() {
        when(mvMap.isEmpty()).thenReturn(true);
        assertTrue(nitriteMVMap.isEmpty());
        verify(mvMap).isEmpty();
    }
}
