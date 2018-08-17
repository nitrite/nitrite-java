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

package org.dizitart.no2.sync;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.Retry;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.objects.Cursor;
import org.dizitart.no2.collection.objects.ObjectRepository;
import org.dizitart.no2.collection.objects.data.DataGenerator;
import org.dizitart.no2.collection.objects.data.Employee;
import org.dizitart.no2.common.ExecutorServiceManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.dizitart.no2.DbTestOperations.getRandomTempDbFile;
import static org.dizitart.no2.filters.ObjectFilters.eq;
import static org.dizitart.no2.sync.TimeSpan.timeSpan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
public class RandomSyncTest {
    private NitriteCollection server;
    private ObjectRepository<Employee> primary;
    private ObjectRepository<Employee> secondary;

    private SyncTemplate syncTemplate;

    private Nitrite primaryDb;
    private Nitrite secondaryDb;
    private Nitrite serverDb;

    private String primaryFile;
    private String secondaryFile;
    private String serverFile;

    @Rule
    public Retry retry = new Retry(3);

    @Before
    public void setUp() {
        primaryFile = getRandomTempDbFile();
        secondaryFile = getRandomTempDbFile();
        serverFile = getRandomTempDbFile();

        primaryDb = Nitrite.builder()
                .filePath(primaryFile)
                .openOrCreate();

        secondaryDb = Nitrite.builder()
                .filePath(secondaryFile)
                .openOrCreate();

        serverDb = Nitrite.builder()
                .filePath(serverFile)
                .openOrCreate();

        primary = primaryDb.getRepository(Employee.class);
        secondary = secondaryDb.getRepository(Employee.class);
        server = serverDb.getCollection("server");

        syncTemplate = new MockSyncTemplate(server, serverDb.getCollection("removeLog"));
    }

    @After
    public void clear() throws IOException {
        primaryDb.close();
        secondaryDb.close();
        serverDb.close();

        Files.delete(Paths.get(serverFile));
        Files.delete(Paths.get(primaryFile));
        Files.delete(Paths.get(secondaryFile));
    }

    @Test
    public void testRandomSync() throws InterruptedException {
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

        worker.submit(new Runnable() {
            @Override
            public void run() {
                // insert some data
                for (int i = 0; i < 7; i++) {
                    primary.insert(DataGenerator.generateEmployee());
                }

                Cursor<Employee> cursor = primary.find();

                int i = 0;
                for (Employee employee : cursor) {
                    if (i == 5) {
                        employee.setAddress("New Address");
                        primary.update(eq("empId", employee.getEmpId()), employee);
                    }
                    i++;
                }

                await().atMost(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        Thread.sleep(2000);
                        return true;
                    }
                });

                latch.countDown();
            }
        });

        worker.submit(new Runnable() {
            @Override
            public void run() {
                // insert some data
                for (int i = 0; i < 5; i++) {
                    secondary.insert(DataGenerator.generateEmployee());
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                secondary.remove(eq("empId", secondary.find().firstOrDefault().getEmpId()));
                latch.countDown();
            }
        });

        latch.await();

        // wait for 4 sec to sync it all up
        try {
            await().atMost(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return primary.size() == 11 && secondary.size() == 11;
                }
            });
        } catch (Throwable t) {
            log.error("Primary Size = " + primary.size() + " & Secondary Size = " + secondary.size());
            log.error("\nPrimary - " + primary.find().toList() + "\nSecondary - "
                    + secondary.find().toList(), t);
            throw t;
        }

        assertEquals(primary.size(), secondary.size());
        List<Employee> secondaryList = secondary.find().toList();
        List<Employee> primaryList = primary.find().toList();
        for (Employee employee : primaryList) {
            if (!secondaryList.contains(employee)) {
                fail(employee + " does not exists in secondary");
            }
        }
    }
}