package org.dizitart.no2.store;

import org.dizitart.no2.Document;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

import static org.dizitart.no2.DbTestOperations.getRandomTempDbFile;

/**
 * @author Anindya Chatterjee.
 */
public class NitriteMapStressTest {
    private String dbPath = getRandomTempDbFile();

    @Test
    public void testWithInsertReadUpdate() throws IOException {
        MVStore store = MVStore.open(dbPath);
        MVMap<String, Document> map = store.openMap("map-test");
        NitriteStore nitriteStore = new NitriteMVStore(store);
        NitriteMap<String, Document> nitriteMap = new NitriteMVMap<>(map, nitriteStore);

        int count = 10000;
        for (int i = 0; i < count; i++) {
            Document record = new Document();
            record.put("firstName", UUID.randomUUID().toString());
            record.put("failed", false);
            record.put("lastName", UUID.randomUUID().toString());
            record.put("processed", false);

            nitriteMap.put(UUID.randomUUID().toString(), record);
        }

        for (Map.Entry<String, Document> entry : nitriteMap.entrySet()) {
            String key = entry.getKey();
            Document record = entry.getValue();

            record.put("processed", true);

            nitriteMap.put(key, record);
        }

        store.close();

        Files.delete(Paths.get(dbPath));
    }
}
