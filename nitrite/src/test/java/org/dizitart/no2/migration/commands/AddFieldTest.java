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

package org.dizitart.no2.migration.commands;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.memory.InMemoryStore;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AddFieldTest {
    @Test
    public void testExecute() {
        AddField addField = new AddField("Collection Name", "Field Name", "Default Value");
        NitriteConfig nitriteConfig = mock(NitriteConfig.class);
        doReturn(new InMemoryStore()).when(nitriteConfig).getNitriteStore();
        Nitrite nitrite = mock(Nitrite.class);
        when(nitrite.getConfig()).thenReturn(nitriteConfig);
        InMemoryStore inMemoryStore = new InMemoryStore();
        doReturn(inMemoryStore).when(nitrite).getStore();
        addField.execute(nitrite);
        verify(nitrite).getConfig();
        verify(nitrite).getStore();
        verify(nitriteConfig).getNitriteStore();
        NitriteMap<NitriteId, Document> nitriteMap = addField.nitriteMap;
        assertTrue(nitriteMap instanceof org.dizitart.no2.store.memory.InMemoryMap);
        assertSame(inMemoryStore, addField.nitriteStore);
        assertNull(nitriteMap.getAttributes());
        assertTrue(nitriteMap.isEmpty());
        assertEquals("Collection Name", nitriteMap.getName());
        assertEquals(0L, addField.operations.getSize());
    }

    @Test
    public void testExecute2() {
        AddField addField = new AddField("Collection Name", "Field Name", "Default Value");
        NitriteConfig nitriteConfig = mock(NitriteConfig.class);
        doReturn(new InMemoryStore()).when(nitriteConfig).getNitriteStore();
        Nitrite nitrite = mock(Nitrite.class);
        when(nitrite.getConfig()).thenReturn(nitriteConfig);
        InMemoryStore inMemoryStore = new InMemoryStore();
        doReturn(inMemoryStore).when(nitrite).getStore();
        addField.execute(nitrite);
        verify(nitrite).getConfig();
        verify(nitrite).getStore();
        verify(nitriteConfig).getNitriteStore();
        NitriteMap<NitriteId, Document> nitriteMap = addField.nitriteMap;
        assertTrue(nitriteMap instanceof org.dizitart.no2.store.memory.InMemoryMap);
        assertSame(inMemoryStore, addField.nitriteStore);
        assertNull(nitriteMap.getAttributes());
        assertTrue(nitriteMap.isEmpty());
        assertEquals("Collection Name", nitriteMap.getName());
        assertEquals(0L, addField.operations.getSize());
    }
}

