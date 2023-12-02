package org.dizitart.no2.transaction;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.common.concurrent.LockService;
import org.dizitart.no2.exceptions.TransactionException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A session represents a transactional context for a Nitrite database.
 * It provides methods to create a new transaction.
 * <p>
 * A session should be closed after use to release any resources 
 * associated with it.
 * <p>
 * If a session is closed and the transaction is not committed,
 * all opened transactions will get rolled back and all volatile
 * data gets discarded for the session.
 * 
 * @author Anindya Chatterjee
 * @since 4.0
 */
public class Session implements AutoCloseable {
    private final Nitrite nitrite;
    private final AtomicBoolean active;
    private final LockService lockService;
    private final Map<String, Transaction> transactionMap;

    /**
     * Instantiates a new Session.
     *
     * @param nitrite     the nitrite
     * @param lockService the lock service
     */
    public Session(Nitrite nitrite, LockService lockService) {
        this.nitrite = nitrite;
        this.active = new AtomicBoolean(true);
        this.lockService = lockService;
        this.transactionMap = new HashMap<>();
    }

    /**
     * Begins a new transaction.
     *
     * @return the new transaction.
     */
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
            if (transaction.getState() != TransactionState.Closed) {
                transaction.rollback();
            }
        }
    }

    /**
     * Checks state of the session. If the session is not active,
     * it will throw a {@link TransactionException}.
     *
     * @throws TransactionException when the session is not active,
     * and a transaction is initiated in this session.
     */
    public void checkState() {
        if (!active.get()) {
            throw new TransactionException("Session is closed");
        }
    }
}
