package org.dizitart.no2.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.dizitart.no2.collection.Document;
import org.junit.Test;

public class DatabaseMetaDataTest {
    @Test
    public void testConstructor() {
        DatabaseMetaData actualDatabaseMetaData = new DatabaseMetaData(Document.createDocument());
        assertNull(actualDatabaseMetaData.getStoreVersion());
        assertNull(actualDatabaseMetaData.getSchemaVersion());
        assertNull(actualDatabaseMetaData.getNitriteVersion());
        assertNull(actualDatabaseMetaData.getCreateTime());
    }

    @Test
    public void testGetInfo() {
        DatabaseMetaData databaseMetaData = new DatabaseMetaData();
        databaseMetaData.setNitriteVersion("1.0.2");
        assertEquals(4, databaseMetaData.getInfo().size());
    }

    @Test
    public void testGetInfo2() {
        assertEquals(4, (new DatabaseMetaData()).getInfo().size());
    }
}

