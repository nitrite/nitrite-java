package org.dizitart.no2;

import org.dizitart.no2.util.ReflectionUtils;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.dizitart.no2.Constants.ID_PREFIX;
import static org.dizitart.no2.Constants.ID_SUFFIX;
import static org.junit.Assert.*;

public class NitriteIdTest {

    @Test
    public void testLimit() {
        NitriteId one = NitriteId.createId(Long.MAX_VALUE);
        NitriteId two = NitriteId.createId(Long.MIN_VALUE);
        assertEquals(one.compareTo(two), 1);
    }

    @Test
    public void testGetNoArg() throws IllegalAccessException {
        NitriteId nitriteId = NitriteId.newId();
        assertNotNull(nitriteId);
        assertNotNull(nitriteId.toString());
        List<Field> fields = ReflectionUtils.getFieldsUpTo(NitriteId.class, Object.class);
        for (Field f : fields) {
            if (f.getName().equals("identifier")) {
                f.setAccessible(true);
                assertNotNull(Long.parseLong(f.get(nitriteId).toString()));
                System.out.println("found id = " + Long.parseLong(f.get(nitriteId).toString()));
            }
        }

        assertNotNull(nitriteId.getIdValue());
        assertEquals(ID_PREFIX + nitriteId.getIdValue().toString() + ID_SUFFIX,
                nitriteId.toString());
    }

    @Test
    public void testGet() throws IllegalAccessException {
        NitriteId nitriteId = NitriteId.createId(1L);
        assertNotNull(nitriteId);
        assertNotNull(nitriteId.toString());

        List<Field> fields = ReflectionUtils.getFieldsUpTo(NitriteId.class, Object.class);
        for (Field f : fields) {
            if (f.getName().equals("objectId")) {
                f.setAccessible(true);
                assertNotNull(f.get(nitriteId).toString());
                System.out.println("found id = " + f.get(nitriteId).toString());
            }
        }

        assertNotNull(nitriteId.getIdValue());
        assertEquals(nitriteId.toString(), ID_PREFIX + 1 + ID_SUFFIX);
    }

    @Test
    public void testHashEquals() {
        NitriteId one = NitriteId.createId(1L);
        NitriteId two = NitriteId.createId(1L);

        assertTrue(one.equals(two));
        assertEquals(one.hashCode(), two.hashCode());

        NitriteId third = NitriteId.createId(2L);
        assertFalse(one.equals(third));
        assertNotEquals(one.hashCode(), third.hashCode());
    }

    @Test
    public void testCompare() {
        NitriteId one = NitriteId.createId(1L);
        NitriteId two = NitriteId.createId(2L);
        NitriteId three = NitriteId.createId(3L);

        assertEquals(one.compareTo(two), -1);
        assertEquals(three.compareTo(one), 1);

        one = NitriteId.createId(10L);
        two = NitriteId.createId(20L);
        assertEquals(one.compareTo(two), -1);

        one = NitriteId.newId();
        two = NitriteId.newId();

        assertFalse(one.compareTo(two) == 0);
    }
}
