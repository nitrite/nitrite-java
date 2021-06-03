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

package org.dizitart.no2.collection.operation;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.common.processors.ProcessorChain;
import org.dizitart.no2.store.memory.InMemoryMap;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class ReadOperationsTest {
    @Test
    public void testFind() {
        IndexManager indexManager = mock(IndexManager.class);
        when(indexManager.getIndexDescriptors()).thenReturn(new ArrayList<>());
        NitriteConfig nitriteConfig = new NitriteConfig();
        IndexOperations indexOperations = new IndexOperations(nitriteConfig,
            new InMemoryMap<>("Map Name", null), null, indexManager);
        NitriteConfig nitriteConfig1 = new NitriteConfig();
        InMemoryMap<NitriteId, Document> nitriteMap = new InMemoryMap<>("Map Name", null);
        ReadOperations readOperations = new ReadOperations("Collection Name", indexOperations, nitriteConfig1, nitriteMap,
            new ProcessorChain());
        Filter filter = mock(Filter.class);
        assertTrue(readOperations.find(filter, new FindOptions()).toList().isEmpty());
        verify(indexManager).getIndexDescriptors();
    }

    @Test
    public void testGetById() {
        NitriteConfig nitriteConfig = new NitriteConfig();
        IndexOperations indexOperations = new IndexOperations(nitriteConfig,
            new InMemoryMap<>("Map Name", null), null, null);
        NitriteConfig nitriteConfig1 = new NitriteConfig();
        InMemoryMap<NitriteId, Document> nitriteMap = new InMemoryMap<>("Map Name", null);
        ReadOperations readOperations = new ReadOperations("Collection Name", indexOperations, nitriteConfig1, nitriteMap,
            new ProcessorChain());
        assertNull(readOperations.getById(NitriteId.newId()));
    }
}

