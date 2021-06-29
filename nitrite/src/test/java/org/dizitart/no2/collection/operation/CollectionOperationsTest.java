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
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.store.memory.InMemoryMap;
import org.dizitart.no2.store.memory.InMemoryStore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class CollectionOperationsTest {
    @Test
    public void testConstructor() {
        InMemoryMap<NitriteId, Document> nitriteMap = new InMemoryMap<>("Map Name", null);
        NitriteConfig nitriteConfig = mock(NitriteConfig.class);
        doReturn(new InMemoryStore()).when(nitriteConfig).getNitriteStore();
        assertEquals(0L, (new CollectionOperations("Collection Name", nitriteMap, nitriteConfig, null)).getSize());
        verify(nitriteConfig).getNitriteStore();
    }
}

