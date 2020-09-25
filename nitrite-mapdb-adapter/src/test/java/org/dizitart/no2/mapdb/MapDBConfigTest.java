package org.dizitart.no2.mapdb;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class MapDBConfigTest {
    @Test
    public void testConstructor() {
        MapDBConfig actualMapDBConfig = new MapDBConfig();
        assertNull(actualMapDBConfig.isThreadSafe());
        assertNull(actualMapDBConfig.volume());
        assertFalse(actualMapDBConfig.isReadOnly());
    }
}

