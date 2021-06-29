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
import org.dizitart.no2.common.FieldValues;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.store.memory.InMemoryStore;
import org.junit.Test;

import static org.dizitart.no2.common.tuples.Pair.pair;
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
        fieldValues.getValues().add(pair("a", 1));
        singleFieldIndex.write(fieldValues);
        assertEquals("FieldValues(nitriteId=null, fields=[a], values=[Pair(first=a, second=1)])", fieldValues.toString());
        assertEquals("[a]", fieldValues.getFields().toString());
    }

    @Test
    public void testRemove() {
        SingleFieldIndex singleFieldIndex = new SingleFieldIndex(
                new IndexDescriptor("Index Type", new Fields(), "Collection Name"), new InMemoryStore());
        FieldValues fieldValues = new FieldValues();
        fieldValues.getValues().add(pair("a", 1));
        singleFieldIndex.remove(fieldValues);
        assertEquals("FieldValues(nitriteId=null, fields=[a], values=[Pair(first=a, second=1)])", fieldValues.toString());
        assertEquals("[a]", fieldValues.getFields().toString());
    }

    @Test
    public void testFindNitriteIds() {
        SingleFieldIndex singleFieldIndex = new SingleFieldIndex(
                new IndexDescriptor("Index Type", new Fields(), "Collection Name"), null);
        assertTrue(singleFieldIndex.findNitriteIds(new FindPlan()).isEmpty());
    }
}

