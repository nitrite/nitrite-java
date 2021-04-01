package org.dizitart.no2.common.concurrent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The lock service.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
public class LockService {
    private final Map<String, ReentrantReadWriteLock> lockRegistry;

    /**
     * Instantiates a new Lock service.
     */
    public LockService() {
        this.lockRegistry = new HashMap<>();
    }

    /**
     * Gets read lock.
     *
     * @param name the name
     * @return the read lock
     */
    public synchronized Lock getReadLock(String name) {
        if (lockRegistry.containsKey(name)) {
            ReentrantReadWriteLock rwLock = lockRegistry.get(name);
            return rwLock.readLock();
        }
        ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
        lockRegistry.put(name, rwLock);
        return rwLock.readLock();
    }

    /**
     * Gets write lock.
     *
     * @param name the name
     * @return the write lock
     */
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
