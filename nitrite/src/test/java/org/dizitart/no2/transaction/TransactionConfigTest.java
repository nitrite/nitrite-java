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
import org.dizitart.no2.common.module.NitriteModule;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.store.memory.InMemoryConfig;
import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TransactionConfigTest {
    @Test
    public void testConstructor() {
        TransactionConfig actualTransactionConfig = new TransactionConfig(new NitriteConfig());
        assertTrue(actualTransactionConfig.getMigrations().isEmpty());
        assertEquals(1, actualTransactionConfig.getSchemaVersion().intValue());
        assertNull(actualTransactionConfig.getNitriteStore());
    }

    @Test
    public void testFindIndexer() {
        assertThrows(IndexingException.class,
            () -> (new TransactionConfig(new NitriteConfig())).findIndexer("Index Type"));
    }

    @Test
    public void testLoadModule() {
        TransactionConfig transactionConfig = new TransactionConfig(new NitriteConfig());
        NitriteModule nitriteModule = mock(NitriteModule.class);
        when(nitriteModule.plugins()).thenReturn(new HashSet<>());
        assertSame(transactionConfig, transactionConfig.loadModule(nitriteModule));
        verify(nitriteModule, times(2)).plugins();
    }

    @Test
    public void testAutoConfigure() {
        TransactionConfig transactionConfig = new TransactionConfig(new NitriteConfig());
        transactionConfig.autoConfigure();
        NitriteStore<?> nitriteStore = transactionConfig.getNitriteStore();
        assertTrue(nitriteStore instanceof org.dizitart.no2.store.memory.InMemoryStore);
        assertFalse(nitriteStore.isClosed());
        assertTrue(((InMemoryConfig) nitriteStore.getStoreConfig()).eventListeners().isEmpty());
    }

    @Test
    public void testNitriteMapper() {
        assertNull((new TransactionConfig(new NitriteConfig())).nitriteMapper());
    }

    @Test
    public void testGetNitriteStore() {
        assertNull((new TransactionConfig(new NitriteConfig())).getNitriteStore());
    }
}

