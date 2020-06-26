/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.test;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.sync.Replica;
import org.dizitart.no2.sync.ReplicationTemplate;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;
import org.dizitart.no2.test.server.Repository;
import org.dizitart.no2.test.server.SimpleDataGateServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.common.util.DocumentUtils.isSimilar;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.dizitart.no2.test.TestUtils.randomDocument;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class ReplicaTest {
    private static SimpleDataGateServer server;
    private String dbFile;
    private ExecutorService executorService;
    private Repository repository;

    public static String getRandomTempDbFile() {
        String dataDir = System.getProperty("java.io.tmpdir") + File.separator + "nitrite" + File.separator + "data";
        File file = new File(dataDir);
        if (!file.exists()) {
            assertTrue(file.mkdirs());
        }
        return file.getPath() + File.separator + UUID.randomUUID().toString() + ".db";
    }

    @Before
    public void setUp() throws Exception {
        server = new SimpleDataGateServer(9090);
        server.start();
        dbFile = getRandomTempDbFile();
        executorService = Executors.newCachedThreadPool();
        repository = Repository.getInstance();
    }

    @After
    public void cleanUp() throws Exception {
        executorService.awaitTermination(2, SECONDS);
        executorService.shutdown();
        if (Files.exists(Paths.get(dbFile))) {
            Files.delete(Paths.get(dbFile));
        }
        server.stop();
    }

    @Test
    public void testSingleUserSingleReplica() {
        repository.getUserMap().put("anidotnet", "abcd");

        Nitrite db = NitriteBuilder.get()
            .filePath(dbFile)
            .openOrCreate();
        NitriteCollection collection = db.getCollection("testSingleUserSingleReplica");
        Document document = createDocument().put("firstName", "Anindya")
            .put("lastName", "Chatterjee")
            .put("address", createDocument("street", "1234 Abcd Street")
                .put("pin", 123456));
        collection.insert(document);

        Replica replica = Replica.builder()
            .of(collection)
            .remote("ws://127.0.0.1:9090/datagate/anidotnet/testSingleUserSingleReplica")
            .jwtAuth("anidotnet", "abcd")
            .create();

        replica.connect();

        await().atMost(5, SECONDS).until(() -> repository.getCollectionReplicaMap().size() == 1);
        assertEquals(repository.getUserReplicaMap().size(), 1);
        assertTrue(repository.getUserReplicaMap().containsKey("anidotnet"));
        assertTrue(repository.getCollectionReplicaMap().containsKey("anidotnet@testSingleUserSingleReplica"));
        LastWriteWinMap lastWriteWinMap = repository.getReplicaStore().get("anidotnet@testSingleUserSingleReplica");

        await().atMost(5, SECONDS).until(() -> lastWriteWinMap.getCollection().find().size() == 1);
        Document doc = lastWriteWinMap.getCollection().find(where("firstName").eq("Anindya")).firstOrNull();

        assertTrue(isSimilar(document, doc, "firstName", "lastName", "address", "pin"));

        collection.remove(doc);
        await().atMost(5, SECONDS).until(() -> lastWriteWinMap.getCollection().size() == 0);
        doc = lastWriteWinMap.getCollection().find(where("firstName").eq("Anindya")).firstOrNull();
        assertNull(doc);
        assertEquals(collection.size(), 0);

        collection.insert(document);
        await().atMost(5, SECONDS).until(() -> lastWriteWinMap.getCollection().size() == 1);
        doc = lastWriteWinMap.getCollection().find(where("firstName").eq("Anindya")).firstOrNull();
        assertTrue(isSimilar(document, doc, "firstName", "lastName", "address", "pin"));

        replica.disconnect();
        collection.remove(doc);
        await().atMost(5, SECONDS).until(() -> lastWriteWinMap.getCollection().size() == 1);
        doc = lastWriteWinMap.getCollection().find(where("firstName").eq("Anindya")).firstOrNull();
        assertTrue(isSimilar(document, doc, "firstName", "lastName", "address", "pin"));

        replica.connect();
        await().atMost(5, SECONDS).until(() -> lastWriteWinMap.getCollection().size() == 0);
        doc = lastWriteWinMap.getCollection().find(where("firstName").eq("Anindya")).firstOrNull();
        assertNull(doc);
    }

    @Test
    public void testSingleUserMultiReplica() {
        repository.getUserMap().put("anidotnet", "abcd");

        Nitrite db1 = NitriteBuilder.get()
            .filePath(dbFile)
            .openOrCreate();

        Nitrite db2 = NitriteBuilder.get()
            .openOrCreate();

        NitriteCollection c1 = db1.getCollection("testSingleUserMultiReplica");
        NitriteCollection c2 = db2.getCollection("testSingleUserMultiReplica");

        Replica r1 = Replica.builder()
            .of(c1)
            .remote("ws://127.0.0.1:9090/datagate/anidotnet/testSingleUserMultiReplica")
            .jwtAuth("anidotnet", "abcd")
            .create();

        Replica r2 = Replica.builder()
            .of(c2)
            .remote("ws://127.0.0.1:9090/datagate/anidotnet/testSingleUserMultiReplica")
            .jwtAuth("anidotnet", "abcd")
            .create();

        r1.connect();

        executorService.submit(() -> {
            for (int i = 0; i < 10; i++) {
                Document document = randomDocument();
                c1.insert(document);
            }
        });

        await().atMost(5, SECONDS).until(() -> c1.size() == 10);
        assertEquals(c2.size(), 0);

        r2.connect();
        await().atMost(5, SECONDS).until(() -> c2.size() == 10);

        Random random = new Random();
        executorService.submit(() -> {
            for (int i = 0; i < 10; i++) {
                Document document = randomDocument();
                c1.insert(document);
                try {
                    Thread.sleep(random.nextInt(100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        executorService.submit(() -> {
            for (int i = 0; i < 20; i++) {
                Document document = randomDocument();
                c2.insert(document);
                try {
                    Thread.sleep(random.nextInt(100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        await().atMost(10, SECONDS).until(() -> c1.size() == 40);
        assertEquals(c2.size(), 40);

        r1.disconnect();

        executorService.submit(() -> {
            for (int i = 0; i < 10; i++) {
                Document document = randomDocument();
                c1.insert(document);
                try {
                    Thread.sleep(random.nextInt(100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        executorService.submit(() -> {
            for (int i = 0; i < 20; i++) {
                Document document = randomDocument();
                c2.insert(document);
                try {
                    Thread.sleep(random.nextInt(100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        r1.connect();
        await().atMost(10, SECONDS).until(() -> c1.size() == 70 && c2.size() == 70);
        TestUtils.assertEquals(c1, c2);

        executorService.submit(() -> {
            c2.remove(Filter.ALL);
        });

        await().atMost(10, SECONDS).until(() -> c2.size() == 0);
        await().atMost(5, SECONDS).until(() -> c1.size() == 0);
        TestUtils.assertEquals(c1, c2);
    }

    @Test
    public void testMultiUserSingleReplica() {
        repository.getUserMap().put("user1", "abcd");
        repository.getUserMap().put("user2", "abcd");
        repository.getUserMap().put("user3", "abcd");

        Nitrite db1 = NitriteBuilder.get()
            .openOrCreate();
        NitriteCollection c1 = db1.getCollection("testMultiUserSingleReplica");

        Nitrite db2 = NitriteBuilder.get()
            .openOrCreate();
        NitriteCollection c2 = db2.getCollection("testMultiUserSingleReplica");

        Nitrite db3 = NitriteBuilder.get()
            .openOrCreate();
        NitriteCollection c3 = db3.getCollection("testMultiUserSingleReplica");

        Replica r1 = Replica.builder()
            .of(c1)
            .remote("ws://127.0.0.1:9090/datagate/user1/testSingleUserSingleReplica")
            .jwtAuth("user1", "abcd")
            .create();
        r1.connect();

        Replica r2 = Replica.builder()
            .of(c2)
            .remote("ws://127.0.0.1:9090/datagate/user2/testSingleUserSingleReplica")
            .jwtAuth("user2", "abcd")
            .create();
        r2.connect();

        Replica r3 = Replica.builder()
            .of(c3)
            .remote("ws://127.0.0.1:9090/datagate/user3/testSingleUserSingleReplica")
            .jwtAuth("user3", "abcd")
            .create();
        r3.connect();

        executorService.submit(() -> {
            for (int i = 0; i < 10; i++) {
                Document document = randomDocument();
                c1.insert(document);
            }
        });

        executorService.submit(() -> {
            for (int i = 0; i < 20; i++) {
                Document document = randomDocument();
                c2.insert(document);
            }
        });

        executorService.submit(() -> {
            for (int i = 0; i < 30; i++) {
                Document document = randomDocument();
                c3.insert(document);
            }
        });

        await().atMost(5, SECONDS).until(() -> c1.size() == 10 && c2.size() == 20 && c3.size() == 30);

        TestUtils.assertNotEquals(c1, c2);
        TestUtils.assertNotEquals(c1, c3);
        TestUtils.assertNotEquals(c2, c3);
    }

    @Test
    public void testMultiUserMultiReplica() {
        repository.getUserMap().put("user1", "abcd");
        repository.getUserMap().put("user2", "abcd");

        Nitrite db1 = NitriteBuilder.get()
            .openOrCreate();
        NitriteCollection c1 = db1.getCollection("testMultiUserSingleReplica1");

        Nitrite db2 = NitriteBuilder.get()
            .openOrCreate();
        NitriteCollection c2 = db2.getCollection("testMultiUserSingleReplica2");

        Replica r1 = Replica.builder()
            .of(c1)
            .remote("ws://127.0.0.1:9090/datagate/user1/testMultiUserSingleReplica1")
            .jwtAuth("user1", "abcd")
            .create();
        r1.connect();

        Replica r2 = Replica.builder()
            .of(c2)
            .remote("ws://127.0.0.1:9090/datagate/user2/testMultiUserSingleReplica2")
            .jwtAuth("user2", "abcd")
            .create();
        r2.connect();

        executorService.submit(() -> {
            for (int i = 0; i < 10; i++) {
                Document document = randomDocument();
                c1.insert(document);
            }
        });

        executorService.submit(() -> {
            for (int i = 0; i < 20; i++) {
                Document document = randomDocument();
                c2.insert(document);
            }
        });

        await().atMost(5, SECONDS).until(() -> c1.size() == 10 && c2.size() == 20);

        TestUtils.assertNotEquals(c1, c2);
    }

    @Test
    public void testSecurityInCorrectCredentials() {
        repository.getUserMap().put("user", "abcd");

        Nitrite db1 = NitriteBuilder.get()
            .openOrCreate();
        NitriteCollection c1 = db1.getCollection("testSecurity");

        Replica r1 = Replica.builder()
            .of(c1)
            .remote("ws://127.0.0.1:9090/datagate/user/testSecurity")
            .jwtAuth("user", "wrong_token")
            .create();
        r1.connect();

        for (int i = 0; i < 10; i++) {
            Document document = randomDocument();
            c1.insert(document);
        }

        assertEquals(c1.size(), 10);
        await().atMost(5, SECONDS).until(() -> !r1.isConnected());
    }

    @Test
    public void testCloseDbAndReconnect() {
        repository.getUserMap().put("anidotnet", "abcd");

        Nitrite db1 = NitriteBuilder.get()
            .filePath(dbFile)
            .openOrCreate();

        Nitrite db2 = NitriteBuilder.get()
            .openOrCreate();

        NitriteCollection c1 = db1.getCollection("testCloseDbAndReconnect");
        NitriteCollection c2 = db2.getCollection("testCloseDbAndReconnect");

        Replica r1 = Replica.builder()
            .of(c1)
            .remote("ws://127.0.0.1:9090/datagate/anidotnet/testCloseDbAndReconnect")
            .jwtAuth("anidotnet", "abcd")
            .create();

        Replica r2 = Replica.builder()
            .of(c2)
            .remote("ws://127.0.0.1:9090/datagate/anidotnet/testCloseDbAndReconnect")
            .jwtAuth("anidotnet", "abcd")
            .create();

        r1.connect();

        for (int i = 0; i < 10; i++) {
            Document document = randomDocument();
            c1.insert(document);
        }

        NitriteCollection finalC1 = c1;
        await().atMost(5, SECONDS).until(() -> finalC1.size() == 10);
        assertEquals(c2.size(), 0);

        r2.connect();
        await().atMost(5, SECONDS).until(() -> c2.size() == 10);

        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            Document document = randomDocument();
            c1.insert(document);
            try {
                Thread.sleep(random.nextInt(100));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        executorService.submit(() -> {
            for (int i = 0; i < 20; i++) {
                Document document = randomDocument();
                c2.insert(document);
                try {
                    Thread.sleep(random.nextInt(100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        NitriteCollection finalC2 = c1;
        await().atMost(10, SECONDS).until(() -> finalC2.size() == 40);
        assertEquals(c2.size(), 40);

        r1.disconnect();
        r1.close();
        db1.close();

        db1 = NitriteBuilder.get()
            .filePath(dbFile)
            .openOrCreate();
        c1 = db1.getCollection("testCloseDbAndReconnect");
        r1 = Replica.builder()
            .of(c1)
            .remote("ws://127.0.0.1:9090/datagate/anidotnet/testCloseDbAndReconnect")
            .jwtAuth("anidotnet", "abcd")
            .create();

        for (int i = 0; i < 10; i++) {
            Document document = randomDocument();
            c1.insert(document);
            try {
                Thread.sleep(random.nextInt(100));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        executorService.submit(() -> {
            for (int i = 0; i < 20; i++) {
                Document document = randomDocument();
                c2.insert(document);
                try {
                    Thread.sleep(random.nextInt(100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        r1.connect();
        NitriteCollection finalC = c1;
        await().atMost(10, SECONDS).until(() -> finalC.size() == 70 && c2.size() == 70);
        TestUtils.assertEquals(c1, c2);

        executorService.submit(() -> {
            c2.remove(Filter.ALL);
        });

        await().atMost(10, SECONDS).until(() -> c2.size() == 0);
        await().atMost(5, SECONDS).until(() -> finalC.size() == 0);
        TestUtils.assertEquals(c1, c2);
    }

    @Test
    public void testDelayedConnect() {
        repository.getUserMap().put("anidotnet", "abcd");

        Nitrite db1 = NitriteBuilder.get()
            .filePath(dbFile)
            .openOrCreate();
        NitriteCollection c1 = db1.getCollection("testDelayedConnect");
        Replica r1 = Replica.builder()
            .of(c1)
            .remote("ws://127.0.0.1:9090/datagate/anidotnet/testDelayedConnect")
            .jwtAuth("anidotnet", "abcd")
            .create();

        r1.connect();

        for (int i = 0; i < 10; i++) {
            Document document = randomDocument();
            c1.insert(document);
        }
        await().atMost(5, SECONDS).until(() -> c1.size() == 10);

        r1.disconnect();
        r1.close();
        db1.close();

        Nitrite db2 = NitriteBuilder.get()
            .openOrCreate();
        NitriteCollection c2 = db2.getCollection("testDelayedConnect");
        Replica r2 = Replica.builder()
            .of(c2)
            .remote("ws://127.0.0.1:9090/datagate/anidotnet/testDelayedConnect")
            .jwtAuth("anidotnet", "abcd")
            .create();
        r2.connect();
        await().atMost(5, SECONDS).until(() -> c2.size() == 10);
    }

    @Test
    public void testDelayedConnectRemoveAll() {
        repository.getUserMap().put("anidotnet", "abcd");

        Nitrite db1 = NitriteBuilder.get()
            .filePath(dbFile)
            .openOrCreate();
        NitriteCollection c1 = db1.getCollection("testDelayedConnect");
        Replica r1 = Replica.builder()
            .of(c1)
            .remote("ws://127.0.0.1:9090/datagate/anidotnet/testDelayedConnect")
            .jwtAuth("anidotnet", "abcd")
            .create();

        r1.connect();

        for (int i = 0; i < 10; i++) {
            Document document = randomDocument();
            c1.insert(document);
        }
        await().atMost(5, SECONDS).until(() -> c1.size() == 10);
        c1.remove(Filter.ALL);
        assertEquals(c1.size(), 0);

        r1.disconnect();
        r1.close();
        db1.close();

        Nitrite db2 = NitriteBuilder.get()
            .openOrCreate();
        NitriteCollection c2 = db2.getCollection("testDelayedConnect");
        Replica r2 = Replica.builder()
            .of(c2)
            .remote("ws://127.0.0.1:9090/datagate/anidotnet/testDelayedConnect")
            .jwtAuth("anidotnet", "abcd")
            .create();
        r2.connect();

        for (int i = 0; i < 5; i++) {
            Document document = randomDocument();
            c2.insert(document);
        }

        db1 = NitriteBuilder.get()
            .filePath(dbFile)
            .openOrCreate();
        NitriteCollection c3 = db1.getCollection("testDelayedConnect");
        r1 = Replica.builder()
            .of(c3)
            .remote("ws://127.0.0.1:9090/datagate/anidotnet/testDelayedConnect")
            .jwtAuth("anidotnet", "abcd")
            .create();

        r1.connect();
        await().atMost(5, SECONDS).until(() -> c3.size() == 5 && c2.size() == 5);
        TestUtils.assertEquals(c3, c2);
        LastWriteWinMap lastWriteWinMap = repository.getReplicaStore().get("anidotnet@testDelayedConnect");
        assertEquals(lastWriteWinMap.getTombstones().size(), 10);
    }

    @Test
    public void testGarbageCollect() throws InterruptedException {
        repository.getUserMap().put("anidotnet", "abcd");
        Nitrite db1 = NitriteBuilder.get()
            .filePath(dbFile)
            .openOrCreate();
        NitriteCollection c1 = db1.getCollection("testGarbageCollect");
        Replica r1 = Replica.builder()
            .of(c1)
            .remote("ws://127.0.0.1:9090/datagate/anidotnet/testGarbageCollect")
            .jwtAuth("anidotnet", "abcd")
            .create();

        r1.connect();

        for (int i = 0; i < 10; i++) {
            Document document = randomDocument();
            c1.insert(document);
        }
        await().atMost(5, SECONDS).until(() -> c1.size() == 10);
        c1.remove(Filter.ALL);
        assertEquals(c1.size(), 0);

        r1.disconnect();
        r1.close();
        db1.close();

        repository.setGcTtl(1L);

        db1 = NitriteBuilder.get()
            .filePath(dbFile)
            .openOrCreate();
        NitriteCollection c2 = db1.getCollection("testGarbageCollect");
        r1 = Replica.builder()
            .of(c2)
            .remote("ws://127.0.0.1:9090/datagate/anidotnet/testGarbageCollect")
            .jwtAuth("anidotnet", "abcd")
            .create();

        r1.connect();

        LastWriteWinMap lastWriteWinMap = getCrdt(r1);

        await().atMost(5, SECONDS).until(() -> c2.size() == 0
            && lastWriteWinMap.getTombstones().size() == 0);
    }

    @SneakyThrows
    private LastWriteWinMap getCrdt(Replica replica) {
        Field field = Replica.class.getDeclaredField("replicationTemplate");
        field.setAccessible(true);
        ReplicationTemplate replicationTemplate = (ReplicationTemplate) field.get(replica);
        return replicationTemplate.getCrdt();
    }
}
