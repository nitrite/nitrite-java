package org.dizitart.no2.common.util;

import com.fasterxml.jackson.databind.type.ClassKey;
import org.apache.commons.lang3.mutable.MutableByte;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ComparablesTest {
    @Test
    public void testCompare() {
        MutableByte first = new MutableByte();
        assertEquals(0, Comparables.compare(first, new MutableByte()));
    }

    @Test
    public void testCompare2() {
        ClassKey first = new ClassKey(Object.class);
        assertEquals(0, Comparables.compare(first, new ClassKey(Object.class)));
    }

    @Test
    public void testCompare3() {
        MutableDouble second = new MutableDouble(10.0);
        assertEquals(-1, Comparables.compare(new MutableByte(), second));
    }

    @Test
    public void testCompare4() {
        MutableByte first = new MutableByte();
        assertEquals(0, Comparables.compare(first, new MutableDouble()));
    }
}

