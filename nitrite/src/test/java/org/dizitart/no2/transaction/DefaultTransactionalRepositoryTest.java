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

import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.common.meta.Attributes;
import org.dizitart.no2.common.processors.ProcessorChain;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexOptions;
import org.junit.Test;

import java.util.ArrayList;

import static org.dizitart.no2.collection.UpdateOptions.updateOptions;
import static org.mockito.Mockito.*;

@SuppressWarnings("all")
public class DefaultTransactionalRepositoryTest {
    @Test
    public void testAddProcessor() {
        DefaultTransactionalRepository<Object> defaultTransactionalRepository = (DefaultTransactionalRepository<Object>) mock(
            DefaultTransactionalRepository.class);
        doNothing().when(defaultTransactionalRepository).addProcessor(any());
        defaultTransactionalRepository.addProcessor(new ProcessorChain());
        verify(defaultTransactionalRepository).addProcessor(any());
    }

    @Test
    public void testCreateIndex() {
        DefaultTransactionalRepository<Object> defaultTransactionalRepository = (DefaultTransactionalRepository<Object>) mock(
            DefaultTransactionalRepository.class);
        doNothing().when(defaultTransactionalRepository).createIndex(any(IndexOptions.class), any(String[].class));
        defaultTransactionalRepository.createIndex(IndexOptions.indexOptions("Index Type"), "foo", "foo", "foo");
        verify(defaultTransactionalRepository).createIndex(any(IndexOptions.class), any(String[].class));
    }

    @Test
    public void testRebuildIndex() {
        DefaultTransactionalRepository<Object> defaultTransactionalRepository = (DefaultTransactionalRepository<Object>) mock(
            DefaultTransactionalRepository.class);
        doNothing().when(defaultTransactionalRepository).rebuildIndex(any(String[].class));
        defaultTransactionalRepository.rebuildIndex("foo", "foo", "foo");
        verify(defaultTransactionalRepository).rebuildIndex(any(String[].class));
    }

    @Test
    public void testListIndices() {
        DefaultTransactionalRepository<Object> defaultTransactionalRepository = (DefaultTransactionalRepository<Object>) mock(
            DefaultTransactionalRepository.class);
        when(defaultTransactionalRepository.listIndices()).thenReturn(new ArrayList<>());
        defaultTransactionalRepository.listIndices();
        verify(defaultTransactionalRepository).listIndices();
    }

    @Test
    public void testHasIndex() {
        DefaultTransactionalRepository<Object> defaultTransactionalRepository = (DefaultTransactionalRepository<Object>) mock(
            DefaultTransactionalRepository.class);
        when(defaultTransactionalRepository.hasIndex(any(String[].class))).thenReturn(true);
        defaultTransactionalRepository.hasIndex("foo", "foo", "foo");
        verify(defaultTransactionalRepository).hasIndex(any(String[].class));
    }

    @Test
    public void testIsIndexing() {
        DefaultTransactionalRepository<Object> defaultTransactionalRepository = (DefaultTransactionalRepository<Object>) mock(
            DefaultTransactionalRepository.class);
        when(defaultTransactionalRepository.isIndexing(any(String[].class))).thenReturn(true);
        defaultTransactionalRepository.isIndexing("foo", "foo", "foo");
        verify(defaultTransactionalRepository).isIndexing(any(String[].class));
    }

    @Test
    public void testDropIndex() {
        DefaultTransactionalRepository<Object> defaultTransactionalRepository = (DefaultTransactionalRepository<Object>) mock(
            DefaultTransactionalRepository.class);
        doNothing().when(defaultTransactionalRepository).dropIndex(any(String[].class));
        defaultTransactionalRepository.dropIndex("foo", "foo", "foo");
        verify(defaultTransactionalRepository).dropIndex(any(String[].class));
    }

    @Test
    public void testDropAllIndices() {
        DefaultTransactionalRepository<Object> defaultTransactionalRepository = (DefaultTransactionalRepository<Object>) mock(
            DefaultTransactionalRepository.class);
        doNothing().when(defaultTransactionalRepository).dropAllIndices();
        defaultTransactionalRepository.dropAllIndices();
        verify(defaultTransactionalRepository).dropAllIndices();
    }

    @Test
    public void testInsert() {
        DefaultTransactionalRepository<Object> defaultTransactionalRepository = (DefaultTransactionalRepository<Object>) mock(
            DefaultTransactionalRepository.class);
        when(defaultTransactionalRepository.insert(any())).thenReturn(null);
        defaultTransactionalRepository.insert(new Object[]{"42", "42", "42"});
        verify(defaultTransactionalRepository).insert(any());
    }

    @Test
    public void testUpdate() {
        DefaultTransactionalRepository<Object> defaultTransactionalRepository = (DefaultTransactionalRepository<Object>) mock(
            DefaultTransactionalRepository.class);
        when(defaultTransactionalRepository.update(any(), anyBoolean())).thenReturn(null);
        defaultTransactionalRepository.update("Element", true);
        verify(defaultTransactionalRepository).update(any(Object.class), anyBoolean());
    }

    @Test
    public void testUpdate2() {
        DefaultTransactionalRepository<Object> defaultTransactionalRepository = (DefaultTransactionalRepository<Object>) mock(
            DefaultTransactionalRepository.class);
        when(defaultTransactionalRepository.update(any(), (Object) any(), any())).thenReturn(null);
        defaultTransactionalRepository.update(mock(Filter.class), "Update", updateOptions(true));
        verify(defaultTransactionalRepository).update(any(), (Object) any(), any());
    }

    @Test
    public void testUpdate3() {
        DefaultTransactionalRepository<Object> defaultTransactionalRepository = (DefaultTransactionalRepository<Object>) mock(
            DefaultTransactionalRepository.class);
        when(defaultTransactionalRepository.update(any(), any(), anyBoolean())).thenReturn(null);
        defaultTransactionalRepository.update(mock(Filter.class), null, true);
        verify(defaultTransactionalRepository).update(any(), any(), anyBoolean());
    }

    @Test
    public void testRemove() {
        DefaultTransactionalRepository<Object> defaultTransactionalRepository = (DefaultTransactionalRepository<Object>) mock(
            DefaultTransactionalRepository.class);
        when(defaultTransactionalRepository.remove((Object) any())).thenReturn(null);
        defaultTransactionalRepository.remove("Element");
        verify(defaultTransactionalRepository).remove((Object) any());
    }

    @Test
    public void testRemove2() {
        DefaultTransactionalRepository<Object> defaultTransactionalRepository = (DefaultTransactionalRepository<Object>) mock(
            DefaultTransactionalRepository.class);
        when(defaultTransactionalRepository.remove(any(), anyBoolean())).thenReturn(null);
        defaultTransactionalRepository.remove(mock(Filter.class), true);
        verify(defaultTransactionalRepository).remove(any(), anyBoolean());
    }

    @Test
    public void testClear() {
        DefaultTransactionalRepository<Object> defaultTransactionalRepository = (DefaultTransactionalRepository<Object>) mock(
            DefaultTransactionalRepository.class);
        doNothing().when(defaultTransactionalRepository).clear();
        defaultTransactionalRepository.clear();
        verify(defaultTransactionalRepository).clear();
    }

    @Test
    public void testFind() {
        DefaultTransactionalRepository<Object> defaultTransactionalRepository = (DefaultTransactionalRepository<Object>) mock(
            DefaultTransactionalRepository.class);
        when(defaultTransactionalRepository.find(any(), any())).thenReturn(null);
        Filter filter = mock(Filter.class);
        defaultTransactionalRepository.find(filter, new FindOptions());
        verify(defaultTransactionalRepository).find(any(), any());
    }

    @Test
    public void testGetById() {
        DefaultTransactionalRepository<Object> defaultTransactionalRepository = (DefaultTransactionalRepository<Object>) mock(
            DefaultTransactionalRepository.class);
        when(defaultTransactionalRepository.getById(any())).thenReturn("42");
        defaultTransactionalRepository.<Object>getById("Id");
        verify(defaultTransactionalRepository).getById(any());
    }

    @Test
    public void testDrop() {
        DefaultTransactionalRepository<Object> defaultTransactionalRepository = (DefaultTransactionalRepository<Object>) mock(
            DefaultTransactionalRepository.class);
        doNothing().when(defaultTransactionalRepository).drop();
        defaultTransactionalRepository.drop();
        verify(defaultTransactionalRepository).drop();
    }

    @Test
    public void testIsDropped() {
        DefaultTransactionalRepository<Object> defaultTransactionalRepository = (DefaultTransactionalRepository<Object>) mock(
            DefaultTransactionalRepository.class);
        when(defaultTransactionalRepository.isDropped()).thenReturn(true);
        defaultTransactionalRepository.isDropped();
        verify(defaultTransactionalRepository).isDropped();
    }

    @Test
    public void testIsOpen() {
        DefaultTransactionalRepository<Object> defaultTransactionalRepository = (DefaultTransactionalRepository<Object>) mock(
            DefaultTransactionalRepository.class);
        when(defaultTransactionalRepository.isOpen()).thenReturn(true);
        defaultTransactionalRepository.isOpen();
        verify(defaultTransactionalRepository).isOpen();
    }

    @Test
    public void testClose() throws Exception {
        DefaultTransactionalRepository<Object> defaultTransactionalRepository = (DefaultTransactionalRepository<Object>) mock(
            DefaultTransactionalRepository.class);
        doNothing().when(defaultTransactionalRepository).close();
        defaultTransactionalRepository.close();
        verify(defaultTransactionalRepository).close();
    }

    @Test
    public void testSize() {
        DefaultTransactionalRepository<Object> defaultTransactionalRepository = (DefaultTransactionalRepository<Object>) mock(
            DefaultTransactionalRepository.class);
        when(defaultTransactionalRepository.size()).thenReturn(1L);
        defaultTransactionalRepository.size();
        verify(defaultTransactionalRepository).size();
    }

    @Test
    public void testGetStore() {
        DefaultTransactionalRepository<Object> defaultTransactionalRepository = (DefaultTransactionalRepository<Object>) mock(
            DefaultTransactionalRepository.class);
        doReturn(new TransactionStore<>(new TransactionStore<>(
            new TransactionStore<>(new TransactionStore<>(null))))).when(defaultTransactionalRepository).getStore();
        defaultTransactionalRepository.getStore();
        verify(defaultTransactionalRepository).getStore();
    }

    @Test
    public void testSubscribe() {
        DefaultTransactionalRepository<Object> defaultTransactionalRepository = (DefaultTransactionalRepository<Object>) mock(
            DefaultTransactionalRepository.class);
        when(defaultTransactionalRepository.subscribe(any())).thenReturn("Subscription");
        defaultTransactionalRepository.subscribe(mock(CollectionEventListener.class));
        verify(defaultTransactionalRepository).subscribe(any());
    }

    @Test
    public void testUnsubscribe() {
        DefaultTransactionalRepository<Object> defaultTransactionalRepository = (DefaultTransactionalRepository<Object>) mock(
            DefaultTransactionalRepository.class);
        doNothing().when(defaultTransactionalRepository).unsubscribe(any());
        defaultTransactionalRepository.unsubscribe("Subscription");
        verify(defaultTransactionalRepository).unsubscribe(any());
    }

    @Test
    public void testGetAttributes() {
        DefaultTransactionalRepository<Object> defaultTransactionalRepository = (DefaultTransactionalRepository<Object>) mock(
            DefaultTransactionalRepository.class);
        when(defaultTransactionalRepository.getAttributes()).thenReturn(new Attributes());
        defaultTransactionalRepository.getAttributes();
        verify(defaultTransactionalRepository).getAttributes();
    }

    @Test
    public void testSetAttributes() {
        DefaultTransactionalRepository<Object> defaultTransactionalRepository = (DefaultTransactionalRepository<Object>) mock(
            DefaultTransactionalRepository.class);
        doNothing().when(defaultTransactionalRepository).setAttributes(any());
        defaultTransactionalRepository.setAttributes(new Attributes());
        verify(defaultTransactionalRepository).setAttributes(any());
    }
}

