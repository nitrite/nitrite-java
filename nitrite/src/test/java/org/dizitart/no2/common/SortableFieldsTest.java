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

package org.dizitart.no2.common;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class SortableFieldsTest {
    @Test
    public void testConstructor() {
        assertEquals("[]", (new SortableFields()).toString());
    }

    @Test
    public void testWithNames() {
        SortableFields actualWithNamesResult = SortableFields.withNames("Fields");
        assertEquals("[Fields]", actualWithNamesResult.toString());
        List<String> stringList = actualWithNamesResult.fieldNames;
        assertEquals(1, stringList.size());
        assertEquals("Fields", stringList.get(0));
    }

    @Test
    public void testAddField() {
        SortableFields sortableFields = new SortableFields();
        assertSame(sortableFields, sortableFields.addField("Field", SortOrder.Ascending));
    }

    @Test
    public void testGetSortingOrders() {
        assertTrue((new SortableFields()).getSortingOrders().isEmpty());
    }
}

