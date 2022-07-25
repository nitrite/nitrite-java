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

package org.dizitart.no2;

import org.dizitart.no2.common.module.NitriteModule;
import org.dizitart.no2.common.module.NitritePlugin;
import org.dizitart.no2.common.module.PluginManager;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.migration.Migration;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.store.memory.InMemoryConfig;
import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class NitriteConfigTest {
    @Test
    public void testConstructor() {
        NitriteConfig actualNitriteConfig = new NitriteConfig();
        assertTrue(actualNitriteConfig.getMigrations().isEmpty());
        assertFalse(actualNitriteConfig.configured);
        assertEquals(1, actualNitriteConfig.getSchemaVersion().intValue());
        assertNull(actualNitriteConfig.getNitriteStore());
        PluginManager pluginManager = actualNitriteConfig.getPluginManager();
        assertSame(actualNitriteConfig, pluginManager.getNitriteConfig());
        assertTrue(pluginManager.getIndexerMap().isEmpty());
    }

    @Test
    public void testFieldSeparator() {
        (new NitriteConfig()).fieldSeparator("Separator");
    }

    @Test
    public void testLoadModule() {
        NitriteConfig nitriteConfig = new NitriteConfig();
        NitriteModule nitriteModule = mock(NitriteModule.class);
        when(nitriteModule.plugins()).thenReturn(new HashSet<NitritePlugin>());
        assertSame(nitriteConfig, nitriteConfig.loadModule(nitriteModule));
        verify(nitriteModule, times(2)).plugins();
    }

    @Test
    public void testAddMigration() {
        NitriteConfig nitriteConfig = new NitriteConfig();
        assertSame(nitriteConfig, nitriteConfig.addMigration(null));
    }

    @Test
    public void testAddMigration2() {
        NitriteConfig nitriteConfig = new NitriteConfig();
        Migration migration = mock(Migration.class);
        when(migration.getToVersion()).thenReturn(1);
        when(migration.getFromVersion()).thenReturn(1);
        assertSame(nitriteConfig, nitriteConfig.addMigration(migration));
        verify(migration).getToVersion();
        verify(migration).getFromVersion();
    }

    @Test
    public void testAddMigration3() {
        NitriteConfig nitriteConfig = new NitriteConfig();
        Migration migration = mock(Migration.class);
        when(migration.getToVersion()).thenReturn(4);
        when(migration.getFromVersion()).thenReturn(1);
        assertSame(nitriteConfig, nitriteConfig.addMigration(migration));
        verify(migration).getToVersion();
        verify(migration).getFromVersion();
    }

    @Test
    public void testAddMigration4() {
        NitriteConfig nitriteConfig = new NitriteConfig();
        Migration migration = mock(Migration.class);
        when(migration.getToVersion()).thenReturn(1);
        when(migration.getFromVersion()).thenReturn(4);
        assertSame(nitriteConfig, nitriteConfig.addMigration(migration));
        verify(migration).getToVersion();
        verify(migration).getFromVersion();
    }

    @Test
    public void testSchemaVersion() {
        NitriteConfig nitriteConfig = new NitriteConfig();
        NitriteConfig actualSchemaVersionResult = nitriteConfig.currentSchemaVersion(1);
        assertSame(nitriteConfig, actualSchemaVersionResult);
        assertEquals(1, actualSchemaVersionResult.getSchemaVersion().intValue());
    }

    @Test
    public void testAutoConfigure() {
        NitriteConfig nitriteConfig = new NitriteConfig();
        nitriteConfig.autoConfigure();
        PluginManager pluginManager = nitriteConfig.getPluginManager();
        assertEquals(3, pluginManager.getIndexerMap().size());
        NitriteStore<?> nitriteStore = nitriteConfig.getNitriteStore();
        assertSame(nitriteStore, pluginManager.getNitriteStore());
        assertTrue(pluginManager.getNitriteMapper() instanceof org.dizitart.no2.common.mapper.MappableMapper);
        assertFalse(nitriteStore.isClosed());
        assertTrue(((InMemoryConfig) nitriteStore.getStoreConfig()).eventListeners().isEmpty());
    }

    @Test
    public void testFindIndexer() {
        assertThrows(IndexingException.class, () -> (new NitriteConfig()).findIndexer("Index Type"));
    }

    @Test
    public void testNitriteMapper() {
        assertNull((new NitriteConfig()).nitriteMapper());
    }

    @Test
    public void testGetNitriteStore() {
        assertNull((new NitriteConfig()).getNitriteStore());
    }

    @Test(expected = NitriteIOException.class)
    public void testInitialize() {
        NitriteConfig nitriteConfig = new NitriteConfig();
        nitriteConfig.initialize();
        assertTrue(nitriteConfig.configured);
    }
}

