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

package org.dizitart.no2.common.module;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.store.NitriteStore;
import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PluginManagerTest {
    @Test
    public void testConstructor() {
        NitriteConfig nitriteConfig = new NitriteConfig();
        assertSame(nitriteConfig, (new PluginManager(nitriteConfig)).getNitriteConfig());
    }

    @Test
    public void testLoadModule() {
        PluginManager pluginManager = new PluginManager(new NitriteConfig());
        NitriteModule nitriteModule = mock(NitriteModule.class);
        when(nitriteModule.plugins()).thenReturn(new HashSet<>());
        pluginManager.loadModule(nitriteModule);
        verify(nitriteModule, times(2)).plugins();
    }

    @Test
    public void testLoadModule2() {
        PluginManager pluginManager = new PluginManager(new NitriteConfig());

        HashSet<NitritePlugin> nitritePluginSet = new HashSet<>();
        nitritePluginSet.add(mock(NitritePlugin.class));
        NitriteModule nitriteModule = mock(NitriteModule.class);
        when(nitriteModule.plugins()).thenReturn(nitritePluginSet);
        pluginManager.loadModule(nitriteModule);
        verify(nitriteModule, times(2)).plugins();
    }

    @Test
    public void testFindAndLoadPlugins() {
        PluginManager pluginManager = new PluginManager(new NitriteConfig());
        pluginManager.findAndLoadPlugins();
        NitriteStore<?> nitriteStore = pluginManager.getNitriteStore();
        assertTrue(nitriteStore instanceof org.dizitart.no2.store.memory.InMemoryStore);
        assertEquals(3, pluginManager.getIndexerMap().size());
        assertTrue(pluginManager.getNitriteMapper() instanceof org.dizitart.no2.common.mapper.MappableMapper);
        assertFalse(nitriteStore.isClosed());
    }
}

