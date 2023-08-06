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

package org.dizitart.no2.repository;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.*;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.common.meta.Attributes;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.common.processors.ProcessorChain;
import org.dizitart.no2.common.streams.DocumentStream;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.store.memory.InMemoryStore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DefaultObjectRepositoryTest {
    @Test
    public void testAddProcessor() {
        NitriteCollection nitriteCollection = mock(NitriteCollection.class);
        doNothing().when(nitriteCollection).addProcessor(any());
        Class<Object> type = Object.class;
        DefaultObjectRepository<Object> defaultObjectRepository = new DefaultObjectRepository<>(type,
            nitriteCollection, new NitriteConfig());
        defaultObjectRepository.addProcessor(new ProcessorChain());
        verify(nitriteCollection).addProcessor(any());
        assertNull(defaultObjectRepository.getAttributes());
    }

    @Test
    public void testCreateIndex() {
        NitriteCollection nitriteCollection = mock(NitriteCollection.class);
        doNothing().when(nitriteCollection).createIndex(any(IndexOptions.class), any(String[].class));
        Class<Object> type = Object.class;
        DefaultObjectRepository<Object> defaultObjectRepository = new DefaultObjectRepository<>(type,
            nitriteCollection, new NitriteConfig());
        defaultObjectRepository.createIndex(IndexOptions.indexOptions("Index Type"), "foo", "foo", "foo");
        verify(nitriteCollection).createIndex(any(IndexOptions.class), any(String[].class));
        assertNull(defaultObjectRepository.getAttributes());
    }

    @Test
    public void testRebuildIndex() {
        NitriteCollection nitriteCollection = mock(NitriteCollection.class);
        doNothing().when(nitriteCollection).rebuildIndex(any(String[].class));
        Class<Object> type = Object.class;
        DefaultObjectRepository<Object> defaultObjectRepository = new DefaultObjectRepository<>(type,
            nitriteCollection, new NitriteConfig());
        defaultObjectRepository.rebuildIndex("foo", "foo", "foo");
        verify(nitriteCollection).rebuildIndex(any(String[].class));
        assertNull(defaultObjectRepository.getAttributes());
    }

    @Test
    public void testListIndices() {
        NitriteCollection nitriteCollection = mock(NitriteCollection.class);
        ArrayList<IndexDescriptor> indexDescriptorList = new ArrayList<>();
        when(nitriteCollection.listIndices()).thenReturn(indexDescriptorList);
        Class<Object> type = Object.class;
        DefaultObjectRepository<Object> defaultObjectRepository = new DefaultObjectRepository<>(type,
            nitriteCollection, new NitriteConfig());
        Collection<IndexDescriptor> actualListIndicesResult = defaultObjectRepository.listIndices();
        assertSame(indexDescriptorList, actualListIndicesResult);
        assertTrue(actualListIndicesResult.isEmpty());
        verify(nitriteCollection).listIndices();
        assertNull(defaultObjectRepository.getAttributes());
    }

    @Test
    public void testHasIndex() {
        NitriteCollection nitriteCollection = mock(NitriteCollection.class);
        when(nitriteCollection.hasIndex(any(String[].class))).thenReturn(true);
        Class<Object> type = Object.class;
        DefaultObjectRepository<Object> defaultObjectRepository = new DefaultObjectRepository<>(type,
            nitriteCollection, new NitriteConfig());
        assertTrue(defaultObjectRepository.hasIndex("foo", "foo", "foo"));
        verify(nitriteCollection).hasIndex(any(String[].class));
        assertNull(defaultObjectRepository.getAttributes());
    }

    @Test
    public void testIsIndexing() {
        NitriteCollection nitriteCollection = mock(NitriteCollection.class);
        when(nitriteCollection.isIndexing(any(String[].class))).thenReturn(true);
        Class<Object> type = Object.class;
        DefaultObjectRepository<Object> defaultObjectRepository = new DefaultObjectRepository<>(type,
            nitriteCollection, new NitriteConfig());
        assertTrue(defaultObjectRepository.isIndexing("foo", "foo", "foo"));
        verify(nitriteCollection).isIndexing(any(String[].class));
        assertNull(defaultObjectRepository.getAttributes());
    }

    @Test
    public void testDropIndex() {
        NitriteCollection nitriteCollection = mock(NitriteCollection.class);
        doNothing().when(nitriteCollection).dropIndex(any(String[].class));
        Class<Object> type = Object.class;
        DefaultObjectRepository<Object> defaultObjectRepository = new DefaultObjectRepository<>(type,
            nitriteCollection, new NitriteConfig());
        defaultObjectRepository.dropIndex("foo", "foo", "foo");
        verify(nitriteCollection).dropIndex(any(String[].class));
        assertNull(defaultObjectRepository.getAttributes());
    }

    @Test
    public void testDropAllIndices() {
        NitriteCollection nitriteCollection = mock(NitriteCollection.class);
        doNothing().when(nitriteCollection).dropAllIndices();
        Class<Object> type = Object.class;
        DefaultObjectRepository<Object> defaultObjectRepository = new DefaultObjectRepository<>(type,
            nitriteCollection, new NitriteConfig());
        defaultObjectRepository.dropAllIndices();
        verify(nitriteCollection).dropAllIndices();
        assertNull(defaultObjectRepository.getAttributes());
    }

    @Test
    public void testInsert() {
        NitriteCollection nitriteCollection = mock(NitriteCollection.class);
        when(nitriteCollection.insert(any())).thenReturn(mock(WriteResult.class));
        Class<Object> type = Object.class;
        DefaultObjectRepository<Object> defaultObjectRepository = new DefaultObjectRepository<>(type,
            nitriteCollection, new NitriteConfig());
        defaultObjectRepository.insert(new Object[]{});
        verify(nitriteCollection).insert(any());
        assertNull(defaultObjectRepository.getAttributes());
    }

    @Test
    public void testUpdate2() {
        DefaultObjectRepository<Object> defaultObjectRepository = (DefaultObjectRepository<Object>) mock(
            DefaultObjectRepository.class);
        when(defaultObjectRepository.update(any(), (Object) any(), any())).thenReturn(null);
        defaultObjectRepository.update(mock(Filter.class), "Update", UpdateOptions.updateOptions(true));
        verify(defaultObjectRepository).update(any(), (Object) any(), any());
    }

    @Test
    public void testRemove2() {
        NitriteCollection nitriteCollection = mock(NitriteCollection.class);
        when(nitriteCollection.remove(any(), anyBoolean())).thenReturn(mock(WriteResult.class));
        Class<Object> type = Object.class;
        DefaultObjectRepository<Object> defaultObjectRepository = new DefaultObjectRepository<>(type,
            nitriteCollection, new NitriteConfig());
        defaultObjectRepository.remove(mock(Filter.class), true);
        verify(nitriteCollection).remove(any(), anyBoolean());
        assertNull(defaultObjectRepository.getAttributes());
    }

    @Test
    public void testClear() {
        NitriteCollection nitriteCollection = mock(NitriteCollection.class);
        doNothing().when(nitriteCollection).clear();
        Class<Object> type = Object.class;
        DefaultObjectRepository<Object> defaultObjectRepository = new DefaultObjectRepository<>(type,
            nitriteCollection, new NitriteConfig());
        defaultObjectRepository.clear();
        verify(nitriteCollection).clear();
        assertNull(defaultObjectRepository.getAttributes());
    }

    @Test
    public void testFind() {
        NitriteCollection nitriteCollection = mock(NitriteCollection.class);
        RecordStream<Pair<NitriteId, Document>> recordStream = (RecordStream<Pair<NitriteId, Document>>) mock(
            RecordStream.class);
        when(nitriteCollection.find(any(), any()))
            .thenReturn(new DocumentStream(recordStream, new ProcessorChain()));
        Class<Object> type = Object.class;
        DefaultObjectRepository<Object> defaultObjectRepository = new DefaultObjectRepository<>(type,
            nitriteCollection, new NitriteConfig());
        Filter filter = mock(Filter.class);
        assertNull(defaultObjectRepository.find(filter, new FindOptions()).getFindPlan());
        verify(nitriteCollection).find(any(), any());
        assertNull(defaultObjectRepository.getAttributes());
    }

    @Test
    public void testDrop() {
        NitriteCollection nitriteCollection = mock(NitriteCollection.class);
        doNothing().when(nitriteCollection).drop();
        Class<Object> type = Object.class;
        DefaultObjectRepository<Object> defaultObjectRepository = new DefaultObjectRepository<>(type,
            nitriteCollection, new NitriteConfig());
        defaultObjectRepository.drop();
        verify(nitriteCollection).drop();
        assertNull(defaultObjectRepository.getAttributes());
    }

    @Test
    public void testIsDropped() {
        NitriteCollection nitriteCollection = mock(NitriteCollection.class);
        when(nitriteCollection.isDropped()).thenReturn(true);
        Class<Object> type = Object.class;
        DefaultObjectRepository<Object> defaultObjectRepository = new DefaultObjectRepository<>(type,
            nitriteCollection, new NitriteConfig());
        assertTrue(defaultObjectRepository.isDropped());
        verify(nitriteCollection).isDropped();
        assertNull(defaultObjectRepository.getAttributes());
    }

    @Test
    public void testIsOpen() {
        NitriteCollection nitriteCollection = mock(NitriteCollection.class);
        when(nitriteCollection.isOpen()).thenReturn(true);
        Class<Object> type = Object.class;
        DefaultObjectRepository<Object> defaultObjectRepository = new DefaultObjectRepository<>(type,
            nitriteCollection, new NitriteConfig());
        assertTrue(defaultObjectRepository.isOpen());
        verify(nitriteCollection).isOpen();
        assertNull(defaultObjectRepository.getAttributes());
    }

    @Test
    public void testClose() throws Exception {
        NitriteCollection nitriteCollection = mock(NitriteCollection.class);
        doNothing().when(nitriteCollection).close();
        Class<Object> type = Object.class;
        DefaultObjectRepository<Object> defaultObjectRepository = new DefaultObjectRepository<>(type,
            nitriteCollection, new NitriteConfig());
        defaultObjectRepository.close();
        verify(nitriteCollection).close();
        assertNull(defaultObjectRepository.getAttributes());
    }

    @Test
    public void testSize() {
        NitriteCollection nitriteCollection = mock(NitriteCollection.class);
        when(nitriteCollection.size()).thenReturn(1L);
        Class<Object> type = Object.class;
        DefaultObjectRepository<Object> defaultObjectRepository = new DefaultObjectRepository<>(type,
            nitriteCollection, new NitriteConfig());
        assertEquals(1L, defaultObjectRepository.size());
        verify(nitriteCollection).size();
        assertNull(defaultObjectRepository.getAttributes());
    }

    @Test
    public void testGetStore() {
        NitriteCollection nitriteCollection = mock(NitriteCollection.class);
        InMemoryStore inMemoryStore = new InMemoryStore();
        doReturn(inMemoryStore).when(nitriteCollection).getStore();
        Class<Object> type = Object.class;
        DefaultObjectRepository<Object> defaultObjectRepository = new DefaultObjectRepository<>(type,
            nitriteCollection, new NitriteConfig());
        assertSame(inMemoryStore, defaultObjectRepository.getStore());
        verify(nitriteCollection).getStore();
        assertNull(defaultObjectRepository.getAttributes());
    }

    @Test
    public void testSubscribe() {
        NitriteCollection nitriteCollection = mock(NitriteCollection.class);
        doNothing().when(nitriteCollection).subscribe(any());
        Class<Object> type = Object.class;
        DefaultObjectRepository<Object> defaultObjectRepository = new DefaultObjectRepository<>(type,
            nitriteCollection, new NitriteConfig());
        defaultObjectRepository.subscribe(mock(CollectionEventListener.class));
        verify(nitriteCollection).subscribe(any());
        assertNull(defaultObjectRepository.getAttributes());
    }

    @Test
    public void testUnsubscribe() {
        NitriteCollection nitriteCollection = mock(NitriteCollection.class);
        doNothing().when(nitriteCollection).unsubscribe(any());
        Class<Object> type = Object.class;
        DefaultObjectRepository<Object> defaultObjectRepository = new DefaultObjectRepository<>(type,
            nitriteCollection, new NitriteConfig());
        defaultObjectRepository.unsubscribe(mock(CollectionEventListener.class));
        verify(nitriteCollection).unsubscribe(any());
        assertNull(defaultObjectRepository.getAttributes());
    }

    @Test
    public void testGetAttributes() {
        NitriteCollection nitriteCollection = mock(NitriteCollection.class);
        Attributes attributes = new Attributes();
        when(nitriteCollection.getAttributes()).thenReturn(attributes);
        Class<Object> type = Object.class;
        DefaultObjectRepository<Object> defaultObjectRepository = new DefaultObjectRepository<>(type,
            nitriteCollection, new NitriteConfig());
        assertSame(attributes, defaultObjectRepository.getAttributes());
        verify(nitriteCollection).getAttributes();
        assertNull(defaultObjectRepository.getStore());
    }

    @Test
    public void testSetAttributes() {
        NitriteCollection nitriteCollection = mock(NitriteCollection.class);
        doNothing().when(nitriteCollection).setAttributes(any());
        Class<Object> type = Object.class;
        DefaultObjectRepository<Object> defaultObjectRepository = new DefaultObjectRepository<>(type,
            nitriteCollection, new NitriteConfig());
        defaultObjectRepository.setAttributes(new Attributes());
        verify(nitriteCollection).setAttributes(any());
        assertNull(defaultObjectRepository.getAttributes());
    }

    @Test(expected = ValidationException.class)
    public void testConstructor() {
        Class<Object> type = Object.class;
        NitriteConfig nitriteConfig = new NitriteConfig();
        new DefaultObjectRepository<>(type, null, nitriteConfig);
        assertTrue(nitriteConfig.getMigrations().isEmpty());
        assertEquals(1, nitriteConfig.getSchemaVersion().intValue());
        assertNull(nitriteConfig.getNitriteStore());
    }

    @Test
    public void testConstructor2() {
        Class<Object> forNameResult = Object.class;
        NitriteCollection collection = mock(NitriteCollection.class);
        DefaultObjectRepository<Object> actualDefaultObjectRepository = new DefaultObjectRepository<>(forNameResult,
            collection, new NitriteConfig());
        assertNull(actualDefaultObjectRepository.getAttributes());
        assertSame(forNameResult, actualDefaultObjectRepository.getType());
    }
}

