package org.dizitart.no2.sync;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteCollection;
import org.dizitart.no2.meta.Attributes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.dizitart.no2.DbTestOperations.getRandomTempDbFile;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee.
 */
public class SyncTemplateTest {
    private NitriteCollection collection;
    private SyncTemplate syncTemplate;
    private Nitrite db;
    private String dbFile;

    @Before
    public void setUp() {
        dbFile = getRandomTempDbFile();
        db = Nitrite.builder()
                .filePath(dbFile)
                .openOrCreate();
        collection = db.getCollection("test");
        syncTemplate = new MockSyncTemplate(collection, null);
    }

    @After
    public void cleanUp() throws IOException {
        db.close();
        Files.delete(Paths.get(dbFile));
    }

    @Test
    public void testTrySyncLock() {
        int waitUnit = 1;
        assertTrue(syncTemplate.trySyncLock(TimeSpan.timeSpan(waitUnit, TimeUnit.SECONDS), "origin"));
        Attributes attributes = collection.getAttributes();
        assertNotNull(attributes);
        long currentTime = System.currentTimeMillis();
        assertTrue(attributes.getSyncLock() <= currentTime);
        assertEquals(attributes.getExpiryWait(), 1000 * waitUnit);
    }

    @Test
    public void testReleaseLock() {
        assertTrue(syncTemplate.trySyncLock(TimeSpan.timeSpan(1, TimeUnit.SECONDS), "origin"));
        syncTemplate.releaseLock("origin");
        Attributes attributes = collection.getAttributes();
        assertNotNull(attributes);
        assertEquals(attributes.getExpiryWait(), 0);
        assertEquals(attributes.getSyncLock(), 0);
    }

    @Test
    public void testConsecutiveLock() {
        assertTrue(syncTemplate.trySyncLock(TimeSpan.timeSpan(1, TimeUnit.SECONDS), "origin"));
        assertFalse(syncTemplate.trySyncLock(TimeSpan.timeSpan(1, TimeUnit.SECONDS), "origin"));
    }

    @Test
    public void testLockReleaseLockCycle() {
        assertTrue(syncTemplate.trySyncLock(TimeSpan.timeSpan(1, TimeUnit.SECONDS), "origin"));
        syncTemplate.releaseLock("origin");
        assertTrue(syncTemplate.trySyncLock(TimeSpan.timeSpan(1, TimeUnit.SECONDS), "origin"));
        syncTemplate.releaseLock("origin");
    }

    @Test
    public void testLockExpiry() {
        assertTrue(syncTemplate.trySyncLock(TimeSpan.timeSpan(1, TimeUnit.SECONDS), "origin"));
        await().atMost(3, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                // wait for 2 seconds
                Thread.sleep(2000);
                return true;
            }
        });
        // after 2 seconds lock will be expired
        assertTrue(syncTemplate.trySyncLock(TimeSpan.timeSpan(1, TimeUnit.SECONDS), "origin"));
    }
}
