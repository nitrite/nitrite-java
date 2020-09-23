package org.dizitart.no2.collection.operation;

import static org.junit.Assert.assertTrue;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.UpdateOptions;
import org.dizitart.no2.store.memory.InMemoryMap;
import org.junit.Test;

public class WriteOperationsTest {
    @Test
    public void testUpdate() {
        InMemoryMap<NitriteId, Document> nitriteMap = new InMemoryMap<NitriteId, Document>("mapName", null);
        ReadOperations readOperations = new ReadOperations("collectionName", new NitriteConfig(), nitriteMap, null);
        WriteOperations writeOperations = new WriteOperations(null, readOperations,
            new InMemoryMap<NitriteId, Document>("mapName", null), null);
        UpdateOptions updateOptions = UpdateOptions.updateOptions(true);
        assertTrue(writeOperations.update(null, Document.createDocument(), updateOptions) instanceof WriteResultImpl);
    }

    @Test
    public void testRemove() {
        InMemoryMap<NitriteId, Document> nitriteMap = new InMemoryMap<NitriteId, Document>("mapName", null);
        ReadOperations readOperations = new ReadOperations("collectionName", new NitriteConfig(), nitriteMap, null);
        assertTrue((new WriteOperations(null, readOperations, new InMemoryMap<NitriteId, Document>("mapName", null), null))
            .remove(null, true) instanceof WriteResultImpl);
    }
}

