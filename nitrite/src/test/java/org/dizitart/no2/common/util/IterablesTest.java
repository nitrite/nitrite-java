/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.common.util;

import org.dizitart.no2.integration.Retry;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.dizitart.no2.common.util.Iterables.firstOrNull;
import static org.dizitart.no2.common.util.Iterables.toArray;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee.
 */
public class IterablesTest {

    @Rule
    public Retry retry = new Retry(3);

    @Test
    public void testFirstOrNull() {
        assertNull(Iterables.firstOrNull(new ArrayList<>()));
    }

    @Test
    public void testFirstOrNull2() {
        ArrayList<Object> objectList = new ArrayList<>();
        objectList.add(null);
        assertNull(Iterables.firstOrNull(objectList));
    }

    @Test
    public void testToList() {
        ArrayList<Object> objectList = new ArrayList<Object>();
        List<Object> actualToListResult = Iterables.toList(objectList);
        assertSame(objectList, actualToListResult);
        assertEquals(0, actualToListResult.size());
    }

    @Test
    public void testToSet() {
        ArrayList<Object> objectList = new ArrayList<>();
        objectList.add("foo");
        assertEquals(1, Iterables.toSet(objectList).size());
    }

    @Test
    public void testToSet2() {
        assertEquals(0, Iterables.toSet(new ArrayList<>()).size());
    }

    @Test
    public void testFirstOrDefault() {
        assertNull(firstOrNull(new ArrayList<>()));
        assertNull(firstOrNull(null));
    }

    @Test
    public void testToArray() {
        final List<String> list = new ArrayList<String>() {{
            add("a");
            add("b");
        }};
        assertArrayEquals(toArray(new ArrayList<String>() {{
                add("a");
                add("b");
            }}),
            new String[]{"a", "b"});
        assertArrayEquals(toArray(list), new String[]{"a", "b"});
    }

    @Test
    public void testToArray2() {
        Class type = Object.class;
        assertEquals(0, Iterables.toArray(new ArrayList<>(), type).length);
    }

    @Test
    public void testToArray3() {
        assertEquals(0, Iterables.toArray(new ArrayList<>()).length);
    }

    @Test
    public void testArrayContains() {
        assertTrue(Iterables.arrayContains(new Object[]{"element"}, "element"));
        assertFalse(Iterables.arrayContains(new Object[]{"array"}, "element"));
    }

    @Test
    public void testListOf() {
        assertEquals(1, Iterables.<Object>listOf("items").size());
        assertEquals(1, Iterables.listOf((Object) null).size());
    }

    @Test
    public void testSize() {
        assertEquals(0L, Iterables.size(new ArrayList<>()));
    }
}
