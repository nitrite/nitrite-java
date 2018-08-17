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
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Queue;

import static org.dizitart.no2.DbTestOperations.getRandomTempDbFile;
import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
public abstract class BaseSyncTest {
    protected NitriteCollection primary;
    protected NitriteCollection server;
    protected NitriteCollection secondary;

    protected String primaryFile;
    protected String secondaryFile;
    protected String serverFile;

    protected Nitrite primaryDb;
    protected Nitrite secondaryDb;
    protected Nitrite serverDb;

    protected SyncTemplate syncTemplate;
    protected SyncHandle syncHandlePrimary;
    protected SyncHandle syncHandleSecondary;
    protected Queue<EventType> eventQueue;

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

        primary = primaryDb.getCollection("primary");
        secondary = secondaryDb.getCollection("secondary");
        server = serverDb.getCollection("server");

        syncTemplate = new MockSyncTemplate(server, serverDb.getCollection("removeLog"));
        eventQueue = new LinkedList<>();
    }

    @After
    public void clear() throws IOException {
        primaryDb.close();
        secondaryDb.close();
        serverDb.close();

        Files.delete(Paths.get(primaryFile));
        Files.delete(Paths.get(secondaryFile));
        Files.delete(Paths.get(serverFile));
    }

    class SyncTestEventListener extends SyncEventListener {
        @Override
        public void onSyncEvent(SyncEventData eventInfo) {
            Throwable syncError = eventInfo.getError();
            EventType syncEventType = eventInfo.getEventType();
            assertEquals(eventInfo.getCollectionName(), "primary");
            if (syncError != null) {
                log.error("Sync error in " + syncEventType, syncError);
            }
            eventQueue.offer(syncEventType);
        }
    }
}
