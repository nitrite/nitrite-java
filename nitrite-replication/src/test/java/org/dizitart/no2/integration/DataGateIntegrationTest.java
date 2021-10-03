/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.integration;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.Retry;
import org.dizitart.no2.TestUtils;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.sync.Replica;
import org.dizitart.no2.sync.event.ReplicationEvent;
import org.dizitart.no2.sync.event.ReplicationEventType;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.shaded.org.bouncycastle.util.Objects;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.dizitart.no2.TestUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class DataGateIntegrationTest {
    private final Network network = Network.newNetwork();
    private String dbFile1, dbFile2;
    private Nitrite db1, db2;
    private GenericContainer<?> datagate;
    private MongoDBContainer mongodb;
    private GenericContainer<?> mongoSeed;

    @Rule(order = 0)
    public Retry retry = new Retry(3);

    @Before
    public void setUp() throws Exception {
        Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(log);

        mongodb = new MongoDBContainer(
            DockerImageName.parse("mongo:latest"))
            .withNetwork(network)
            .withNetworkAliases("mongo")
            .withExposedPorts(27017);

        mongoSeed = new GenericContainer<>(new ImageFromDockerfile()
            .withFileFromClasspath("appConfig.json", "mongo-seed/appConfig.json")
            .withFileFromClasspath("Dockerfile", "mongo-seed/Dockerfile"))
            .withNetwork(network);

        mongodb.start();
        mongoSeed.start();

        datagate = new GenericContainer<>(
            DockerImageName.parse("nitrite/nitrite-datagate:latest"))
            .withEnv("MONGO_URL", "mongodb://mongo:27017/datagate")
            .withImagePullPolicy(imageName -> false)
            .withNetwork(network)
            .withLogConsumer(logConsumer)
            .withExposedPorts(46005);

        datagate.start();

        UserClient.createUser(datagate.getHost(), datagate.getFirstMappedPort(), "abcd@gmail.com");
        UserClient.createUser(datagate.getHost(), datagate.getFirstMappedPort(), "abcd2@gmail.com");
        UserClient.createUser(datagate.getHost(), datagate.getFirstMappedPort(), "abcd3@gmail.com");
    }

    @After
    public void cleanUp() {
        mongodb.stop();
        mongoSeed.stop();
        datagate.stop();

        if (db1 != null && dbFile1 != null) {
            if (!db1.isClosed()) {
                db1.close();
            }
            TestUtils.deleteDb(dbFile1);
        }

        if (db2 != null && dbFile2 != null) {
            if (!db2.isClosed()) {
                db2.close();
            }
            TestUtils.deleteDb(dbFile2);
        }
    }

    @Test
    public void testSingleUserSingleReplica() throws Exception {
        String host = datagate.getHost();
        Integer port = datagate.getMappedPort(46005);

        String jwt = UserClient.getToken(host, port, "abcd@gmail.com");

        db1 = createDb(dbFile1);
        NitriteCollection c1 = db1.getCollection("testSingleUserSingleReplica");

        Replica replica = Replica.builder()
            .database(db1)
            .of(c1)
            .remoteHost(host)
            .remotePort(port)
            .tenant("integration-test")
            .jwtAuth("abcd@gmail.com", jwt)
            .create();

        replica.connect();

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

        await().atMost(5, SECONDS).until(() -> c1.size() == 10);
        c1.remove(Filter.ALL);
        await().atMost(5, SECONDS).until(() -> c1.size() == 0);
        replica.disconnectNow();
    }

    @Test
    public void testSingleUserMultiReplica() throws Exception {
        dbFile1 = getRandomTempDbFile();
        dbFile2 = getRandomTempDbFile();

        String host = datagate.getHost();
        Integer port = datagate.getMappedPort(46005);

        db1 = createDb(dbFile1);
        db2 = createDb(dbFile2);

        NitriteCollection c1 = db1.getCollection("testSingleUserMultiReplica");
        NitriteCollection c2 = db2.getCollection("testSingleUserMultiReplica");

        String jwt = UserClient.getToken(host, port, "abcd@gmail.com");

        Replica r1 = Replica.builder()
            .database(db1)
            .of(c1)
            .remoteHost(host)
            .remotePort(port)
            .tenant("integration-test")
            .jwtAuth("abcd@gmail.com", jwt)
            .replicaName("r1")
            .acceptAllCertificates(true)
            .create();

        Replica r2 = Replica.builder()
            .database(db2)
            .of(c2)
            .remoteHost(host)
            .remotePort(port)
            .tenant("integration-test")
            .jwtAuth("abcd@gmail.com", jwt)
            .replicaName("r2")
            .acceptAllCertificates(true)
            .create();

        r1.connect();

        for (int i = 0; i < 10; i++) {
            Document document = randomDocument();
            c1.insert(document);
        }

        await().atMost(5, SECONDS).until(() -> c1.size() == 10);
        assertEquals(c2.size(), 0);

        r2.connect();
        await().atMost(15, SECONDS).until(() -> c2.size() == 10);

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

        for (int i = 0; i < 20; i++) {
            Document document = randomDocument();
            c2.insert(document);
            try {
                Thread.sleep(random.nextInt(100));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        await().atMost(10, SECONDS).until(() -> c1.size() == 40);
        assertEquals(c2.size(), 40);

        r1.disconnect();

        for (int i = 0; i < 10; i++) {
            Document document = randomDocument();
            c1.insert(document);
            try {
                Thread.sleep(random.nextInt(100));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < 20; i++) {
            Document document = randomDocument();
            c2.insert(document);
            try {
                Thread.sleep(random.nextInt(100));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        r1.connect();
        await().atMost(10, SECONDS).until(() -> c1.size() == 70 && c2.size() == 70);
        TestUtils.assertEquals(c1, c2);

        c2.remove(Filter.ALL);

        await().atMost(10, SECONDS).until(() -> c2.size() == 0);

        await().atMost(30, SECONDS).until(() -> c1.size() == 0);
        TestUtils.assertEquals(c1, c2);

        r1.disconnectNow();
        r2.disconnectNow();
    }

    @Test
    public void testMultiUserSingleReplica() throws Exception {
        String host = datagate.getHost();
        Integer port = datagate.getMappedPort(46005);

        String jwt1 = UserClient.getToken(host, port, "abcd@gmail.com");
        String jwt2 = UserClient.getToken(host, port, "abcd2@gmail.com");
        String jwt3 = UserClient.getToken(host, port, "abcd3@gmail.com");

        Nitrite db1 = createDb();
        NitriteCollection c1 = db1.getCollection("testMultiUserSingleReplica");

        Nitrite db2 = createDb();
        NitriteCollection c2 = db2.getCollection("testMultiUserSingleReplica");

        Nitrite db3 = createDb();
        NitriteCollection c3 = db3.getCollection("testMultiUserSingleReplica");

        Replica r1 = Replica.builder()
            .database(db1)
            .of(c1)
            .remoteHost(host)
            .remotePort(port)
            .tenant("junit-test")
            .jwtAuth("abcd@gmail.com", jwt1)
            .create();
        r1.connect();

        Replica r2 = Replica.builder()
            .database(db2)
            .of(c2)
            .remoteHost(host)
            .remotePort(port)
            .tenant("junit-test")
            .jwtAuth("abcd2@gmail.com", jwt2)
            .create();
        r2.connect();

        Replica r3 = Replica.builder()
            .database(db3)
            .of(c3)
            .remoteHost(host)
            .remotePort(port)
            .tenant("junit-test")
            .jwtAuth("abcd3@gmail.com", jwt3)
            .create();
        r3.connect();

        for (int i = 0; i < 10; i++) {
            Document document = randomDocument();
            c1.insert(document);
        }

        for (int i = 0; i < 20; i++) {
            Document document = randomDocument();
            c2.insert(document);
        }

        for (int i = 0; i < 30; i++) {
            Document document = randomDocument();
            c3.insert(document);
        }

        await().atMost(5, SECONDS).until(() -> c1.size() == 10 && c2.size() == 20 && c3.size() == 30);

        TestUtils.assertNotEquals(c1, c2);
        TestUtils.assertNotEquals(c1, c3);
        TestUtils.assertNotEquals(c2, c3);

        r1.disconnectNow();
        r2.disconnectNow();
        r3.disconnectNow();
    }

    @Test
    public void testMultiUserMultiReplica() throws Exception {
        String host = datagate.getHost();
        Integer port = datagate.getMappedPort(46005);

        String jwt1 = UserClient.getToken(host, port, "abcd@gmail.com");
        String jwt2 = UserClient.getToken(host, port, "abcd2@gmail.com");

        Nitrite db1 = createDb();
        NitriteCollection c1 = db1.getCollection("testMultiUserSingleReplica1");

        Nitrite db2 = createDb();
        NitriteCollection c2 = db2.getCollection("testMultiUserSingleReplica2");

        Replica r1 = Replica.builder()
            .database(db1)
            .of(c1)
            .remoteHost(host)
            .remotePort(port)
            .tenant("junit-test")
            .jwtAuth("abcd@gmail.com", jwt1)
            .create();
        r1.connect();

        Replica r2 = Replica.builder()
            .database(db2)
            .of(c2)
            .remoteHost(host)
            .remotePort(port)
            .tenant("junit-test")
            .jwtAuth("abcd2@gmail.com", jwt2)
            .create();
        r2.connect();

        for (int i = 0; i < 10; i++) {
            Document document = randomDocument();
            c1.insert(document);
        }

        for (int i = 0; i < 20; i++) {
            Document document = randomDocument();
            c2.insert(document);
        }

        await().atMost(5, SECONDS).until(() -> c1.size() == 10 && c2.size() == 20);

        TestUtils.assertNotEquals(c1, c2);
        r1.disconnectNow();
        r2.disconnectNow();
    }

    @Test
    public void testSecurityInCorrectCredentials() {
        dbFile1 = getRandomTempDbFile();

        String host = datagate.getHost();
        Integer port = datagate.getMappedPort(46005);

        db1 = createDb(dbFile1);
        NitriteCollection c1 = db1.getCollection("testSecurityInCorrectCredentials");

        AtomicReference<ReplicationEvent> errorEvent = new AtomicReference<>();

        Replica r1 = Replica.builder()
            .database(db1)
            .of(c1)
            .remoteHost(host)
            .remotePort(port)
            .tenant("junit-test")
            .jwtAuth("abcd@gmail.com", "wrong_token")
            .addReplicationEventListener(event -> {
                if (event.getEventType() == ReplicationEventType.Error && errorEvent.get() == null) {
                    errorEvent.set(event);
                }
            })
            .create();
        r1.connect();

        for (int i = 0; i < 10; i++) {
            Document document = randomDocument();
            c1.insert(document);
        }

        assertEquals(c1.size(), 10);

        await().atMost(5, SECONDS).until(() -> {
            ReplicationEvent replicationEvent = errorEvent.get();
            return replicationEvent.getError().getMessage().contains("failed to validate token");
        });
        r1.disconnectNow();
    }

    @Test
    public void testCloseDbAndReconnect() throws Exception {
        String host = datagate.getHost();
        Integer port = datagate.getMappedPort(46005);

        String jwt = UserClient.getToken(host, port, "abcd@gmail.com");

        dbFile1 = getRandomTempDbFile();
        dbFile2 = getRandomTempDbFile();

        db1 = createDb(dbFile1);
        db2 = createDb(dbFile2);

        NitriteCollection c1 = db1.getCollection("testCloseDbAndReconnect");
        NitriteCollection c2 = db2.getCollection("testCloseDbAndReconnect");

        Replica r1 = Replica.builder()
            .database(db1)
            .of(c1)
            .remoteHost(host)
            .remotePort(port)
            .tenant("junit-test")
            .jwtAuth("abcd@gmail.com", jwt)
            .replicaName("r1")
            .create();

        Replica r2 = Replica.builder()
            .database(db2)
            .of(c2)
            .remoteHost(host)
            .remotePort(port)
            .tenant("junit-test")
            .jwtAuth("abcd@gmail.com", jwt)
            .replicaName("r2")
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
        }

        for (int i = 0; i < 20; i++) {
            Document document = randomDocument();
            c2.insert(document);
            try {
                Thread.sleep(random.nextInt(100));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        NitriteCollection finalC2 = c1;
        await().atMost(10, SECONDS).until(() -> finalC2.size() == 40);
        assertEquals(c2.size(), 40);

        r1.disconnect();
        r1.close();
        db1.close();

        db1 = createDb(dbFile1);
        c1 = db1.getCollection("testCloseDbAndReconnect");
        r1 = Replica.builder()
            .database(db1)
            .of(c1)
            .remoteHost(host)
            .remotePort(port)
            .tenant("junit-test")
            .jwtAuth("abcd@gmail.com", jwt)
            .replicaName("r1")
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

        for (int i = 0; i < 20; i++) {
            Document document = randomDocument();
            c2.insert(document);
            try {
                Thread.sleep(random.nextInt(100));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        r1.connect();
        NitriteCollection finalC = c1;
        await().atMost(10, SECONDS).until(() -> finalC.size() == 70 && c2.size() == 70);
        TestUtils.assertEquals(c1, c2);

        c2.remove(Filter.ALL);

        await().atMost(10, SECONDS).until(() -> finalC.size() == 0);
        TestUtils.assertEquals(c1, c2);

        r1.disconnectNow();
        r2.disconnectNow();
    }

    @Test
    public void testDelayedConnect() throws Exception {
        String host = datagate.getHost();
        Integer port = datagate.getMappedPort(46005);

        String jwt = UserClient.getToken(host, port, "abcd@gmail.com");

        dbFile1 = getRandomTempDbFile();

        db1 = createDb(dbFile1);

        NitriteCollection c1 = db1.getCollection("testDelayedConnect");
        Replica r1 = Replica.builder()
            .database(db1)
            .of(c1)
            .remoteHost(host)
            .remotePort(port)
            .tenant("junit-test")
            .jwtAuth("abcd@gmail.com", jwt)
            .create();

        r1.connect();

        for (int i = 0; i < 10; i++) {
            Document document = randomDocument();
            c1.insert(document);
        }

        // allow it to reach the datagate server
        Thread.sleep(5000);

        r1.disconnect();
        r1.close();
        db1.close();

        Nitrite db2 = createDb();
        NitriteCollection c2 = db2.getCollection("testDelayedConnect");
        Replica r2 = Replica.builder()
            .database(db2)
            .of(c2)
            .remoteHost(host)
            .remotePort(port)
            .tenant("junit-test")
            .jwtAuth("abcd@gmail.com", jwt)
            .create();
        r2.connect();
        await().atMost(5, SECONDS).until(() -> c2.size() == 10);

        r1.disconnectNow();
        r2.disconnectNow();
    }

    @Test
    public void testDelayedConnectRemoveAll() throws Exception {
        String host = datagate.getHost();
        Integer port = datagate.getMappedPort(46005);

        String jwt = UserClient.getToken(host, port, "abcd@gmail.com");

        dbFile1 = getRandomTempDbFile();

        db1 = createDb(dbFile1);
        NitriteCollection c1 = db1.getCollection("testDelayedConnect");
        Replica r1 = Replica.builder()
            .database(db1)
            .of(c1)
            .remoteHost(host)
            .remotePort(port)
            .tenant("junit-test")
            .jwtAuth("abcd@gmail.com", jwt)
            .replicaName("r1")
            .create();

        r1.connect();

        for (int i = 0; i < 10; i++) {
            Document document = randomDocument();
            c1.insert(document);
        }

        c1.remove(Filter.ALL);
        assertEquals(c1.size(), 0);

        r1.disconnect();
        r1.close();
        db1.close();

        Nitrite db2 = createDb();
        NitriteCollection c2 = db2.getCollection("testDelayedConnect");
        Replica r2 = Replica.builder()
            .database(db2)
            .of(c2)
            .remoteHost(host)
            .remotePort(port)
            .tenant("junit-test")
            .jwtAuth("abcd@gmail.com", jwt)
            .replicaName("r2")
            .create();

        r2.connect();

        for (int i = 0; i < 5; i++) {
            Document document = randomDocument();
            c2.insert(document);
        }

        db1 = createDb(dbFile1);
        NitriteCollection c3 = db1.getCollection("testDelayedConnect");
        r1 = Replica.builder()
            .database(db1)
            .of(c3)
            .remoteHost(host)
            .remotePort(port)
            .tenant("junit-test")
            .jwtAuth("abcd@gmail.com", jwt)
            .replicaName("r1")
            .create();

        r1.connect();

        await().atMost(5, SECONDS).until(() -> {
            List<Document> l1 = c3.find().toList().stream().map(TestUtils::trimMeta).collect(Collectors.toList());
            List<Document> l2 = c2.find().toList().stream().map(TestUtils::trimMeta).collect(Collectors.toList());
            return l1.equals(l2);
        });

        r1.disconnectNow();
        r2.disconnectNow();
    }

    @Test
    public void testHighestRevisionWin() throws Exception {
        String host = datagate.getHost();
        Integer port = datagate.getMappedPort(46005);

        String jwt = UserClient.getToken(host, port, "abcd@gmail.com");

        dbFile1 = getRandomTempDbFile();
        dbFile2 = getRandomTempDbFile();

        db1 = createDb(dbFile1);
        db2 = createDb(dbFile2);

        NitriteCollection c1 = db1.getCollection("testHighestRevisionWin");
        NitriteCollection c2 = db2.getCollection("testHighestRevisionWin");

        Replica r1 = Replica.builder()
            .database(db1)
            .of(c1)
            .remoteHost(host)
            .remotePort(port)
            .tenant("junit-test")
            .jwtAuth("abcd@gmail.com", jwt)
            .replicaName("r1")
            .create();

        Replica r2 = Replica.builder()
            .database(db2)
            .of(c2)
            .remoteHost(host)
            .remotePort(port)
            .tenant("junit-test")
            .jwtAuth("abcd@gmail.com", jwt)
            .replicaName("r2")
            .create();

        r1.connect();
        r2.connect();

        Document document = randomDocument();
        c1.insert(document);

        await().atMost(5, SECONDS).until(() -> c2.size() == 1);

        Document d2 = c2.find().firstOrNull();
        d2.put("age", 38);

        assertTrue(Objects.areEqual(c1.find().firstOrNull().getRevision(), 1));
        assertNotEquals(c1.find().firstOrNull().get("age"), d2.get("age"));

        c2.update(d2);

        await().atMost(5, SECONDS).until(() -> {
            Document d1 = c1.find().firstOrNull();
            return Objects.areEqual(d1.get("age"), 38) && Objects.areEqual(d1.getRevision(), 2);
        });
    }

    @Test
    public void testRandomInsertDelete() throws Exception {
        String host = datagate.getHost();
        Integer port = datagate.getMappedPort(46005);

        String jwt = UserClient.getToken(host, port, "abcd@gmail.com");

        dbFile1 = getRandomTempDbFile();
        dbFile2 = getRandomTempDbFile();

        db1 = createDb(dbFile1);
        db2 = createDb(dbFile2);

        NitriteCollection c1 = db1.getCollection("testRandomInsertDelete");
        NitriteCollection c2 = db2.getCollection("testRandomInsertDelete");

        Replica r1 = Replica.builder()
            .database(db1)
            .of(c1)
            .remoteHost(host)
            .remotePort(port)
            .tenant("junit-test")
            .jwtAuth("abcd@gmail.com", jwt)
            .replicaName("r1")
            .create();

        Replica r2 = Replica.builder()
            .database(db2)
            .of(c2)
            .remoteHost(host)
            .remotePort(port)
            .tenant("junit-test")
            .jwtAuth("abcd@gmail.com", jwt)
            .replicaName("r2")
            .create();

        r1.connect();
        r2.connect();

        Random random = new Random();

        // insert
        for (int i = 0; i < random.nextInt(50); i++) {
            Document document = randomDocument();
            c1.insert(document);
            Thread.sleep(random.nextInt(100));
        }

        // insert
        for (int i = 0; i < random.nextInt(30); i++) {
            Document document = randomDocument();
            c2.insert(document);
            Thread.sleep(random.nextInt(100));
        }

        // update
        DocumentCursor cursor = c1.find();
        for (Document document : cursor) {
            document.put("age", random.nextInt());
            c1.update(document);
        }

        // delete
        for (int i = 0; i < random.nextInt(30); i++) {
            Document document = c1.find().firstOrNull();
            c1.remove(document);
            Thread.sleep(random.nextInt(100));
        }

        await().atMost(20, SECONDS).until(() -> c1.size() > 0 && c1.size() == c2.size());
        System.out.println("C1 Size = " + c1.size());
        TestUtils.assertEquals(c1, c2);
    }
}
