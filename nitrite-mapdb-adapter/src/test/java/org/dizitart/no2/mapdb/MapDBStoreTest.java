package org.dizitart.no2.mapdb;

import org.junit.Test;

import static org.junit.Assert.*;

public class MapDBStoreTest {
    @Test
    public void testConstructor() {
        MapDBStore actualMapDBStore = new MapDBStore();
        assertNull(actualMapDBStore.getStoreConfig());
        assertEquals("MapDB/3.0.8", actualMapDBStore.getStoreVersion());
        assertFalse(actualMapDBStore.hasUnsavedChanges());
        assertTrue(actualMapDBStore.isClosed());
    }

    @Test
    public void testIsClosed() {
        assertTrue((new MapDBStore()).isClosed());
    }

    @Test
    public void testHasUnsavedChanges() {
        assertFalse((new MapDBStore()).hasUnsavedChanges());
    }

    @Test
    public void testGetStoreVersion() {
        assertEquals("MapDB/3.0.8", (new MapDBStore()).getStoreVersion());
    }
}

