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

public class TransactionalConfigTest {
    @Test
    public void testConstructor() {
        TransactionalConfig actualTransactionalConfig = new TransactionalConfig(new NitriteConfig());
        assertTrue(actualTransactionalConfig.getMigrations().isEmpty());
        assertEquals(1, actualTransactionalConfig.getSchemaVersion().intValue());
        assertNull(actualTransactionalConfig.getNitriteStore());
    }

    @Test
    public void testFindIndexer() {
        assertThrows(IndexingException.class,
            () -> (new TransactionalConfig(new NitriteConfig())).findIndexer("Index Type"));
    }

    @Test
    public void testLoadModule() {
        TransactionalConfig transactionalConfig = new TransactionalConfig(new NitriteConfig());
        NitriteModule nitriteModule = mock(NitriteModule.class);
        when(nitriteModule.plugins()).thenReturn(new HashSet<>());
        assertSame(transactionalConfig, transactionalConfig.loadModule(nitriteModule));
        verify(nitriteModule, times(2)).plugins();
    }

    @Test
    public void testAutoConfigure() {
        TransactionalConfig transactionalConfig = new TransactionalConfig(new NitriteConfig());
        transactionalConfig.autoConfigure();
        NitriteStore<?> nitriteStore = transactionalConfig.getNitriteStore();
        assertTrue(nitriteStore instanceof org.dizitart.no2.store.memory.InMemoryStore);
        assertFalse(nitriteStore.isClosed());
        assertTrue(((InMemoryConfig) nitriteStore.getStoreConfig()).eventListeners().isEmpty());
    }

    @Test
    public void testNitriteMapper() {
        assertNull((new TransactionalConfig(new NitriteConfig())).nitriteMapper());
    }

    @Test
    public void testGetNitriteStore() {
        assertNull((new TransactionalConfig(new NitriteConfig())).getNitriteStore());
    }
}

