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

public class TransactionStoreTest {
    @Test
    public void testCommit() {
        assertThrows(InvalidOperationException.class,
            () -> (new TransactionStore<>(new TransactionStore<>(
                new TransactionStore<>(new TransactionStore<>(null))))).commit());
    }

    @Test
    public void testClose() throws Exception {
        TransactionStore<InMemoryConfig> transactionStore = new TransactionStore<>(
            new TransactionStore<>(
                new TransactionStore<>(new TransactionStore<>(new InMemoryStore()))));
        transactionStore.close();
        assertNotNull(transactionStore.getCatalog());
    }

    @Test
    public void testHasMap() {
        NitriteStore<StoreConfig> nitriteStore = (NitriteStore<StoreConfig>) mock(NitriteStore.class);
        when(nitriteStore.hasMap(anyString())).thenReturn(true);
        TransactionStore<StoreConfig> transactionStore = new TransactionStore<>(
            new TransactionStore<>(new TransactionStore<>(nitriteStore)));
        assertTrue(transactionStore.hasMap("Map Name"));
        verify(nitriteStore).hasMap(anyString());
        assertNull(transactionStore.getStoreVersion());
    }

    @Test
    public void testHasMap2() {
        NitriteStore<StoreConfig> nitriteStore = (NitriteStore<StoreConfig>) mock(NitriteStore.class);
        when(nitriteStore.hasMap(anyString())).thenReturn(false);
        TransactionStore<StoreConfig> transactionStore = new TransactionStore<>(
            new TransactionStore<>(new TransactionStore<>(nitriteStore)));
        assertFalse(transactionStore.hasMap("Map Name"));
        verify(nitriteStore).hasMap(anyString());
        assertNull(transactionStore.getStoreVersion());
    }

    @Test
    public void testOpenRTree() {
        NitriteStore<StoreConfig> nitriteStore = (NitriteStore<StoreConfig>) mock(NitriteStore.class);
        when(nitriteStore.openRTree(anyString(), any(), any()))
            .thenReturn(new InMemoryRTree<>());
        when(nitriteStore.hasMap(anyString())).thenReturn(true);
        TransactionStore<StoreConfig> transactionStore = new TransactionStore<>(
            new TransactionStore<>(new TransactionStore<>(nitriteStore)));
        Class<?> keyType = Object.class;
        assertEquals(0L, transactionStore.openRTree("R Tree Name", keyType, Object.class).size());
        verify(nitriteStore, times(3)).hasMap(anyString());
        verify(nitriteStore).openRTree(anyString(), any(), any());
        assertNull(transactionStore.getStoreVersion());
    }

    @Test
    public void testOpenRTree2() {
        NitriteStore<StoreConfig> nitriteStore = (NitriteStore<StoreConfig>) mock(NitriteStore.class);
        when(nitriteStore.openRTree(anyString(), any(), any()))
            .thenReturn(new InMemoryRTree<>());
        when(nitriteStore.hasMap(anyString())).thenReturn(false);
        TransactionStore<StoreConfig> transactionStore = new TransactionStore<>(
            new TransactionStore<>(new TransactionStore<>(nitriteStore)));
        Class<?> keyType = Object.class;
        assertEquals(0L, transactionStore.openRTree("R Tree Name", keyType, Object.class).size());
        verify(nitriteStore).hasMap(anyString());
        assertNull(transactionStore.getStoreVersion());
    }

    @Test
    public void testGetStoreVersion() {
        NitriteStore<StoreConfig> nitriteStore = (NitriteStore<StoreConfig>) mock(NitriteStore.class);
        when(nitriteStore.getStoreVersion()).thenReturn("foo");
        assertEquals("foo", (new TransactionStore<>(
            new TransactionStore<>(new TransactionStore<>(nitriteStore)))).getStoreVersion());
        verify(nitriteStore).getStoreVersion();
    }

    @Test
    public void testConstructor() {
        TransactionStore<InMemoryConfig> transactionStore = new TransactionStore<>(new InMemoryStore());
        TransactionStore<InMemoryConfig> actualTransactionStore = new TransactionStore<>(transactionStore);
        actualTransactionStore.initialize(new NitriteConfig());
        actualTransactionStore.openOrCreate();
        actualTransactionStore.subscribe(mock(StoreEventListener.class));
        actualTransactionStore.unsubscribe(mock(StoreEventListener.class));
        assertTrue(transactionStore.hasUnsavedChanges());
        assertTrue(actualTransactionStore.hasUnsavedChanges());
        assertFalse(transactionStore.isClosed());
        assertFalse(actualTransactionStore.isClosed());
        assertFalse(transactionStore.isReadOnly());
        assertFalse(actualTransactionStore.isReadOnly());
    }

    @Test
    public void testConstructor2() {
        TransactionStore<InMemoryConfig> transactionStore = new TransactionStore<>(
            new TransactionStore<>(
                new TransactionStore<>(new TransactionStore<>(new InMemoryStore()))));
        TransactionStore<InMemoryConfig> actualTransactionStore = new TransactionStore<>(transactionStore);
        assertNotNull(actualTransactionStore.getCatalog());
        assertFalse(actualTransactionStore.isReadOnly());
        assertFalse(actualTransactionStore.isClosed());
        assertTrue(actualTransactionStore.hasUnsavedChanges());
        assertNull(actualTransactionStore.getStoreConfig());
        assertNotNull(transactionStore.getCatalog());
        assertFalse(transactionStore.isReadOnly());
        assertFalse(transactionStore.isClosed());
        assertTrue(transactionStore.hasUnsavedChanges());
        assertNull(transactionStore.getStoreConfig());
    }
}

