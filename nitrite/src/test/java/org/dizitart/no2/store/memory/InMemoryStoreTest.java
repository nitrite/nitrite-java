package org.dizitart.no2.store.memory;

import org.dizitart.no2.store.NitriteMap;
import org.junit.Test;

import static org.dizitart.no2.common.Constants.NITRITE_VERSION;
import static org.junit.Assert.*;

public class InMemoryStoreTest {
    @Test
    public void testConstructor() {
        assertFalse((new InMemoryStore()).isClosed());
    }

    @Test
    public void testHasUnsavedChanges() {
        assertFalse((new InMemoryStore()).hasUnsavedChanges());
    }

    @Test
    public void testIsReadOnly() {
        assertFalse((new InMemoryStore()).isReadOnly());
    }

    @Test
    public void testClose() throws Exception {
        InMemoryStore inMemoryStore = new InMemoryStore();
        inMemoryStore.close();
        assertTrue(inMemoryStore.isClosed());
    }

    @Test
    public void testHasMap() {
        assertFalse((new InMemoryStore()).hasMap("mapName"));
    }

    @Test
    public void testOpenMap() {
        Class<?> keyType = Object.class;
        Class<?> valueType = Object.class;
        NitriteMap<Object, Object> actualOpenMapResult = (new InMemoryStore()).openMap("mapName", keyType,
            valueType);
        assertEquals("mapName", actualOpenMapResult.getName());
        assertEquals(0L, actualOpenMapResult.size());
        assertNull(actualOpenMapResult.getAttributes());
    }

    @Test
    public void testOpenRTree() {
        Class<?> keyType = Object.class;
        Class<?> valueType = Object.class;
        assertEquals(0L, (new InMemoryStore()).openRTree("rTreeName", keyType, valueType).size());
    }

    @Test
    public void testGetStoreVersion() {
        String actualStoreVersion = (new InMemoryStore()).getStoreVersion();
        assertEquals(String.join("", "InMemory/", NITRITE_VERSION), actualStoreVersion);
    }
}

