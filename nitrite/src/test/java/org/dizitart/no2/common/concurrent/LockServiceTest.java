package org.dizitart.no2.common.concurrent;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LockServiceTest {
    @Test
    public void testGetReadLock() {
        assertTrue(
            (new LockService()).getReadLock("name") instanceof java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock);
    }

    @Test
    public void testGetWriteLock() {
        assertTrue((new LockService())
            .getWriteLock("name") instanceof java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock);
    }
}

