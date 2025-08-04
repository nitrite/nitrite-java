package org.dizitart.no2.collection;

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

    @Test
    public void testCreateId() {
        assertEquals(42L, NitriteId.createId("42").getIdValue());
        assertThrows(InvalidIdException.class, () -> NitriteId.createId(null));
        assertThrows(InvalidIdException.class, () -> NitriteId.createId("Value"));
    }

    @Test
    public void testValidId() {
        assertThrows(InvalidIdException.class, () -> NitriteId.validId("Value"));
        assertThrows(InvalidIdException.class, () -> NitriteId.validId(null));
        assertTrue(NitriteId.validId(42));
    }

    @Test
    public void testCompareTo() {
        NitriteId newIdResult = NitriteId.newId();
        assertEquals(-1, newIdResult.compareTo(NitriteId.newId()));
    }

    @Test
    public void testCompareTo2() {
        NitriteId newIdResult = NitriteId.newId();
        assertEquals(1, newIdResult.compareTo(NitriteId.createId("42")));
    }

    @Test
    public void testEquals() {
        assertFalse(NitriteId.newId().equals("42"));
    }

    @Test
    public void testEquals2() {
        NitriteId newIdResult = NitriteId.newId();
        assertFalse(newIdResult.equals(NitriteId.newId()));
    }

    @Test
    public void testEquals3() {
        NitriteId createIdResult = NitriteId.createId("42");
        assertTrue(createIdResult.equals(NitriteId.createId("42")));
    }
}

