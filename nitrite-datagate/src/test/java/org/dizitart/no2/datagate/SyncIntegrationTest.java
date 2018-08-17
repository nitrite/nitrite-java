/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.datagate;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.Document;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Cursor;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.objects.ObjectRepository;
import org.dizitart.no2.common.ExecutorServiceManager;
import org.dizitart.no2.sync.*;
import org.dizitart.no2.sync.types.UserAccount;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.dizitart.no2.Document.createDocument;
import static org.dizitart.no2.datagate.Constants.*;
import static org.dizitart.no2.datagate.DbTestOperations.getRandomTempDbFile;
import static org.dizitart.no2.filters.ObjectFilters.eq;
import static org.dizitart.no2.sync.TimeSpan.timeSpan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SyncIntegrationTest {

    @LocalServerPort
    private int serverPort;

    @Autowired
    private Jongo jongo;

    private NitriteCollection primary;
    private NitriteCollection secondary;

    private ObjectRepository<Employee> primaryEmployeeRepository;
    private ObjectRepository<Employee> secondaryEmployeeRepository;

    private Nitrite primaryDb;
    private Nitrite secondaryDb;

    private String primaryFile;
    private String secondaryFile;

    private String clientId = "junit";
    private String clientSecret = "jun1t";

    private String userId = "user";
    private String password = "password";

    private String remoteCollection = "test-collection@" + userId;

    private DataGateSyncTemplate syncTemplate;

    @Rule
    public Retry retry = new Retry(3);

    @Before
    public void setUp() {
        primaryFile = getRandomTempDbFile();
        secondaryFile = getRandomTempDbFile();

        primaryDb = Nitrite.builder()
                .filePath(primaryFile)
                .openOrCreate();

        secondaryDb = Nitrite.builder()
                .filePath(secondaryFile)
                .openOrCreate();

        primary = primaryDb.getCollection("primary");
        secondary = secondaryDb.getCollection("secondary");

        primaryEmployeeRepository = primaryDb.getRepository(Employee.class);
        secondaryEmployeeRepository = secondaryDb.getRepository(Employee.class);

        prepareClientAuth();
        prepareUserAuth();

        DataGateClient dataGateClient = new DataGateClient("http://localhost:" + serverPort)
                .withAuth(userId, password);

        syncTemplate = new DataGateSyncTemplate(dataGateClient, remoteCollection);
    }

    private void prepareClientAuth() {
        UserAccount clientAccount = new UserAccount();
        clientAccount.setUserName(clientId);
        clientAccount.setPassword(clientSecret);
        clientAccount.setAuthorities(new String[]{AUTH_CLIENT});

        MongoCollection collection = jongo.getCollection(USER_REPO);
        collection.insert(clientAccount);
    }

    private void prepareUserAuth() {
        UserAccount userAccount = new UserAccount();
        userAccount.setUserName(userId);
        userAccount.setPassword(password);
        userAccount.setAuthorities(new String[]{AUTH_USER});
        userAccount.setCollections(new ArrayList<String>() {{
            add(remoteCollection);
        }});

        DataGateClient dataGateClient = new DataGateClient("http://localhost:" + serverPort)
                .withAuth(clientId, clientSecret);
        DataGateUserTemplate userTemplate = new DataGateUserTemplate(dataGateClient);
        userTemplate.createRemoteUser(userAccount);
    }

    @After
    public void clear() throws IOException {
        primaryDb.close();
        secondaryDb.close();

        Files.delete(Paths.get(primaryFile));
        Files.delete(Paths.get(secondaryFile));

        jongo.getCollection(remoteCollection).drop();
        jongo.getCollection(USER_REPO).remove();
    }

    @Test
    public void testDocumentSync() throws InterruptedException {
        ExecutorService worker = ExecutorServiceManager.daemonExecutor();
        final CountDownLatch latch = new CountDownLatch(2);

        SyncHandle syncHandlePrimary = Replicator.of(primaryDb)
                .forLocal(primary)
                .withSyncTemplate(syncTemplate)
                .delay(timeSpan(1, TimeUnit.SECONDS))
                .ofType(ReplicationType.BOTH_WAY)
                .withListener(new SyncEventListener() {
                    @Override
                    public void onSyncEvent(SyncEventData eventInfo) {
                        Throwable syncError = eventInfo.getError();
                        EventType syncEventType = eventInfo.getEventType();
                        assertEquals(eventInfo.getCollectionName(), "primary");
                        if (syncError != null) {
                            log.error("Sync error in " + syncEventType, syncError);
                        }
                    }
                })
                .configure();
        syncHandlePrimary.startSync();


        SyncHandle syncHandleSecondary = Replicator.of(primaryDb)
                .forLocal(secondary)
                .withSyncTemplate(syncTemplate)
                .delay(timeSpan(1, TimeUnit.SECONDS))
                .ofType(ReplicationType.BOTH_WAY)
                .withListener(new SyncEventListener() {
                    @Override
                    public void onSyncEvent(SyncEventData eventInfo) {
                        Throwable syncError = eventInfo.getError();
                        EventType syncEventType = eventInfo.getEventType();
                        assertEquals(eventInfo.getCollectionName(), "secondary");
                        if (syncError != null) {
                            log.error("Sync error in " + syncEventType, syncError);
                        }
                    }
                })
                .configure();
        syncHandleSecondary.startSync();

        worker.submit(() -> {
            // insert some data
            log.info("****************** Inserting data to primary **********************");
            for (int i = 0; i < 7; i++) {
                Document document = createDocument("pkey1", i)
                        .put("pkey2", "primary value");
                primary.insert(document);
            }

            Cursor cursor = primary.find();

            log.info("****************** Updating data to primary **********************");
            int i = 0;
            for (Document document : cursor) {
                if (i == 5) {
                    document.put("pkey2", "new primary value");
                    primary.update(document);
                }
                i++;
            }

            await().atMost(5, TimeUnit.SECONDS).until(() -> {
                Thread.sleep(2000);
                return true;
            });

            latch.countDown();
        });

        worker.submit(() -> {
            // insert some data
            log.info("****************** Inserting data to secondary **********************");
            for (int i = 0; i < 5; i++) {
                Document document = createDocument("skey1", i)
                        .put("skey2", "secondary value");
                secondary.insert(document);
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            log.info("****************** Removing data from secondary **********************");
            secondary.remove(secondary.find().firstOrDefault());
            latch.countDown();
        });

        latch.await();

        // wait for 4 sec to sync it all up
        try {
            await().atMost(5, TimeUnit.SECONDS).until(() -> primary.size() == 11 && secondary.size() == 11);
        } catch (Throwable t) {
            log.error("Primary Size = " + primary.size() + " & Secondary Size = " + secondary.size());
            log.error("\nPrimary - " + primary.find().toList() + "\nSecondary - "
                    + secondary.find().toList(), t);
            throw t;
        }

        assertEquals(primary.size(), secondary.size());
        List<Document> secondaryList = secondary.find().toList();
        List<Document> primaryList = primary.find().toList();
        for (Document document : primaryList) {
            if (!secondaryList.contains(document)) {
                fail(document + " does not exists in secondary");
            }
        }
    }

    @Test
    public void testObjectSync() throws InterruptedException {
        ExecutorService worker = ExecutorServiceManager.daemonExecutor();
        final CountDownLatch latch = new CountDownLatch(2);

        SyncHandle syncHandlePrimary = Replicator.of(primaryDb)
                .forLocal(primaryEmployeeRepository)
                .withSyncTemplate(syncTemplate)
                .delay(timeSpan(1, TimeUnit.SECONDS))
                .ofType(ReplicationType.BOTH_WAY)
                .withListener(new SyncEventListener() {
                    @Override
                    public void onSyncEvent(SyncEventData eventInfo) {
                        Throwable syncError = eventInfo.getError();
                        EventType syncEventType = eventInfo.getEventType();
                        if (syncError != null) {
                            log.error("Sync error in " + syncEventType, syncError);
                        }
                    }
                })
                .configure();
        syncHandlePrimary.startSync();

        SyncHandle syncHandleSecondary = Replicator.of(primaryDb)
                .forLocal(secondaryEmployeeRepository)
                .withSyncTemplate(syncTemplate)
                .delay(timeSpan(1, TimeUnit.SECONDS))
                .ofType(ReplicationType.BOTH_WAY)
                .withListener(new SyncEventListener() {
                    @Override
                    public void onSyncEvent(SyncEventData eventInfo) {
                        Throwable syncError = eventInfo.getError();
                        EventType syncEventType = eventInfo.getEventType();
                        if (syncError != null) {
                            log.error("Sync error in " + syncEventType, syncError);
                        }
                    }
                })
                .configure();
        syncHandleSecondary.startSync();

        worker.submit(() -> {
            // insert some data
            for (int i = 0; i < 7; i++) {
                Employee employee = DataGenerator.generateEmployee(0);
                employee.setEmpId(employee.getEmpId() + i + 10000);
                primaryEmployeeRepository.insert(employee);
            }

            org.dizitart.no2.collection.objects.Cursor<Employee> cursor = primaryEmployeeRepository.find();

            int i = 0;
            for (Employee employee : cursor) {
                if (i == 5) {
                    employee.setAddress("New Address");
                    primaryEmployeeRepository.update(eq("empId", employee.getEmpId()), employee);
                }
                i++;
            }

            await().atMost(5, TimeUnit.SECONDS).until(() -> {
                Thread.sleep(2000);
                return true;
            });

            latch.countDown();
        });

        worker.submit(() -> {
            // insert some data
            for (int i = 0; i < 5; i++) {
                Employee employee = DataGenerator.generateEmployee(0);
                employee.setEmpId(employee.getEmpId() + i + 50000);
                secondaryEmployeeRepository.insert(employee);
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            secondaryEmployeeRepository.remove(eq("empId", secondaryEmployeeRepository.find()
                    .firstOrDefault().getEmpId()));
            latch.countDown();
        });

        latch.await();

        // wait for 4 sec to sync it all up
        try {
            await().atMost(5, TimeUnit.SECONDS).until(() -> primaryEmployeeRepository.size() == 11
                && secondaryEmployeeRepository.size() == 11);
        } catch (Throwable t) {
            log.error("Primary Size = " + primaryEmployeeRepository.size() + " & Secondary Size = " + secondaryEmployeeRepository.size());
            log.error("\nPrimary - " + primaryEmployeeRepository.find().toList() + "\nSecondary - "
                    + secondaryEmployeeRepository.find().toList(), t);
            throw t;
        }

        assertEquals(primaryEmployeeRepository.size(), secondaryEmployeeRepository.size());
        List<Employee> secondaryList = secondaryEmployeeRepository.find().toList();
        List<Employee> primaryList = primaryEmployeeRepository.find().toList();
        for (Employee employee : primaryList) {
            if (!secondaryList.contains(employee)) {
                fail(employee + " does not exists in secondary");
            }
        }
    }
}
