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

package org.dizitart.no2.transaction;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.store.StoreConfig;
import org.dizitart.no2.store.events.StoreEventListener;
import org.dizitart.no2.store.memory.InMemoryConfig;
import org.dizitart.no2.store.memory.InMemoryRTree;
import org.dizitart.no2.store.memory.InMemoryStore;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TransactionalStoreTest {
    @Test
    public void testCommit() {
        assertThrows(InvalidOperationException.class,
            () -> (new TransactionalStore<>(new TransactionalStore<>(
                new TransactionalStore<>(new TransactionalStore<>(null))))).commit());
    }

    @Test
    public void testClose() throws Exception {
        TransactionalStore<InMemoryConfig> transactionalStore = new TransactionalStore<>(
            new TransactionalStore<>(
                new TransactionalStore<>(new TransactionalStore<>(new InMemoryStore()))));
        transactionalStore.close();
        assertNotNull(transactionalStore.getCatalog());
    }

    @Test
    public void testHasMap() {
        NitriteStore<StoreConfig> nitriteStore = (NitriteStore<StoreConfig>) mock(NitriteStore.class);
        when(nitriteStore.hasMap(anyString())).thenReturn(true);
        TransactionalStore<StoreConfig> transactionalStore = new TransactionalStore<>(
            new TransactionalStore<>(new TransactionalStore<>(nitriteStore)));
        assertTrue(transactionalStore.hasMap("Map Name"));
        verify(nitriteStore).hasMap(anyString());
        assertNull(transactionalStore.getStoreVersion());
    }

    @Test
    public void testHasMap2() {
        NitriteStore<StoreConfig> nitriteStore = (NitriteStore<StoreConfig>) mock(NitriteStore.class);
        when(nitriteStore.hasMap(anyString())).thenReturn(false);
        TransactionalStore<StoreConfig> transactionalStore = new TransactionalStore<>(
            new TransactionalStore<>(new TransactionalStore<>(nitriteStore)));
        assertFalse(transactionalStore.hasMap("Map Name"));
        verify(nitriteStore).hasMap(anyString());
        assertNull(transactionalStore.getStoreVersion());
    }

    @Test
    public void testOpenRTree() {
        NitriteStore<StoreConfig> nitriteStore = (NitriteStore<StoreConfig>) mock(NitriteStore.class);
        when(nitriteStore.openRTree(anyString(), any(), any()))
            .thenReturn(new InMemoryRTree<>());
        when(nitriteStore.hasMap(anyString())).thenReturn(true);
        TransactionalStore<StoreConfig> transactionalStore = new TransactionalStore<>(
            new TransactionalStore<>(new TransactionalStore<>(nitriteStore)));
        Class<?> keyType = Object.class;
        assertEquals(0L, transactionalStore.openRTree("R Tree Name", keyType, Object.class).size());
        verify(nitriteStore, times(3)).hasMap(anyString());
        verify(nitriteStore).openRTree(anyString(), any(), any());
        assertNull(transactionalStore.getStoreVersion());
    }

    @Test
    public void testOpenRTree2() {
        NitriteStore<StoreConfig> nitriteStore = (NitriteStore<StoreConfig>) mock(NitriteStore.class);
        when(nitriteStore.openRTree(anyString(), any(), any()))
            .thenReturn(new InMemoryRTree<>());
        when(nitriteStore.hasMap(anyString())).thenReturn(false);
        TransactionalStore<StoreConfig> transactionalStore = new TransactionalStore<>(
            new TransactionalStore<>(new TransactionalStore<>(nitriteStore)));
        Class<?> keyType = Object.class;
        assertEquals(0L, transactionalStore.openRTree("R Tree Name", keyType, Object.class).size());
        verify(nitriteStore).hasMap(anyString());
        assertNull(transactionalStore.getStoreVersion());
    }

    @Test
    public void testGetStoreVersion() {
        NitriteStore<StoreConfig> nitriteStore = (NitriteStore<StoreConfig>) mock(NitriteStore.class);
        when(nitriteStore.getStoreVersion()).thenReturn("foo");
        assertEquals("foo", (new TransactionalStore<>(
            new TransactionalStore<>(new TransactionalStore<>(nitriteStore)))).getStoreVersion());
        verify(nitriteStore).getStoreVersion();
    }

    @Test
    public void testConstructor() {
        TransactionalStore<InMemoryConfig> transactionalStore = new TransactionalStore<>(new InMemoryStore());
        TransactionalStore<InMemoryConfig> actualTransactionalStore = new TransactionalStore<>(transactionalStore);
        actualTransactionalStore.initialize(new NitriteConfig());
        actualTransactionalStore.openOrCreate();
        actualTransactionalStore.subscribe(mock(StoreEventListener.class));
        actualTransactionalStore.unsubscribe(mock(StoreEventListener.class));
        assertTrue(transactionalStore.hasUnsavedChanges());
        assertTrue(actualTransactionalStore.hasUnsavedChanges());
        assertFalse(transactionalStore.isClosed());
        assertFalse(actualTransactionalStore.isClosed());
        assertFalse(transactionalStore.isReadOnly());
        assertFalse(actualTransactionalStore.isReadOnly());
    }

    @Test
    public void testConstructor2() {
        TransactionalStore<InMemoryConfig> transactionalStore = new TransactionalStore<>(
            new TransactionalStore<>(
                new TransactionalStore<>(new TransactionalStore<>(new InMemoryStore()))));
        TransactionalStore<InMemoryConfig> actualTransactionalStore = new TransactionalStore<>(transactionalStore);
        assertNotNull(actualTransactionalStore.getCatalog());
        assertFalse(actualTransactionalStore.isReadOnly());
        assertFalse(actualTransactionalStore.isClosed());
        assertTrue(actualTransactionalStore.hasUnsavedChanges());
        assertNull(actualTransactionalStore.getStoreConfig());
        assertNotNull(transactionalStore.getCatalog());
        assertFalse(transactionalStore.isReadOnly());
        assertFalse(transactionalStore.isClosed());
        assertTrue(transactionalStore.hasUnsavedChanges());
        assertNull(transactionalStore.getStoreConfig());
    }
}

