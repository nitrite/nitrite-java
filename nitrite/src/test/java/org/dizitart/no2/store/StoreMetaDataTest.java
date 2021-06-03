package org.dizitart.no2.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.dizitart.no2.collection.Document;
import org.junit.Test;

public class StoreMetaDataTest {
    @Test
    public void testConstructor() {
        StoreMetaData actualStoreMetaData = new StoreMetaData(Document.createDocument());
        assertNull(actualStoreMetaData.getStoreVersion());
        assertNull(actualStoreMetaData.getSchemaVersion());
        assertNull(actualStoreMetaData.getNitriteVersion());
        assertNull(actualStoreMetaData.getCreateTime());
    }

    @Test
    public void testGetInfo() {
        StoreMetaData storeMetaData = new StoreMetaData();
        storeMetaData.setNitriteVersion("1.0.2");
        assertEquals(4, storeMetaData.getInfo().size());
    }

    @Test
    public void testGetInfo2() {
        assertEquals(4, (new StoreMetaData()).getInfo().size());
    }
}

