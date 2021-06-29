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
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.index.UniqueIndexer;
import org.dizitart.no2.store.memory.InMemoryStore;
import org.junit.Test;

import java.util.ArrayList;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.mockito.Mockito.*;

public class DocumentIndexWriterTest {

    @Test
    public void testWriteIndexEntry() {
        ArrayList<IndexDescriptor> indexDescriptorList = new ArrayList<>();
        indexDescriptorList.add(new IndexDescriptor(IndexType.UNIQUE, Fields.withNames("a"), "Collection Name"));
        IndexOperations indexOperations = mock(IndexOperations.class);
        when(indexOperations.listIndexes()).thenReturn(indexDescriptorList);
        NitriteConfig nitriteConfig = mock(NitriteConfig.class);
        doReturn(new UniqueIndexer()).when(nitriteConfig).findIndexer(IndexType.UNIQUE);
        doReturn(new InMemoryStore()).when(nitriteConfig).getNitriteStore();

        (new DocumentIndexWriter(nitriteConfig, indexOperations)).writeIndexEntry(createDocument("a", 1));
        verify(indexOperations).listIndexes();
    }

    @Test
    public void testRemoveIndexEntry() {
        ArrayList<IndexDescriptor> indexDescriptorList = new ArrayList<>();
        indexDescriptorList.add(new IndexDescriptor(IndexType.UNIQUE, Fields.withNames("a"), "Collection Name"));
        IndexOperations indexOperations = mock(IndexOperations.class);
        when(indexOperations.listIndexes()).thenReturn(indexDescriptorList);
        NitriteConfig nitriteConfig = mock(NitriteConfig.class);
        doReturn(new UniqueIndexer()).when(nitriteConfig).findIndexer(IndexType.UNIQUE);
        doReturn(new InMemoryStore()).when(nitriteConfig).getNitriteStore();
        (new DocumentIndexWriter(nitriteConfig, indexOperations)).removeIndexEntry(createDocument("a", 1));
        verify(indexOperations).listIndexes();
    }

    @Test
    public void testUpdateIndexEntry() {
        ArrayList<IndexDescriptor> indexDescriptorList = new ArrayList<>();
        indexDescriptorList.add(new IndexDescriptor(IndexType.UNIQUE, Fields.withNames("a"), "Collection Name"));
        IndexOperations indexOperations = mock(IndexOperations.class);
        when(indexOperations.listIndexes()).thenReturn(indexDescriptorList);
        NitriteConfig nitriteConfig = mock(NitriteConfig.class);
        doReturn(new UniqueIndexer()).when(nitriteConfig).findIndexer(IndexType.UNIQUE);
        doReturn(new InMemoryStore()).when(nitriteConfig).getNitriteStore();

        (new DocumentIndexWriter(nitriteConfig, indexOperations)).updateIndexEntry(createDocument("a", 1), createDocument("a", 2));
        verify(indexOperations).listIndexes();
    }
}

