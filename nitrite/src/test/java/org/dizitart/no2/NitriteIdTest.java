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

package org.dizitart.no2;

import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.exceptions.InvalidIdException;
import org.junit.Test;

import static org.junit.Assert.*;

public class NitriteIdTest {

    @Test
    public void testLimit() {
        NitriteId one = NitriteId.createId(Long.toString(Long.MAX_VALUE));
        NitriteId two = NitriteId.createId(Long.toString(Long.MIN_VALUE));
        assertEquals(one.compareTo(two), 1);
    }

    @Test
    public void testHashEquals() {
        NitriteId one = NitriteId.createId("1");
        NitriteId two = NitriteId.createId("1");

        assertEquals(one, two);
        assertEquals(one.hashCode(), two.hashCode());

        NitriteId third = NitriteId.createId("2");
        assertNotEquals(one, third);
        assertNotEquals(one.hashCode(), third.hashCode());
    }

    @Test
    public void testCompare() {
        NitriteId one = NitriteId.createId("1");
        NitriteId two = NitriteId.createId("2");
        NitriteId three = NitriteId.createId("3");

        assertEquals(one.compareTo(two), -1);
        assertEquals(three.compareTo(one), 1);

        one = NitriteId.createId("10");
        two = NitriteId.createId("20");
        assertEquals(one.compareTo(two), -1);

        one = NitriteId.newId();
        two = NitriteId.newId();

        assertFalse(one.compareTo(two) == 0);
    }

    @Test(expected = InvalidIdException.class)
    public void testToString() {
        NitriteId nullId = NitriteId.createId(null);
        assertNotEquals(nullId.toString(), "");
    }

    @Test(expected = InvalidIdException.class)
    public void testCompareNull() {
        NitriteId first = NitriteId.newId();
        NitriteId second = NitriteId.createId(null);
        assertEquals(first.compareTo(second), 1);
    }
}
