package org.dizitart.no2.common.concurrent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Anindya Chatterjee
 */
public class LockService {
    private final Map<String, ReentrantReadWriteLock> lockRegistry;

    public LockService() {
        this.lockRegistry = new HashMap<>();
    }

    public synchronized Lock getReadLock(String name) {
        if (lockRegistry.containsKey(name)) {
            ReentrantReadWriteLock rwLock = lockRegistry.get(name);
            return rwLock.readLock();
        }
        ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
        lockRegistry.put(name, rwLock);
        return rwLock.readLock();
    }

    public synchronized Lock getWriteLock(String name) {
        if (lockRegistry.containsKey(name)) {
            ReentrantReadWriteLock rwLock = lockRegistry.get(name);
            return rwLock.writeLock();
        }
        ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
        lockRegistry.put(name, rwLock);
        return rwLock.writeLock();
    }
}
