package org.dizitart.no2.collection.operation;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.store.memory.InMemoryMap;
import org.junit.Test;

public class ReadOperationsTest {
    @Test
    public void testFind() {
        InMemoryMap<NitriteId, Document> nitriteMap = new InMemoryMap<NitriteId, Document>("mapName", null);
        assertTrue((new ReadOperations("collectionName", new NitriteConfig(), nitriteMap, null)).find().isEmpty());
    }

    @Test
    public void testFind2() {
        InMemoryMap<NitriteId, Document> nitriteMap = new InMemoryMap<NitriteId, Document>("mapName", null);
        assertTrue((new ReadOperations("collectionName", new NitriteConfig(), nitriteMap, null)).find(null).isEmpty());
    }

    @Test
    public void testGetById() {
        InMemoryMap<NitriteId, Document> nitriteMap = new InMemoryMap<NitriteId, Document>("mapName", null);
        ReadOperations readOperations = new ReadOperations("collectionName", new NitriteConfig(), nitriteMap, null);
        assertNull(readOperations.getById(NitriteId.newId()));
    }
}

