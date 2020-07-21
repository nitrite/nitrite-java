package org.dizitart.no2.leveldb;

import com.github.javafaker.Faker;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.StoreConfig;
import org.dizitart.no2.store.events.StoreEventListener;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

public class LevelDBStoreTest {

    @Test
    public void multiMapTests() throws ExecutionException, InterruptedException {
        try(LevelDBStore store = new LevelDBStore()) {
            StoreConfig config = new StoreConfig() {
                @Override
                public String getFilePath() {
                    return "D:\\personal\\db";
                }

                @Override
                public boolean isReadOnly() {
                    return false;
                }

                @Override
                public void addStoreEventListener(StoreEventListener listener) {

                }
            };
            store.openOrCreate("", "", config);
            Faker faker = new Faker();

            assertEquals(store.getCollectionNames().size(), 0);

            ExecutorService pool = Executors.newCachedThreadPool();
            Future<?> future1 = pool.submit(() -> {
                NitriteMap<String, NitriteId> indexMap = store.openMap("IndexMap");

                for (int i = 0; i < 100; i++) {
                    try {
                        String key = faker.funnyName().name();
                        NitriteId id = NitriteId.newId();

                        indexMap.put(key, id);
                        Thread.sleep(20);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            Future<?> future2 = pool.submit(() -> {
                NitriteMap<NitriteId, Document> collection = store.openMap("Collection");

                for (int i = 0; i < 110; i++) {
                    try {
                        NitriteId id = NitriteId.newId();
                        Document document = Document.createDocument("name", faker.funnyName().name());

                        collection.put(id, document);
                        Thread.sleep(20);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            future1.get();
            future2.get();

            NitriteMap<NitriteId, Document> collection = store.openMap("Collection");
            NitriteMap<String, NitriteId> indexMap = store.openMap("IndexMap");

            assertEquals(collection.size(), 110);
//            assertEquals(indexMap.size(), 100);

            System.out.println("Collections Contents");
            int i = 1;
            for (KeyValuePair<NitriteId, Document> entry : collection.entries()) {
                System.out.println(i + ". Key - " + entry.getKey() + " : Value - " + entry.getValue());
                i++;
            }

            System.out.println("Index Contents");
            int j = 1;
            for (KeyValuePair<String, NitriteId> entry : indexMap.entries()) {
                System.out.println(j + ". Value - " + entry.getKey() + " : Key - " + entry.getValue());
                j++;
            }
        }
    }
}
