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

import static org.junit.Assert.*;

public class LookupTest {
    @Test
    public void testCanEqual() {
        assertFalse((new Lookup()).canEqual("Other"));
    }

    @Test
    public void testCanEqual2() {
        Lookup lookup = new Lookup();
        assertTrue(lookup.canEqual(new Lookup()));
    }

    @Test
    public void testConstructor() {
        Lookup actualLookup = new Lookup();
        actualLookup.setForeignField("Foreign Field");
        actualLookup.setLocalField("Local Field");
        actualLookup.setTargetField("Target Field");
        assertEquals("Foreign Field", actualLookup.getForeignField());
        assertEquals("Local Field", actualLookup.getLocalField());
        assertEquals("Target Field", actualLookup.getTargetField());
    }

    @Test
    public void testEquals() {
        assertFalse((new Lookup()).equals("42"));
    }

    @Test
    public void testEquals10() {
        Lookup lookup = new Lookup();
        lookup.setLocalField("Local Field");

        Lookup lookup1 = new Lookup();
        lookup1.setLocalField("Local Field");
        assertTrue(lookup.equals(lookup1));
    }

    @Test
    public void testEquals11() {
        Lookup lookup = new Lookup();
        lookup.setTargetField("Target Field");

        Lookup lookup1 = new Lookup();
        lookup1.setTargetField("Target Field");
        assertTrue(lookup.equals(lookup1));
    }

    @Test
    public void testEquals2() {
        Lookup lookup = new Lookup();
        assertTrue(lookup.equals(new Lookup()));
    }

    @Test
    public void testEquals3() {
        Lookup lookup = new Lookup();
        lookup.setForeignField("Foreign Field");
        assertFalse(lookup.equals(new Lookup()));
    }

    @Test
    public void testEquals4() {
        Lookup lookup = new Lookup();
        lookup.setLocalField("Local Field");
        assertFalse(lookup.equals(new Lookup()));
    }

    @Test
    public void testEquals5() {
        Lookup lookup = new Lookup();
        lookup.setTargetField("Target Field");
        assertFalse(lookup.equals(new Lookup()));
    }

    @Test
    public void testEquals6() {
        Lookup lookup = new Lookup();

        Lookup lookup1 = new Lookup();
        lookup1.setForeignField("Foreign Field");
        assertFalse(lookup.equals(lookup1));
    }

    @Test
    public void testEquals7() {
        Lookup lookup = new Lookup();

        Lookup lookup1 = new Lookup();
        lookup1.setLocalField("Local Field");
        assertFalse(lookup.equals(lookup1));
    }

    @Test
    public void testEquals8() {
        Lookup lookup = new Lookup();

        Lookup lookup1 = new Lookup();
        lookup1.setTargetField("Target Field");
        assertFalse(lookup.equals(lookup1));
    }

    @Test
    public void testEquals9() {
        Lookup lookup = new Lookup();
        lookup.setForeignField("Foreign Field");

        Lookup lookup1 = new Lookup();
        lookup1.setForeignField("Foreign Field");
        assertTrue(lookup.equals(lookup1));
    }

    @Test
    public void testHashCode() {
        assertEquals(357642, (new Lookup()).hashCode());
    }

    @Test
    public void testHashCode2() {
        Lookup lookup = new Lookup();
        lookup.setForeignField("Foreign Field");
        assertEquals(1964140155, lookup.hashCode());
    }

    @Test
    public void testHashCode3() {
        Lookup lookup = new Lookup();
        lookup.setLocalField("Local Field");
        assertEquals(1343314452, lookup.hashCode());
    }

    @Test
    public void testHashCode4() {
        Lookup lookup = new Lookup();
        lookup.setTargetField("Target Field");
        assertEquals(-1878733174, lookup.hashCode());
    }
}

