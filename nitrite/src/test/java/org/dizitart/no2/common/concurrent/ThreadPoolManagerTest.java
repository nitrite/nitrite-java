package org.dizitart.no2.common.concurrent;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ThreadPoolManagerTest {
    @Test
    public void testGetThreadPool() {
        assertTrue(ThreadPoolManager.getThreadPool(3, "threadName") instanceof java.util.concurrent.ThreadPoolExecutor);
        assertTrue(ThreadPoolManager.getThreadPool(1, "foo") instanceof java.util.concurrent.ThreadPoolExecutor);
    }
}

