package org.dizitart.no2.common.concurrent;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ThreadPoolManagerTest {
    @Test
    public void testGetThreadPool() {
        assertTrue(ThreadPoolManager.getThreadPool(3, "threadName") instanceof java.util.concurrent.ThreadPoolExecutor);
    }
}

