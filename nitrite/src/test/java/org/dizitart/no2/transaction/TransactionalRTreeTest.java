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

import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.index.BoundingBox;
import org.dizitart.no2.store.memory.InMemoryRTree;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class TransactionalRTreeTest {

    @Test
    public void testAdd2() {
        TransactionalRTree<BoundingBox, Object> transactionalRTree = new TransactionalRTree<>(
            new InMemoryRTree<>(null, null));
        BoundingBox boundingBox = mock(BoundingBox.class);
        when(boundingBox.getMaxY()).thenReturn(10.0f);
        when(boundingBox.getMinY()).thenReturn(10.0f);
        when(boundingBox.getMaxX()).thenReturn(10.0f);
        when(boundingBox.getMinX()).thenReturn(10.0f);
        transactionalRTree.add(boundingBox, NitriteId.newId());
        verify(boundingBox).getMaxX();
        verify(boundingBox).getMaxY();
        verify(boundingBox).getMinX();
        verify(boundingBox).getMinY();
        assertEquals(1L, transactionalRTree.size());
    }

    @Test
    public void testRemove2() {
        TransactionalRTree<BoundingBox, Object> transactionalRTree = new TransactionalRTree<>(
            new InMemoryRTree<>(null, null));
        BoundingBox boundingBox = mock(BoundingBox.class);
        when(boundingBox.getMaxY()).thenReturn(10.0f);
        when(boundingBox.getMinY()).thenReturn(10.0f);
        when(boundingBox.getMaxX()).thenReturn(10.0f);
        when(boundingBox.getMinX()).thenReturn(10.0f);
        transactionalRTree.remove(boundingBox, NitriteId.newId());
        verify(boundingBox).getMaxX();
        verify(boundingBox).getMaxY();
        verify(boundingBox).getMinX();
        verify(boundingBox).getMinY();
        assertEquals(0L, transactionalRTree.size());
    }

    @Test
    public void testFindIntersectingKeys() {
        TransactionalRTree<BoundingBox, Object> transactionalRTree = new TransactionalRTree<>(
            new InMemoryRTree<>(null, null));
        BoundingBox boundingBox = mock(BoundingBox.class);
        when(boundingBox.getMaxY()).thenReturn(10.0f);
        when(boundingBox.getMinY()).thenReturn(10.0f);
        when(boundingBox.getMaxX()).thenReturn(10.0f);
        when(boundingBox.getMinX()).thenReturn(10.0f);
        assertTrue(transactionalRTree.findIntersectingKeys(boundingBox).toList().isEmpty());
        verify(boundingBox, times(2)).getMaxX();
        verify(boundingBox, times(2)).getMaxY();
        verify(boundingBox, times(2)).getMinX();
        verify(boundingBox, times(2)).getMinY();
    }

    @Test
    public void testFindIntersectingKeys2() {
        TransactionalRTree<BoundingBox, Object> transactionalRTree = new TransactionalRTree<>(
            new TransactionalRTree<>(new InMemoryRTree<>(null, null)));
        BoundingBox boundingBox = mock(BoundingBox.class);
        when(boundingBox.getMaxY()).thenReturn(10.0f);
        when(boundingBox.getMinY()).thenReturn(10.0f);
        when(boundingBox.getMaxX()).thenReturn(10.0f);
        when(boundingBox.getMinX()).thenReturn(10.0f);
        assertTrue(transactionalRTree.findIntersectingKeys(boundingBox).toList().isEmpty());
        verify(boundingBox, times(3)).getMaxX();
        verify(boundingBox, times(3)).getMaxY();
        verify(boundingBox, times(3)).getMinX();
        verify(boundingBox, times(3)).getMinY();
    }

    @Test
    public void testFindContainedKeys() {
        TransactionalRTree<BoundingBox, Object> transactionalRTree = new TransactionalRTree<>(
            new InMemoryRTree<>(null, null));
        BoundingBox boundingBox = mock(BoundingBox.class);
        when(boundingBox.getMaxY()).thenReturn(10.0f);
        when(boundingBox.getMinY()).thenReturn(10.0f);
        when(boundingBox.getMaxX()).thenReturn(10.0f);
        when(boundingBox.getMinX()).thenReturn(10.0f);
        assertTrue(transactionalRTree.findContainedKeys(boundingBox).toList().isEmpty());
        verify(boundingBox, times(2)).getMaxX();
        verify(boundingBox, times(2)).getMaxY();
        verify(boundingBox, times(2)).getMinX();
        verify(boundingBox, times(2)).getMinY();
    }

    @Test
    public void testFindContainedKeys2() {
        TransactionalRTree<BoundingBox, Object> transactionalRTree = new TransactionalRTree<>(
            new TransactionalRTree<>(new InMemoryRTree<>(null, null)));
        BoundingBox boundingBox = mock(BoundingBox.class);
        when(boundingBox.getMaxY()).thenReturn(10.0f);
        when(boundingBox.getMinY()).thenReturn(10.0f);
        when(boundingBox.getMaxX()).thenReturn(10.0f);
        when(boundingBox.getMinX()).thenReturn(10.0f);
        assertTrue(transactionalRTree.findContainedKeys(boundingBox).toList().isEmpty());
        verify(boundingBox, times(3)).getMaxX();
        verify(boundingBox, times(3)).getMaxY();
        verify(boundingBox, times(3)).getMinX();
        verify(boundingBox, times(3)).getMinY();
    }

    @Test
    public void testSize() {
        assertEquals(0L, (new TransactionalRTree<>(new InMemoryRTree<>(null, null))).size());
    }

    @Test
    public void testClose() {
        TransactionalRTree<BoundingBox, Object> transactionalRTree = new TransactionalRTree<>(
            new InMemoryRTree<>(null, null));
        transactionalRTree.close();
        assertEquals(0L, transactionalRTree.size());
    }

    @Test
    public void testConstructor() {
        assertEquals(0L, (new TransactionalRTree<>(new InMemoryRTree<>(null, null))).size());
    }
}

