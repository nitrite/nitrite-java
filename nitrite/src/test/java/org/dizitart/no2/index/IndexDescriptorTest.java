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

import org.dizitart.no2.common.Fields;
import org.dizitart.no2.exceptions.ValidationException;
import org.junit.Test;

import static org.junit.Assert.*;

public class IndexDescriptorTest {
    @Test
    public void testConstructor() {
        IndexDescriptor actualIndexDescriptor = new IndexDescriptor("Index Type", new Fields(), "Collection Name");
        assertEquals("Collection Name", actualIndexDescriptor.getCollectionName());
        assertFalse(actualIndexDescriptor.isCompoundIndex());
        assertEquals("Index Type", actualIndexDescriptor.getIndexType());
    }

    @Test(expected = ValidationException.class)
    public void testConstructor3() {
        Fields fields = new Fields();
        new IndexDescriptor("Index Type", fields, "");
        assertEquals("", fields.getEncodedName());
        assertEquals("[]", fields.toString());
    }

    @Test
    public void testCompareTo() {
        IndexDescriptor indexDescriptor = new IndexDescriptor("Index Type", new Fields(), "Collection Name");
        assertEquals(0, indexDescriptor.compareTo(new IndexDescriptor("Index Type", new Fields(), "Collection Name")));
    }

    @Test
    public void testIsCompoundIndex() {
        assertFalse((new IndexDescriptor("Index Type", new Fields(), "Collection Name")).isCompoundIndex());
    }

    @Test
    public void testIsCompoundIndex2() {
        Fields fields = new Fields();
        fields.addField("Field");
        fields.addField("Field");
        assertTrue((new IndexDescriptor("Index Type", fields, "Collection Name")).isCompoundIndex());
    }
}

