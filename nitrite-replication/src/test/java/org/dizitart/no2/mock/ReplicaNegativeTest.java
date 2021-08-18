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

package org.dizitart.no2.mock;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.Retry;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.concurrent.ThreadPoolManager;
import org.dizitart.no2.mock.server.MockDataGateServer;
import org.dizitart.no2.mock.server.MockRepository;
import org.dizitart.no2.mock.server.ServerLastWriteWinMap;
import org.dizitart.no2.sync.Replica;
import org.dizitart.no2.sync.ReplicationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.ExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.dizitart.no2.TestUtils.createDb;
import static org.dizitart.no2.TestUtils.randomDocument;
import static org.dizitart.no2.mock.ReplicaTest.getRandomTempDbFile;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee
 */
public class ReplicaNegativeTest {
    private MockDataGateServer server;
    private String dbFile;
    private ExecutorService executorService;
    private MockRepository mockRepository;

    @Rule
    public Retry retry = new Retry(3);

    @Before
    public void setUp() throws Exception {
        dbFile = getRandomTempDbFile();
        server = new MockDataGateServer(46005);
        executorService = ThreadPoolManager.getThreadPool(2, "ReplicaNegativeTest");
        server.start();
        mockRepository = MockRepository.getInstance();
    }

    @After
    public void cleanUp() {
        if (executorService != null) {
            executorService.shutdown();
        }
        server.stop();
    }

    @Test
    public void testServerClose() {
        mockRepository.getUserMap().put("anidotnet", "abcd");

        Nitrite db1 = createDb(dbFile);

        NitriteCollection c1 = db1.getCollection("testServerClose");

        Replica r1 = Replica.builder()
            .of(c1)
            .remoteHost("127.0.0.1")
            .remotePort(46005)
            .tenant("junit-test")
            .jwtAuth("anidotnet", "abcd")
            .create();

        r1.connect();

        executorService.submit(() -> {
            for (int i = 0; i < 10; i++) {
                Document document = randomDocument();
                c1.insert(document);
            }
        });

        await().atMost(5, SECONDS).until(() -> mockRepository.getCollectionReplicaMap().size() == 1);
        assertEquals(mockRepository.getUserReplicaMap().size(), 1);
        assertTrue(mockRepository.getUserReplicaMap().containsKey("anidotnet"));
        assertTrue(mockRepository.getCollectionReplicaMap().containsKey("anidotnet@testServerClose"));
        ServerLastWriteWinMap lastWriteWinMap = mockRepository.getReplicaStore().get("anidotnet@testServerClose");

        await().atMost(5, SECONDS).until(() -> lastWriteWinMap.getCollection().find().size() == 10);
        server.stop();
        await().atMost(5, SECONDS).until(() -> !r1.isConnected());
    }

    @Test(expected = ReplicationException.class)
    public void testRemoteHostValidation() {
        Nitrite db1 = createDb(dbFile);
        NitriteCollection c1 = db1.getCollection("testServerClose");


        Replica r1 = Replica.builder()
            .of(c1)
            .remotePort(46005)
            .tenant("junit-test")
            .jwtAuth("anidotnet", "abcd")
            .create();
    }

    @Test(expected = ReplicationException.class)
    public void testRemotePortValidation() {
        Nitrite db1 = createDb(dbFile);
        NitriteCollection c1 = db1.getCollection("testServerClose");


        Replica r1 = Replica.builder()
            .of(c1)
            .remoteHost("127.0.0.1")
            .remotePort(null)
            .tenant("junit-test")
            .jwtAuth("anidotnet", "abcd")
            .create();
    }

    @Test(expected = ReplicationException.class)
    public void testTenantValidation() {
        Nitrite db1 = createDb(dbFile);
        NitriteCollection c1 = db1.getCollection("testServerClose");


        Replica r1 = Replica.builder()
            .of(c1)
            .remoteHost("127.0.0.1")
            .remotePort(46005)
            .jwtAuth("anidotnet", "abcd")
            .create();
    }

    @Test(expected = ReplicationException.class)
    public void testCollectionValidation() {
        Nitrite db1 = createDb(dbFile);
        NitriteCollection c1 = db1.getCollection("testServerClose");


        Replica r1 = Replica.builder()
            .remoteHost("127.0.0.1")
            .remotePort(46005)
            .tenant("junit-test")
            .jwtAuth("anidotnet", "abcd")
            .create();
    }

    @Test(expected = ReplicationException.class)
    public void testUserValidation() {
        Nitrite db1 = createDb(dbFile);
        NitriteCollection c1 = db1.getCollection("testServerClose");


        Replica r1 = Replica.builder()
            .of(c1)
            .remoteHost("127.0.0.1")
            .remotePort(46005)
            .tenant("junit-test")
            .jwtAuth("", "abcd")
            .create();
    }
}
