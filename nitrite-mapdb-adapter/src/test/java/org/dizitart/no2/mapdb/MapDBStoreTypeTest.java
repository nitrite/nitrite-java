package org.dizitart.no2.mapdb;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MapDBStoreTypeTest {
    @Test
    public void testValueOf() {
        assertEquals(MapDBStoreType.ByteArray, MapDBStoreType.valueOf("ByteArray"));
    }

    @Test
    public void testValues() {
        assertEquals(5, MapDBStoreType.values().length);
    }
}

