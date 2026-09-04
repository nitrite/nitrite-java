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

package org.dizitart.no2.index;

import org.dizitart.no2.collection.FindPlan;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.DBValue;
import org.dizitart.no2.common.FieldValues;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.filters.ComparableFilter;
import org.dizitart.no2.filters.IndexScanFilter;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.store.memory.InMemoryMap;
import org.dizitart.no2.filters.SortingAwareFilter;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.memory.InMemoryStore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.dizitart.no2.common.tuples.Pair.pair;
import static org.dizitart.no2.common.util.IndexUtils.deriveCompositeIndexMapName;
import static org.dizitart.no2.common.util.IndexUtils.deriveIndexMapName;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class SingleFieldIndexTest {
    @Test
    public void testConstructor() {
        IndexDescriptor indexDescriptor = new IndexDescriptor("Index Type", new Fields(), "Collection Name");
        assertSame(indexDescriptor, (new SingleFieldIndex(indexDescriptor, null)).getIndexDescriptor());
    }

    @Test
    public void testWrite() {
        SingleFieldIndex singleFieldIndex = new SingleFieldIndex(
                new IndexDescriptor("Index Type", new Fields(), "Collection Name"), new InMemoryStore());
        FieldValues fieldValues = new FieldValues();
        fieldValues.setNitriteId(NitriteId.createId(1L));
        fieldValues.getValues().add(pair("a", 1));
        singleFieldIndex.write(fieldValues);
        assertEquals("[a]", fieldValues.getFields().toString());
    }

    @Test
    public void testRemove() {
        SingleFieldIndex singleFieldIndex = new SingleFieldIndex(
                new IndexDescriptor("Index Type", new Fields(), "Collection Name"), new InMemoryStore());
        FieldValues fieldValues = new FieldValues();
        fieldValues.setNitriteId(NitriteId.createId(1L));
        fieldValues.getValues().add(pair("a", 1));
        singleFieldIndex.remove(fieldValues);
        assertEquals("[a]", fieldValues.getFields().toString());
    }

    @Test
    public void testFindNitriteIds() {
        SingleFieldIndex singleFieldIndex = new SingleFieldIndex(
                new IndexDescriptor("Index Type", new Fields(), "Collection Name"), null);
        assertTrue(singleFieldIndex.findNitriteIds(new FindPlan()).isEmpty());
    }

    @Test
    public void testLegacyArrayIndexMigratedToComposite() {
        // Issue #1260: a non-unique index written by an older Nitrite uses the legacy
        // value -> [ids] array layout. On first access it must be migrated into the composite
        // layout (and the legacy map dropped) while still returning every id.
        InMemoryStore store = new InMemoryStore();
        IndexDescriptor desc = new IndexDescriptor(IndexType.NON_UNIQUE,
                Fields.withNames("firstField"), "coll");

        String legacyName = deriveIndexMapName(desc);
        NitriteMap<DBValue, List<NitriteId>> legacy = store.openMap(legacyName,
                DBValue.class, ArrayList.class);
        NitriteId id1 = NitriteId.createId(1L);
        NitriteId id2 = NitriteId.createId(2L);
        NitriteId id3 = NitriteId.createId(3L);
        legacy.put(new DBValue("k1"), new ArrayList<>(Arrays.asList(id1, id2)));
        legacy.put(new DBValue("k2"), new ArrayList<>(Collections.singletonList(id3)));

        SingleFieldIndex index = new SingleFieldIndex(desc, store);
        FindPlan plan = new FindPlan();
        plan.setIndexDescriptor(desc);
        plan.setIndexScanFilter(new IndexScanFilter(Collections.singletonList(
                (ComparableFilter) where("firstField").eq("k1"))));

        LinkedHashSet<NitriteId> ids = index.findNitriteIds(plan);
        assertEquals(new LinkedHashSet<>(Arrays.asList(id1, id2)), ids);

        // legacy map dropped, composite map now backs the index
        assertFalse(store.hasMap(legacyName));
        assertTrue(store.hasMap(deriveCompositeIndexMapName(desc)));
    }

    @Test
    public void testLazyStreamMatchesMaterializedScanForEqualityAndRange() {
        InMemoryStore store = new InMemoryStore();
        IndexDescriptor desc = new IndexDescriptor(IndexType.NON_UNIQUE, Fields.withNames("k"), "c");
        SingleFieldIndex index = new SingleFieldIndex(desc, store);
        for (long id = 1; id <= 30; id++) {
            index.write(values(id, "k", (int) (id % 5)));           // five keys, six ids each
        }
        index.write(values(31L, "k", new int[]{1, 2, 3}));          // one document under three keys

        FindPlan eq = plan(desc, Collections.singletonList((ComparableFilter) where("k").eq(2)), null);
        assertEquals(new ArrayList<>(index.findNitriteIds(eq)), index.findNitriteIdStream(eq).toList());

        List<ComparableFilter> between = Arrays.asList((ComparableFilter) where("k").gte(1), (ComparableFilter) where("k").lt(3));
        FindPlan range = plan(desc, between, null);
        assertEquals(new ArrayList<>(index.findNitriteIds(range)), index.findNitriteIdStream(range).toList());
        assertEquals("id 31 is under two keys of the range but returned once", 13, index.findNitriteIdStream(range).toList().size());

        Map<String, Boolean> descending = new HashMap<>();
        descending.put("k", true);
        FindPlan reversed = plan(desc, between, descending);
        assertEquals(new ArrayList<>(index.findNitriteIds(reversed)), index.findNitriteIdStream(reversed).toList());
        // key groups are visited backwards, ids inside a group keep their stored order
        List<NitriteId> forward = index.findNitriteIdStream(range).toList();
        List<NitriteId> backward = index.findNitriteIdStream(reversed).toList();
        assertEquals(NitriteId.createId(1L), forward.get(0));
        assertEquals(NitriteId.createId(2L), backward.get(0));
        assertEquals(forward.size(), backward.size());
    }

    @Test
    public void testLazyStreamDeclinesShapesItDoesNotServe() {
        InMemoryStore store = new InMemoryStore();
        IndexDescriptor desc = new IndexDescriptor(IndexType.NON_UNIQUE, Fields.withNames("k"), "c");
        SingleFieldIndex index = new SingleFieldIndex(desc, store);
        index.write(values(1L, "k", 1));

        assertNull("one-sided range", index.findNitriteIdStream(plan(desc, Collections.singletonList((ComparableFilter) where("k").gt(0)), null)));
        assertNull("in filter", index.findNitriteIdStream(plan(desc, Collections.singletonList((ComparableFilter) where("k").in(1, 2)), null)));
        assertNull("no scan filter", index.findNitriteIdStream(new FindPlan()));

        IndexDescriptor unique = new IndexDescriptor(IndexType.UNIQUE, Fields.withNames("k"), "c");
        SingleFieldIndex uniqueIndex = new SingleFieldIndex(unique, store);
        uniqueIndex.write(values(1L, "k", 1));
        assertNull("unique layout", uniqueIndex.findNitriteIdStream(plan(unique, Collections.singletonList((ComparableFilter) where("k").eq(1)), null)));
    }

    @Test
    public void testLazyStreamReadsOnlyAsFarAsConsumed() {
        IndexDescriptor desc = new IndexDescriptor(IndexType.NON_UNIQUE, Fields.withNames("k"), "c");
        InMemoryMap<IndexEntryKey, Object> composite = spy(new InMemoryMap<>(deriveCompositeIndexMapName(desc), new InMemoryStore()));
        for (long id = 1; id <= 500; id++) {
            composite.put(new IndexEntryKey(new DBValue("same"), NitriteId.createId(id)), Boolean.TRUE);
        }
        NitriteStore<?> store = mock(NitriteStore.class);
        when(store.hasMap(anyString())).thenReturn(false);
        doReturn(composite).when(store).openMap(eq(deriveCompositeIndexMapName(desc)), any(), any());
        clearInvocations(composite);

        SingleFieldIndex index = new SingleFieldIndex(desc, store);
        FindPlan eq = plan(desc, Collections.singletonList((ComparableFilter) where("k").eq("same")), null);
        RecordStream<NitriteId> stream = index.findNitriteIdStream(eq);
        assertNotNull(stream);

        assertEquals(NitriteId.createId(1L), stream.iterator().next());
        verify(composite, atMost(2)).higherKey(any());
        verify(composite, never()).entries();
        assertEquals(500, stream.toList().size());
    }

    private static FindPlan plan(IndexDescriptor desc, List<ComparableFilter> filters, Map<String, Boolean> scanOrder) {
        FindPlan plan = new FindPlan();
        plan.setIndexDescriptor(desc);
        plan.setIndexScanFilter(new IndexScanFilter(filters));
        plan.setIndexScanOrder(scanOrder);
        return plan;
    }

    private static FieldValues values(long id, String field, Object value) {
        FieldValues fieldValues = new FieldValues();
        fieldValues.setNitriteId(NitriteId.createId(id));
        fieldValues.getValues().add(pair(field, value));
        return fieldValues;
    }
}
