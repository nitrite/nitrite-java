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
import org.awaitility.core.ConditionTimeoutException;
import org.dizitart.no2.Document;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.dizitart.no2.Document.createDocument;
import static org.dizitart.no2.common.Constants.DOC_ID;
import static org.dizitart.no2.filters.Filters.eq;
import static org.dizitart.no2.sync.TimeSpan.timeSpan;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
public class SimpleSyncTest extends BaseSyncTest {

    @Test
    public void testSimpleSync() {
        // create primary sync handle
        syncHandlePrimary = Replicator.of(primaryDb)
                .forLocal(primary)
                .withSyncTemplate(syncTemplate)
                .delay(timeSpan(1, TimeUnit.SECONDS))
                .ofType(ReplicationType.BOTH_WAY)
                .withListener(new SyncTestEventListener())
                .configure();

        // create secondary sync handle
        syncHandleSecondary = Replicator.of(secondaryDb)
                .forLocal(secondary)
                .withSyncTemplate(syncTemplate)
                .delay(timeSpan(1, TimeUnit.SECONDS))
                .ofType(ReplicationType.BOTH_WAY)
                .withListener(new SyncTestEventListener())
                .configure();

        // start primary sync using handle
        syncHandlePrimary.startSync();

        // start secondary sync using handle
        syncHandleSecondary.startSync();

        Document doc = createDocument("field", "one");

        primary.insert(doc);
        assertEquals(secondary.find().size(), 0);
        await().atMost(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return secondary.find().size() == 1;
            }
        });
        assertEquals(secondary.find().size(), 1);


        Document document = primary.find(eq("field", "one"))
                .firstOrDefault();
        document.put("field", "two");
        primary.update(document);
        await().atMost(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return secondary.find(eq("field", "two")).size() == 1;
            }
        });

        Document document1 = secondary.find(eq("field", "two"))
                .firstOrDefault();
        assertEquals(document.get("field"), document1.get("field"));
        assertEquals(document.get(DOC_ID), document1.get(DOC_ID));
        assertEquals(primary.find().size(), 1);
        assertEquals(secondary.find().size(), 1);

        primary.remove(document);
        await().atMost(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return secondary.find().size() == 0;
            }
        });

        document1 = secondary.find().firstOrDefault();
        assertNull(document1);

        doc = createDocument("field", "one");

        secondary.insert(doc);
        assertEquals(primary.find().size(), 0);
        await().atMost(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return primary.find().size() == 1;
            }
        });
        assertEquals(primary.find().size(), 1);

        document = secondary.find(eq("field", "one"))
                .firstOrDefault();
        document.put("field", "two");
        secondary.update(document);
        await().atMost(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return primary.find(eq("field", "two")).size() == 1;
            }
        });

        document1 = primary.find(eq("field", "two"))
                .firstOrDefault();
        assertEquals(document.get("field"), document1.get("field"));
        assertEquals(document.get(DOC_ID), document1.get(DOC_ID));
        assertEquals(primary.find().size(), 1);
        assertEquals(secondary.find().size(), 1);

        secondary.remove(document);
        await().atMost(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return primary.find().size() == 0;
            }
        });

        document1 = primary.find().firstOrDefault();
        assertNull(document1);
    }

    @Test
    public void testPullSync() {
        syncHandlePrimary = Replicator.of(primaryDb)
                .forLocal(primary)
                .withSyncTemplate(syncTemplate)
                .delay(timeSpan(1, TimeUnit.SECONDS))
                .ofType(ReplicationType.PULL)
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
        primary.insert(new Document[]{ doc0 });

        Document doc1 = createDocument("first-key", "first-value");
        secondary.insert(new Document[]{ doc1 });

        Document doc2 = createDocument("second-key", "second-value");
        secondary.insert(new Document[]{ doc2 });

        await().atMost(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return primary.find().size() == 3;
            }
        });

        assertNotNull(primary.find(eq("first-key", "first-value")).firstOrDefault());
        assertNotNull(primary.find(eq("second-key", "second-value")).firstOrDefault());

        assertNull(secondary.find(eq("no-key", "no-value")).firstOrDefault());

        doc1.put("first-key", "new-value");
        secondary.update(doc1);
        await().atMost(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return primary.find(eq("first-key", "new-value")).size() == 1;
            }
        });

        secondary.remove(doc1);
        await().atMost(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return primary.find(eq("first-key", "new-value")).size() == 0;
            }
        });
    }

    @Test
    public void testPushSync() {
        syncHandlePrimary = Replicator.of(primaryDb)
                .forLocal(primary)
                .withSyncTemplate(syncTemplate)
                .delay(timeSpan(1, TimeUnit.SECONDS))
                .ofType(ReplicationType.PUSH)
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
                return secondary.find().size() == 3;
            }
        });

        assertNotNull(secondary.find(eq("first-key", "first-value")).firstOrDefault());
        assertNotNull(secondary.find(eq("second-key", "second-value")).firstOrDefault());

        assertNull(primary.find(eq("no-key", "no-value")).firstOrDefault());

        doc1.put("first-key", "new-value");
        primary.update(doc1);
        await().atMost(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return secondary.find(eq("first-key", "new-value")).size() == 1;
            }
        });

        primary.remove(doc1);
        await().atMost(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return secondary.find(eq("first-key", "new-value")).size() == 0;
            }
        });
    }

    @Test
    public void testBiDirectionalSync() {
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

        primary.remove(doc1);
        await().atMost(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return secondary.find(eq("first-key", "new-value")).size() == 0;
            }
        });

        doc2.put("second-key", "new-value");
        secondary.update(doc2);
        await().atMost(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return primary.find(eq("second-key", "new-value")).size() == 1;
            }
        });

        secondary.remove(doc2);
        await().atMost(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return primary.find(eq("second-key", "new-value")).size() == 0;
            }
        });

        assertEquals(primary.find().size(), 1);
        assertEquals(secondary.find().size(), 1);
        assertEquals(secondary.find().firstOrDefault(), doc0);
        assertEquals(primary.find().firstOrDefault(), doc0);
    }

    @Test
    public void testPauseAndResume() {
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

        assertEquals(secondary.find().size(), 0);
        Document doc1 = createDocument("first-key", "first-value");
        primary.insert(new Document[]{ doc1 });

        await().atMost(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return secondary.find().size() == 1;
            }
        });

        syncHandlePrimary.pauseSync();
        assertTrue(syncHandlePrimary.isPaused());

        doc1.put("first-key", "new-value");
        primary.update(doc1);

        boolean notSynced = false;
        try {
            await().atMost(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return secondary.find(eq("first-key", "new-value")).size() == 1;
                }
            });
        } catch (ConditionTimeoutException cte) {
            notSynced = true;
        } finally {
            assertTrue(notSynced);
        }

        syncHandlePrimary.resumeSync();
        assertFalse(syncHandlePrimary.isPaused());

        await().atMost(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return secondary.find(eq("first-key", "new-value")).size() == 1;
            }
        });
    }
}
