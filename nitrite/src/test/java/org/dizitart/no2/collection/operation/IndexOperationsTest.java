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
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.NitriteIndexer;
import org.dizitart.no2.store.memory.InMemoryMap;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class IndexOperationsTest {
    @Test
    public void testCreateIndex() {
        IndexManager indexManager = mock(IndexManager.class);
        when(indexManager.findExactIndexDescriptor(any()))
            .thenReturn(new IndexDescriptor("Index Type", new Fields(), "Collection Name"));
        NitriteConfig nitriteConfig = new NitriteConfig();
        IndexOperations indexOperations = new IndexOperations(nitriteConfig,
            new InMemoryMap<>("Map Name", null), null, indexManager);
        assertThrows(IndexingException.class, () -> indexOperations.createIndex(new Fields(), "Index Type"));
        verify(indexManager).findExactIndexDescriptor(any());
    }

    @Test
    public void testCreateIndex2() {
        IndexManager indexManager = mock(IndexManager.class);
        doNothing().when(indexManager).beginIndexing(any());
        doNothing().when(indexManager).endIndexing(any());
        when(indexManager.createIndexDescriptor(any(), anyString()))
            .thenReturn(new IndexDescriptor("Index Type", new Fields(), "Collection Name"));
        when(indexManager.findExactIndexDescriptor(any())).thenReturn(null);

        NitriteIndexer nitriteIndexer = mock(NitriteIndexer.class);
        NitriteConfig nitriteConfig = mock(NitriteConfig.class);
        when(nitriteConfig.findIndexer(anyString())).thenReturn(nitriteIndexer);

        IndexOperations indexOperations = new IndexOperations(nitriteConfig,
            new InMemoryMap<>("Map Name", null), null, indexManager);
        indexOperations.createIndex(new Fields(), "Index Type");
        verify(indexManager).endIndexing(any());
        verify(indexManager).createIndexDescriptor(any(), anyString());
        verify(indexManager).findExactIndexDescriptor(any());
        verify(indexManager).beginIndexing(any());
    }

    @Test
    public void testBuildIndex() {
        IndexManager indexManager = mock(IndexManager.class);
        doNothing().when(indexManager).beginIndexing(any());
        doNothing().when(indexManager).endIndexing(any());

        NitriteIndexer nitriteIndexer = mock(NitriteIndexer.class);
        NitriteConfig nitriteConfig = mock(NitriteConfig.class);
        when(nitriteConfig.findIndexer(anyString())).thenReturn(nitriteIndexer);

        IndexOperations indexOperations = new IndexOperations(nitriteConfig,
            new InMemoryMap<>("Map Name", null), null, indexManager);
        indexOperations.buildIndex(new IndexDescriptor("Index Type", new Fields(), "Collection Name"), true);
        verify(indexManager).endIndexing(any());
        verify(indexManager).beginIndexing(any());
    }

    @Test
    public void testDropIndex() {
        IndexManager indexManager = mock(IndexManager.class);
        when(indexManager.findExactIndexDescriptor(any()))
            .thenReturn(new IndexDescriptor("Index Type", new Fields(), "Collection Name"));

        NitriteIndexer nitriteIndexer = mock(NitriteIndexer.class);
        NitriteConfig nitriteConfig = mock(NitriteConfig.class);
        when(nitriteConfig.findIndexer(anyString())).thenReturn(nitriteIndexer);

        IndexOperations indexOperations = new IndexOperations(nitriteConfig,
            new InMemoryMap<>("Map Name", null), null, indexManager);
        indexOperations.dropIndex(new Fields());
        verify(indexManager).findExactIndexDescriptor(any());
    }

    @Test
    public void testDropIndex2() {
        IndexManager indexManager = mock(IndexManager.class);
        when(indexManager.findExactIndexDescriptor(any())).thenReturn(null);
        NitriteConfig nitriteConfig = new NitriteConfig();
        IndexOperations indexOperations = new IndexOperations(nitriteConfig,
            new InMemoryMap<>("Map Name", null), null, indexManager);
        assertThrows(IndexingException.class, () -> indexOperations.dropIndex(new Fields()));
        verify(indexManager).findExactIndexDescriptor(any());
    }

    @Test
    public void testDropIndex3() {
        IndexDescriptor indexDescriptor = mock(IndexDescriptor.class);
        when(indexDescriptor.getIndexType()).thenReturn("foo");
        IndexManager indexManager = mock(IndexManager.class);
        when(indexManager.findExactIndexDescriptor(any())).thenReturn(indexDescriptor);

        NitriteIndexer nitriteIndexer = mock(NitriteIndexer.class);
        NitriteConfig nitriteConfig = mock(NitriteConfig.class);
        when(nitriteConfig.findIndexer(anyString())).thenReturn(nitriteIndexer);

        IndexOperations indexOperations = new IndexOperations(nitriteConfig,
            new InMemoryMap<>("Map Name", null), null, indexManager);
        indexOperations.dropIndex(new Fields());
        verify(indexDescriptor).getIndexType();
        verify(indexManager).findExactIndexDescriptor(any());
    }

    @Test(expected = IndexingException.class)
    public void testDropAllIndices() {
        ArrayList<IndexDescriptor> indexDescriptorList = new ArrayList<>();
        indexDescriptorList.add(new IndexDescriptor("Index Type", Fields.withNames("a"), "Collection Name"));
        IndexManager indexManager = mock(IndexManager.class);
        when(indexManager.getIndexDescriptors()).thenReturn(indexDescriptorList);
        NitriteConfig nitriteConfig = new NitriteConfig();
        (new IndexOperations(nitriteConfig, new InMemoryMap<>("Map Name", null), null, indexManager))
            .dropAllIndices();
        verify(indexManager).getIndexDescriptors();
    }

    @Test(expected = IndexingException.class)
    public void testDropAllIndices2() {
        ArrayList<IndexDescriptor> indexDescriptorList = new ArrayList<>();
        indexDescriptorList.add(new IndexDescriptor("Index Type", Fields.withNames("a"), "Collection Name"));
        indexDescriptorList.add(new IndexDescriptor("Index Type", Fields.withNames("b"), "Collection Name"));
        IndexManager indexManager = mock(IndexManager.class);
        when(indexManager.getIndexDescriptors()).thenReturn(indexDescriptorList);
        NitriteConfig nitriteConfig = new NitriteConfig();
        (new IndexOperations(nitriteConfig, new InMemoryMap<>("Map Name", null), null, indexManager))
            .dropAllIndices();
        verify(indexManager).getIndexDescriptors();
    }

    @Test
    public void testIsIndexing() {
        IndexManager indexManager = mock(IndexManager.class);
        when(indexManager.hasIndexDescriptor(any())).thenReturn(true);
        NitriteConfig nitriteConfig = new NitriteConfig();
        IndexOperations indexOperations = new IndexOperations(nitriteConfig,
            new InMemoryMap<>("Map Name", null), null, indexManager);
        assertFalse(indexOperations.isIndexing(new Fields()));
        verify(indexManager).hasIndexDescriptor(any());
    }

    @Test
    public void testIsIndexing2() {
        IndexManager indexManager = mock(IndexManager.class);
        when(indexManager.hasIndexDescriptor(any())).thenReturn(false);
        NitriteConfig nitriteConfig = new NitriteConfig();
        IndexOperations indexOperations = new IndexOperations(nitriteConfig,
            new InMemoryMap<>("Map Name", null), null, indexManager);
        assertFalse(indexOperations.isIndexing(new Fields()));
        verify(indexManager).hasIndexDescriptor(any());
    }

    @Test
    public void testHasIndexEntry() {
        IndexManager indexManager = mock(IndexManager.class);
        when(indexManager.hasIndexDescriptor(any())).thenReturn(true);
        NitriteConfig nitriteConfig = new NitriteConfig();
        IndexOperations indexOperations = new IndexOperations(nitriteConfig,
            new InMemoryMap<>("Map Name", null), null, indexManager);
        assertTrue(indexOperations.hasIndexEntry(new Fields()));
        verify(indexManager).hasIndexDescriptor(any());
    }

    @Test
    public void testListIndexes() {
        IndexManager indexManager = mock(IndexManager.class);
        ArrayList<IndexDescriptor> indexDescriptorList = new ArrayList<>();
        when(indexManager.getIndexDescriptors()).thenReturn(indexDescriptorList);
        NitriteConfig nitriteConfig = new NitriteConfig();
        Collection<IndexDescriptor> actualListIndexesResult = (new IndexOperations(nitriteConfig,
            new InMemoryMap<>("Map Name", null), null, indexManager)).listIndexes();
        assertSame(indexDescriptorList, actualListIndexesResult);
        assertTrue(actualListIndexesResult.isEmpty());
        verify(indexManager).getIndexDescriptors();
    }

    @Test
    public void testFindIndexDescriptor() {
        IndexManager indexManager = mock(IndexManager.class);
        IndexDescriptor indexDescriptor = new IndexDescriptor("Index Type", new Fields(), "Collection Name");
        when(indexManager.findExactIndexDescriptor(any())).thenReturn(indexDescriptor);
        NitriteConfig nitriteConfig = new NitriteConfig();
        IndexOperations indexOperations = new IndexOperations(nitriteConfig,
            new InMemoryMap<>("Map Name", null), null, indexManager);
        assertSame(indexDescriptor, indexOperations.findIndexDescriptor(new Fields()));
        verify(indexManager).findExactIndexDescriptor(any());
    }

    @Test
    public void testGetBuildFlag() {
        // TODO: This test is incomplete.
        //   Reason: No meaningful assertions found.
        //   To help Diffblue Cover to find assertions, please add getters to the
        //   class under test that return fields written by the method under test.
        //   See https://diff.blue/R004

        NitriteConfig nitriteConfig = new NitriteConfig();
        IndexOperations indexOperations = new IndexOperations(nitriteConfig,
            new InMemoryMap<>("Map Name", null), null, null);
        indexOperations.getBuildFlag(new Fields());
    }

    @Test
    public void testShouldRebuildIndex() {
        IndexManager indexManager = mock(IndexManager.class);
        when(indexManager.isDirtyIndex(any())).thenReturn(true);
        NitriteConfig nitriteConfig = new NitriteConfig();
        IndexOperations indexOperations = new IndexOperations(nitriteConfig,
            new InMemoryMap<>("Map Name", null), null, indexManager);
        assertTrue(indexOperations.shouldRebuildIndex(new Fields()));
        verify(indexManager).isDirtyIndex(any());
    }

    @Test
    public void testShouldRebuildIndex2() {
        IndexManager indexManager = mock(IndexManager.class);
        when(indexManager.isDirtyIndex(any())).thenReturn(false);
        NitriteConfig nitriteConfig = new NitriteConfig();
        IndexOperations indexOperations = new IndexOperations(nitriteConfig,
            new InMemoryMap<>("Map Name", null), null, indexManager);
        assertFalse(indexOperations.shouldRebuildIndex(new Fields()));
        verify(indexManager).isDirtyIndex(any());
    }
}

