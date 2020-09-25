package org.dizitart.no2.mapdb;

import org.junit.Test;

import static org.junit.Assert.*;

public class MapDBModuleTest {
    @Test
    public void testWithConfig() {
        MapDBModuleBuilder actualWithConfigResult = MapDBModule.withConfig();
        assertNull(actualWithConfigResult.isThreadSafe());
        assertNull(actualWithConfigResult.volume());
        assertFalse(actualWithConfigResult.readOnly());
    }

    @Test
    public void testGetStore() {
        assertTrue((new MapDBModule("path")).getStore() instanceof MapDBStore);
    }

    @Test
    public void testPlugins() {
        assertEquals(1, (new MapDBModule("path")).plugins().size());
    }
}

