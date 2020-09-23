package org.dizitart.no2.collection;

import org.dizitart.no2.exceptions.InvalidIdException;
import org.junit.Test;

import static org.junit.Assert.*;

public class NitriteIdTest {
    @Test
    public void testValidId() {
        assertThrows(InvalidIdException.class, () -> NitriteId.validId("value"));
        assertTrue(NitriteId.validId(42));
    }

    @Test
    public void testCompareTo() {
        NitriteId newIdResult = NitriteId.newId();
        assertEquals(-1, newIdResult.compareTo(NitriteId.newId()));
    }
}

