package org.dizitart.no2.transaction;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.common.concurrent.LockService;
import org.dizitart.no2.exceptions.TransactionException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A nitrite transaction session. A session is needed to
 * initiate a transaction in nitrite database.
 *
 * <p>
 * If a session is closed and the transaction is not committed,
 * all opened transactions will get rolled back and all volatile
 * data gets discarded for the session.
 * </p>
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
     * @return the transaction
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
            if (transaction.getState() != State.Closed) {
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
            throw new TransactionException("This session is not active");
        }
    }
}
