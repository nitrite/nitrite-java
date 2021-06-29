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
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.store.memory.InMemoryStore;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CompoundIndexTest {
    @Test
    public void testConstructor() {
        IndexDescriptor indexDescriptor = new IndexDescriptor("Index Type", new Fields(), "Collection Name");
        assertSame(indexDescriptor, (new CompoundIndex(indexDescriptor, null)).getIndexDescriptor());
    }

    @Test
    public void testWrite() {
        CompoundIndex compoundIndex = new CompoundIndex(new IndexDescriptor("Index Type", Fields.withNames("a"), "Collection Name"),
            new InMemoryStore());
        FieldValues fieldValues = new FieldValues();
        fieldValues.getValues().add(Pair.pair("a", 1));
        compoundIndex.write(fieldValues);
        assertEquals("FieldValues(nitriteId=null, fields=[a], values=[Pair(first=a, second=1)])", fieldValues.toString());
        assertEquals("[a]", fieldValues.getFields().toString());
    }

    @Test
    public void testRemove() {
        CompoundIndex compoundIndex = new CompoundIndex(new IndexDescriptor("Index Type", Fields.withNames("a"), "Collection Name"),
            new InMemoryStore());
        FieldValues fieldValues = new FieldValues();
        fieldValues.getValues().add(Pair.pair("a", 1));
        compoundIndex.remove(fieldValues);
        assertEquals("FieldValues(nitriteId=null, fields=[a], values=[Pair(first=a, second=1)])", fieldValues.toString());
        assertEquals("[a]", fieldValues.getFields().toString());
    }

    @Test(expected = IndexingException.class)
    public void testDrop() {
        Fields fields = mock(Fields.class);
        when(fields.getEncodedName()).thenThrow(new IndexingException("An error occurred"));
        CompoundIndex compoundIndex = new CompoundIndex(new IndexDescriptor("Index Type", fields, "Collection Name"),
            new InMemoryStore());
        compoundIndex.drop();
        verify(fields).getEncodedName();
        assertFalse(compoundIndex.getIndexDescriptor().isCompoundIndex());
    }

    @Test
    public void testDrop2() {
        Fields fields = mock(Fields.class);
        when(fields.getEncodedName()).thenReturn("foo");
        IndexDescriptor indexDescriptor = new IndexDescriptor("Index Type", fields, "Collection Name");
        CompoundIndex compoundIndex = new CompoundIndex(indexDescriptor, new InMemoryStore());
        compoundIndex.drop();
        verify(fields).getEncodedName();
        assertFalse(compoundIndex.getIndexDescriptor().isCompoundIndex());
    }

    @Test
    public void testFindNitriteIds() {
        CompoundIndex compoundIndex = new CompoundIndex(new IndexDescriptor("Index Type", new Fields(), "Collection Name"),
            null);
        assertTrue(compoundIndex.findNitriteIds(new FindPlan()).isEmpty());
    }
}

