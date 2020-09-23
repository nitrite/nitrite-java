package org.dizitart.no2.transaction;

import lombok.Data;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.store.NitriteMap;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Anindya Chatterjee
 */
@Data
class TransactionContext implements AutoCloseable {
    private String collectionName;
    private Queue<JournalEntry> journal;
    private NitriteMap<NitriteId, Document> nitriteMap;
    private TransactionalConfig config;
    private AtomicBoolean active;

    public TransactionContext() {
        active = new AtomicBoolean(true);
    }

    @Override
    public void close() {
        journal.clear();
        nitriteMap.clear();
        nitriteMap.close();
        active.compareAndSet(true, false);
    }
}
