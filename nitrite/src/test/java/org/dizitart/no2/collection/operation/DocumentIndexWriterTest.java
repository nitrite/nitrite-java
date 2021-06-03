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
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.NitriteIndexer;
import org.dizitart.no2.store.memory.InMemoryMap;
import org.junit.Test;

import java.util.ArrayList;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.mockito.Mockito.*;

public class DocumentIndexWriterTest {
    @Test
    public void testWriteIndexEntry() {
        ArrayList<IndexDescriptor> indexDescriptorList = new ArrayList<>();
        indexDescriptorList.add(new IndexDescriptor("Index Type", new Fields(), "Collection Name"));
        IndexManager indexManager = mock(IndexManager.class);
        when(indexManager.getIndexDescriptors()).thenReturn(indexDescriptorList);

        NitriteIndexer nitriteIndexer = mock(NitriteIndexer.class);
        NitriteConfig nitriteConfig = mock(NitriteConfig.class);
        when(nitriteConfig.findIndexer(anyString())).thenReturn(nitriteIndexer);
        doNothing().when(nitriteIndexer).writeIndexEntry(any(), any(), any());

        IndexOperations indexOperations = new IndexOperations(nitriteConfig,
            new InMemoryMap<>("Map Name", null), null, indexManager);
        (new DocumentIndexWriter(nitriteConfig, indexOperations)).writeIndexEntry(createDocument("a", "b"));
        verify(indexManager).getIndexDescriptors();
    }

    @Test
    public void testRemoveIndexEntry() {
        ArrayList<IndexDescriptor> indexDescriptorList = new ArrayList<>();
        indexDescriptorList.add(new IndexDescriptor("Index Type", new Fields(), "Collection Name"));
        IndexManager indexManager = mock(IndexManager.class);
        when(indexManager.getIndexDescriptors()).thenReturn(indexDescriptorList);

        NitriteIndexer nitriteIndexer = mock(NitriteIndexer.class);
        NitriteConfig nitriteConfig = mock(NitriteConfig.class);
        when(nitriteConfig.findIndexer(anyString())).thenReturn(nitriteIndexer);
        doNothing().when(nitriteIndexer).removeIndexEntry(any(), any(), any());

        IndexOperations indexOperations = new IndexOperations(nitriteConfig,
            new InMemoryMap<>("Map Name", null), null, indexManager);
        (new DocumentIndexWriter(nitriteConfig, indexOperations)).removeIndexEntry(createDocument("a", "b"));
        verify(indexManager).getIndexDescriptors();
    }

    @Test
    public void testUpdateIndexEntry() {
        ArrayList<IndexDescriptor> indexDescriptorList = new ArrayList<>();
        indexDescriptorList.add(new IndexDescriptor("Index Type", new Fields(), "Collection Name"));
        IndexManager indexManager = mock(IndexManager.class);
        when(indexManager.getIndexDescriptors()).thenReturn(indexDescriptorList);

        NitriteIndexer nitriteIndexer = mock(NitriteIndexer.class);
        NitriteConfig nitriteConfig = mock(NitriteConfig.class);
        when(nitriteConfig.findIndexer(anyString())).thenReturn(nitriteIndexer);
        doNothing().when(nitriteIndexer).removeIndexEntry(any(), any(), any());

        IndexOperations indexOperations = new IndexOperations(nitriteConfig,
            new InMemoryMap<>("Map Name", null), null, indexManager);
        (new DocumentIndexWriter(nitriteConfig, indexOperations)).updateIndexEntry(createDocument("x", "y"),
            createDocument("a", "b"));
        verify(indexManager).getIndexDescriptors();
    }
}

