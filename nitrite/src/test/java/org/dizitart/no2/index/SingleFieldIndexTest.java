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
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.memory.InMemoryStore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import static org.dizitart.no2.common.tuples.Pair.pair;
import static org.dizitart.no2.common.util.IndexUtils.deriveCompositeIndexMapName;
import static org.dizitart.no2.common.util.IndexUtils.deriveIndexMapName;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.*;

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
}

