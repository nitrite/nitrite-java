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
import org.dizitart.no2.store.StoreConfig;
import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TransactionalConfigTest {
    @Test
    public void testConstructor() {
        NitriteConfig config = new NitriteConfig();
        TransactionalStore<StoreConfig> transactionalStore = new TransactionalStore<>(
            new TransactionalStore<>(
                new TransactionalStore<>(new TransactionalStore<>(null))));
        TransactionalConfig actualTransactionalConfig = new TransactionalConfig(config, transactionalStore);
        assertTrue(actualTransactionalConfig.getMigrations().isEmpty());
        assertEquals(1, actualTransactionalConfig.getSchemaVersion().intValue());
        assertSame(transactionalStore, actualTransactionalConfig.getNitriteStore());
    }

    @Test
    public void testFindIndexer() {
        NitriteConfig config = new NitriteConfig();
        assertThrows(IndexingException.class,
            () -> (new TransactionalConfig(config,
                new TransactionalStore<>(
                    new TransactionalStore<>(new TransactionalStore<>(null)))))
                .findIndexer("Index Type"));
    }

    @Test
    public void testLoadModule() {
        NitriteConfig config = new NitriteConfig();
        TransactionalConfig transactionalConfig = new TransactionalConfig(config, new TransactionalStore<>(
            new TransactionalStore<>(new TransactionalStore<>(null))));
        NitriteModule nitriteModule = mock(NitriteModule.class);
        when(nitriteModule.plugins()).thenReturn(new HashSet<>());
        assertSame(transactionalConfig, transactionalConfig.loadModule(nitriteModule));
        verify(nitriteModule, times(2)).plugins();
    }

    @Test
    public void testNitriteMapper() {
        NitriteConfig config = new NitriteConfig();
        assertNull((new TransactionalConfig(config, new TransactionalStore<>(
            new TransactionalStore<>(new TransactionalStore<>(null))))).nitriteMapper());
    }

    @Test
    public void testGetNitriteStore() {
        NitriteConfig config = new NitriteConfig();
        TransactionalStore<StoreConfig> transactionalStore = new TransactionalStore<>(
            new TransactionalStore<>(new TransactionalStore<>(null)));
        assertSame(transactionalStore, (new TransactionalConfig(config, transactionalStore)).getNitriteStore());
    }
}

