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

public class FieldsTest {
    @Test
    public void testConstructor() {
        assertEquals("[]", (new Fields()).toString());
    }

    @Test
    public void testWithNames() {
        Fields actualWithNamesResult = Fields.withNames("Fields");
        assertEquals("[Fields]", actualWithNamesResult.toString());
        List<String> stringList = actualWithNamesResult.fieldNames;
        assertEquals(1, stringList.size());
        assertEquals("Fields", stringList.get(0));
    }

    @Test
    public void testAddField() {
        Fields fields = new Fields();
        assertSame(fields, fields.addField("Field"));
    }

    @Test
    public void testGetFieldNames() {
        assertTrue((new Fields()).getFieldNames().isEmpty());
    }

    @Test
    public void testStartsWith() {
        Fields fields = new Fields();
        assertTrue(fields.startsWith(new Fields()));
    }

    @Test
    public void testStartsWith2() {
        assertFalse((new Fields()).startsWith(null));
    }

    @Test
    public void testStartsWith3() {
        Fields fields = new Fields();
        assertFalse(fields.startsWith(Fields.withNames("foo", "foo", "foo")));
    }

    @Test
    public void testGetEncodedName() {
        assertEquals("", (new Fields()).getEncodedName());
    }

    @Test
    public void testCompareTo() {
        Fields fields = new Fields();
        assertEquals(0, fields.compareTo(new Fields()));
    }

    @Test
    public void testCompareTo2() {
        Fields withNamesResult = Fields.withNames("foo", "foo", "foo");
        assertEquals(1, withNamesResult.compareTo(new Fields()));
    }

    @Test
    public void testCompareTo3() {
        assertEquals(1, (new Fields()).compareTo(null));
    }

    @Test
    public void testCompareTo4() {
        Fields fields = new Fields();
        fields.setFieldNames(null);
        assertEquals(1, fields.compareTo(null));
    }

    @Test
    public void testCompareTo5() {
        Fields withNamesResult = Fields.withNames("foo", "foo", "foo");
        assertEquals(0, withNamesResult.compareTo(Fields.withNames("foo", "foo", "foo")));
    }

    @Test
    public void testCompareTo6() {
        Fields withNamesResult = Fields.withNames("Fields", "foo", "foo");
        assertEquals(-32, withNamesResult.compareTo(Fields.withNames("foo", "foo", "foo")));
    }
}

