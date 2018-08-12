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
import org.dizitart.no2.Document;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.dizitart.no2.Document.createDocument;
import static org.dizitart.no2.filters.Filters.eq;
import static org.dizitart.no2.sync.TimeSpan.timeSpan;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
public class SyncHandleTest extends BaseSyncTest {
    @Test
    public void testStartSync() {
        syncHandlePrimary = Replicator.of(primaryDb)
                .forLocal(primary)
                .withSyncTemplate(syncTemplate)
                .delay(timeSpan(1, TimeUnit.SECONDS))
                .ofType(ReplicationType.BOTH_WAY)
                .withListener(new SyncTestEventListener())
                .configure();

        syncHandlePrimary.startSync();
        assertFalse(syncHandlePrimary.isPaused());
        assertFalse(syncHandlePrimary.isCancelled());

        await().atMost(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return eventQueue.poll() == EventType.STARTED;
            }
        });
    }

    @Test
    public void testPauseSync() {
        syncHandlePrimary = Replicator.of(primaryDb)
                .forLocal(primary)
                .withSyncTemplate(syncTemplate)
                .delay(timeSpan(1, TimeUnit.SECONDS))
                .ofType(ReplicationType.BOTH_WAY)
                .withListener(new SyncTestEventListener())
                .configure();

        syncHandlePrimary.startSync();
        assertFalse(syncHandlePrimary.isPaused());
        assertFalse(syncHandlePrimary.isCancelled());

        syncHandlePrimary.pauseSync();
        assertTrue(syncHandlePrimary.isPaused());
        assertFalse(syncHandlePrimary.isCancelled());
    }

    @Test
    public void testResumeSync() {
        syncHandlePrimary = Replicator.of(primaryDb)
                .forLocal(primary)
                .withSyncTemplate(syncTemplate)
                .delay(timeSpan(1, TimeUnit.SECONDS))
                .ofType(ReplicationType.BOTH_WAY)
                .withListener(new SyncTestEventListener())
                .configure();

        syncHandlePrimary.startSync();
        assertFalse(syncHandlePrimary.isPaused());
        assertFalse(syncHandlePrimary.isCancelled());

        syncHandlePrimary.pauseSync();
        assertTrue(syncHandlePrimary.isPaused());
        assertFalse(syncHandlePrimary.isCancelled());

        syncHandlePrimary.resumeSync();
        assertFalse(syncHandlePrimary.isPaused());
        assertFalse(syncHandlePrimary.isCancelled());
    }

    @Test
    public void testResetLocalWithRemote() {
        syncHandlePrimary = Replicator.of(primaryDb)
                .forLocal(primary)
                .withSyncTemplate(syncTemplate)
                .delay(timeSpan(1, TimeUnit.SECONDS))
                .ofType(ReplicationType.BOTH_WAY)
                .withListener(new SyncTestEventListener())
                .configure();

        Document doc1 = createDocument("field1", "one");
        primary.insert(doc1);
        assertEquals(server.find(eq("field1", "one")).size(), 0);

        Document doc2 = createDocument("field2", "two");
        server.insert(doc2);
        assertEquals(primary.find(eq("field2", "two")).size(), 0);

        syncHandlePrimary.resetLocalWithRemote(0, (int)primary.size());

        assertEquals(server.find(eq("field1", "one")).size(), 0);
        assertEquals(primary.find(eq("field1", "one")).size(), 0);
        assertEquals(primary.find(eq("field2", "two")).size(), 1);
        assertEquals(server.find(eq("field2", "two")).size(), 1);
    }

    @Test
    public void testResetRemoteWithLocal() {
        syncHandlePrimary = Replicator.of(primaryDb)
                .forLocal(primary)
                .withSyncTemplate(syncTemplate)
                .delay(timeSpan(1, TimeUnit.SECONDS))
                .ofType(ReplicationType.BOTH_WAY)
                .withListener(new SyncTestEventListener())
                .configure();

        Document doc1 = createDocument("field1", "one");
        primary.insert(doc1);
        assertEquals(secondary.find(eq("field1", "one")).size(), 0);

        Document doc2 = createDocument("field2", "two");
        server.insert(doc2);
        assertEquals(primary.find(eq("field2", "two")).size(), 0);

        log.debug("******************************************");
        syncHandlePrimary.resetRemoteWithLocal(0, (int)secondary.size());

        log.debug("Secondary Content" + secondary.find());
        log.debug("Primary Content" + primary.find());

        assertEquals(primary.find(eq("field1", "one")).size(), 1);
        assertEquals(primary.find(eq("field2", "two")).size(), 0);
        assertEquals(server.find(eq("field2", "two")).size(), 0);
        assertEquals(server.find(eq("field1", "one")).size(), 1);
    }

    @Test
    public void testCancelSync() {
        syncHandlePrimary = Replicator.of(primaryDb)
                .forLocal(primary)
                .withSyncTemplate(syncTemplate)
                .delay(timeSpan(1, TimeUnit.SECONDS))
                .ofType(ReplicationType.BOTH_WAY)
                .withListener(new SyncTestEventListener())
                .configure();
        syncHandlePrimary.startSync();

        syncHandleSecondary = Replicator.of(secondaryDb)
                .forLocal(secondary)
                .withSyncTemplate(syncTemplate)
                .delay(timeSpan(1, TimeUnit.SECONDS))
                .ofType(ReplicationType.BOTH_WAY)
                .withListener(new SyncTestEventListener())
                .configure();
        syncHandleSecondary.startSync();

        Document doc0 = createDocument("no-key", "no-value");
        secondary.insert(new Document[]{ doc0 });

        Document doc1 = createDocument("first-key", "first-value");
        primary.insert(new Document[]{ doc1 });

        Document doc2 = createDocument("second-key", "second-value");
        primary.insert(new Document[]{ doc2 });

        await().atMost(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return secondary.find().size() == 3 && primary.find().size() == 3;
            }
        });

        assertNotNull(secondary.find(eq("first-key", "first-value")).firstOrDefault());
        assertNotNull(secondary.find(eq("second-key", "second-value")).firstOrDefault());

        assertNotNull(primary.find(eq("no-key", "no-value")).firstOrDefault());

        doc1.put("first-key", "new-value");
        primary.update(doc1);
        await().atMost(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return secondary.find(eq("first-key", "new-value")).size() == 1;
            }
        });

        log.info("******************Remove***************");
        primary.remove(doc1);
        await().atMost(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return secondary.find(eq("first-key", "new-value")).size() == 0;
            }
        });

        syncHandlePrimary.cancelSync();
        assertTrue(syncHandlePrimary.isCancelled());

        secondary.remove(doc2);
        await().atMost(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                // just wait for more than sync delay time = 1 sec
                // to make sure sync thread did not run
                Thread.sleep(2000);
                return true;
            }
        });

        // remove in secondary does not trigger remove in primary as sync is off
        assertEquals(primary.find().size(), 2);
        assertEquals(secondary.find().size(), 1);
        assertEquals(secondary.find().firstOrDefault(), doc0);
        // doc2 still should be in primary
        assertEquals(primary.find(eq("second-key", "second-value"))
                .firstOrDefault(), doc2);
    }

    @Test
    public void testStopSync() {
        syncHandlePrimary = Replicator.of(primaryDb)
                .forLocal(primary)
                .withSyncTemplate(syncTemplate)
                .delay(timeSpan(1, TimeUnit.SECONDS))
                .ofType(ReplicationType.BOTH_WAY)
                .withListener(new SyncTestEventListener())
                .configure();

        syncHandlePrimary.startSync();
        assertFalse(syncHandlePrimary.isStopped());

        syncHandlePrimary.stopSync();
        assertTrue(syncHandlePrimary.isStopped());

        syncHandlePrimary.startSync();
        assertFalse(syncHandlePrimary.isStopped());
    }

    @Test(expected = InvalidOperationException.class)
    public void testInvalidStart() {
        syncHandlePrimary = Replicator.of(primaryDb)
                .forLocal(primary)
                .withSyncTemplate(syncTemplate)
                .delay(timeSpan(1, TimeUnit.SECONDS))
                .ofType(ReplicationType.BOTH_WAY)
                .withListener(new SyncTestEventListener())
                .configure();

        syncHandlePrimary.startSync();
        syncHandlePrimary.startSync();
    }
}
