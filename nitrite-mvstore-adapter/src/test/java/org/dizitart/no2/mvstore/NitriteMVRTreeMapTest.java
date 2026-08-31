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

package org.dizitart.no2.mvstore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Iterator;

import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.index.BoundingBox;
import org.dizitart.no2.store.NitriteStore;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.rtree.MVRTreeMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class NitriteMVRTreeMapTest {
    private MVStore mvStore;
    private MVRTreeMap<BoundingBox> mvMap;
    private NitriteMVRTreeMap<BoundingBox, BoundingBox> nitriteMVRTreeMap;

    @Before
    public void setUp() {
        mvStore = spy(new MVStore.Builder().open());
        mvMap = spy(mvStore.openMap("test", new MVRTreeMap.Builder<>()));
        NitriteStore<?> nitriteStore = mock(NitriteStore.class);
        nitriteMVRTreeMap = new NitriteMVRTreeMap<>(mvMap, nitriteStore);

        nitriteMVRTreeMap.add(new BoundingBox(0, 1, 0, 1), NitriteId.createId("1"));
        clearInvocations(mvStore, mvMap);
    }

    @After
    public void tearDown() {
        mvStore.closeImmediately();
    }

    @Test
    public void testIntersectingCursorRetainsVersionUntilExhausted() {
        RecordStream<NitriteId> recordStream =
            nitriteMVRTreeMap.findIntersectingKeys(new BoundingBox(-1, 2, -1, 2));
        verify(mvMap, never()).findIntersectingKeys(any());

        Iterator<NitriteId> iterator = recordStream.iterator();
        InOrder inOrder = inOrder(mvStore, mvMap);
        inOrder.verify(mvStore).registerVersionUsage();
        inOrder.verify(mvMap).findIntersectingKeys(any());
        assertVersionRetainedUntilExhausted(iterator);
    }

    @Test
    public void testContainedCursorRetainsVersionUntilExhausted() {
        RecordStream<NitriteId> recordStream =
            nitriteMVRTreeMap.findContainedKeys(new BoundingBox(-1, 2, -1, 2));
        verify(mvMap, never()).findContainedKeys(any());

        Iterator<NitriteId> iterator = recordStream.iterator();
        InOrder inOrder = inOrder(mvStore, mvMap);
        inOrder.verify(mvStore).registerVersionUsage();
        inOrder.verify(mvMap).findContainedKeys(any());
        assertVersionRetainedUntilExhausted(iterator);
    }

    @Test
    public void testAbandonedCursorReleasesVersionOnClose() {
        Iterator<NitriteId> iterator = nitriteMVRTreeMap
            .findIntersectingKeys(new BoundingBox(-1, 2, -1, 2))
            .iterator();

        assertEquals(NitriteId.createId("1"), iterator.next());
        verify(mvStore, never()).deregisterVersionUsage(any());

        nitriteMVRTreeMap.close();
        verify(mvStore).deregisterVersionUsage(any());
        assertThrows(NitriteIOException.class, iterator::hasNext);
    }

    private void assertVersionRetainedUntilExhausted(Iterator<NitriteId> iterator) {
        assertTrue(iterator.hasNext());
        assertEquals(NitriteId.createId("1"), iterator.next());
        verify(mvStore, never()).deregisterVersionUsage(any());

        assertFalse(iterator.hasNext());
        verify(mvStore).deregisterVersionUsage(any());
    }
}
