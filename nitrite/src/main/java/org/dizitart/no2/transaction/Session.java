package org.dizitart.no2.transaction;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.common.concurrent.LockService;
import org.dizitart.no2.exceptions.TransactionException;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Anindya Chatterjee
 */
public class Session implements Closeable {
    private final Nitrite nitrite;
    private final AtomicBoolean active;
    private final LockService lockService;
    private final Map<String, Transaction> transactionMap;

    public Session(Nitrite nitrite, LockService lockService) {
        this.nitrite = nitrite;
        this.active = new AtomicBoolean(true);
        this.lockService = lockService;
        this.transactionMap = new HashMap<>();
    }

    public Transaction beginTransaction() {
        checkState();

        Transaction tx = new NitriteTransaction(nitrite, lockService);
        transactionMap.put(tx.getId(), tx);
        return tx;
    }

    @Override
    public void close() {
        this.active.compareAndSet(true, false);
        for (Transaction transaction : transactionMap.values()) {
            if (transaction.getState() != State.Closed) {
                transaction.rollback();
            }
        }
    }

    public void checkState() {
        if (!active.get()) {
            throw new TransactionException("this session is not active");
        }
    }
}
