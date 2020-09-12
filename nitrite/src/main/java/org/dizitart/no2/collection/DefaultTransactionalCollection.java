package org.dizitart.no2.collection;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.common.tuples.Triplet;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.TransactionException;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.tx.ChangeLog;
import org.dizitart.no2.store.tx.ChangeType;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Anindya Chatterjee
 */
class DefaultTransactionalCollection extends BaseNitriteCollection implements TransactionalCollection {
    private final Queue<ChangeLog> changeLogs;
    private final NitriteCollection primary;
    private final Lock primaryWriteLock;
    private volatile boolean transactionOpened;

    public DefaultTransactionalCollection(String name,
                                          NitriteMap<NitriteId, Document> nitriteMap,
                                          NitriteConfig nitriteConfig,
                                          NitriteCollection primary,
                                          Lock primaryWriteLock) {
        super(name, nitriteMap, nitriteConfig);
        this.changeLogs = new LinkedList<>();
        this.primary = primary;
        this.primaryWriteLock = primaryWriteLock;
        this.transactionOpened = true;
    }

    protected void checkOpened() {
        if (!primary.isOpen()) {
            throw new NitriteIOException("store is closed");
        }

        if (isDropped()) {
            throw new NitriteIOException("collection has been dropped");
        }

        if (!transactionOpened) {
            throw new TransactionException("transaction is closed");
        }
    }

    @Override
    public WriteResult insert(Document[] documents) {
        checkOpened();
        WriteResult result = super.insert(documents);

        Document[] data = new Document[documents.length];
        int counter = 0;
        for (NitriteId nitriteId : result) {
            data[counter++] = super.getById(nitriteId);
        }

        ChangeLog changeLog = new ChangeLog();
        changeLog.setChangeType(ChangeType.Insert);
        changeLog.setObject(data);
        changeLogs.add(changeLog);

        // add inserts numbers

        return result;
    }

    @Override
    public WriteResult update(Filter filter, Document update, UpdateOptions updateOptions) {
        checkOpened();
        WriteResult result = super.update(filter, update, updateOptions);

        ChangeLog changeLog = new ChangeLog();
        changeLog.setChangeType(ChangeType.Update);
        changeLog.setObject(new Triplet<>(filter, update, updateOptions));
        changeLogs.add(changeLog);

        return result;
    }

    @Override
    public WriteResult remove(Document element) {
        checkOpened();
        WriteResult result = super.remove(element);

        ChangeLog changeLog = new ChangeLog();
        changeLog.setChangeType(ChangeType.Remove);
        changeLog.setObject(element);
        changeLogs.add(changeLog);

        // one element is removed

        return result;
    }

    @Override
    public WriteResult remove(Filter filter, boolean justOne) {
        checkOpened();

        WriteResult result = super.remove(filter, justOne);

        // create commandLog
        ChangeLog changeLog = new ChangeLog();
        changeLog.setObject(new Pair<>(filter, justOne));
        changeLog.setChangeType(ChangeType.Remove);

        changeLogs.add(changeLog);

        // add removed numbers

        return result;
    }

    @Override
    public void clear() {
        checkOpened();
        super.clear();

        ChangeLog changeLog = new ChangeLog();
        changeLog.setObject(null);
        changeLog.setChangeType(ChangeType.Clear);

        changeLogs.add(changeLog);

    }

    @Override
    public void createIndex(String field, IndexOptions indexOptions) {
        super.createIndex(field, indexOptions);

        ChangeLog changeLog = new ChangeLog();
        changeLog.setChangeType(ChangeType.CreateIndex);
        changeLog.setObject(new Pair<>(field, indexOptions));
        changeLogs.add(changeLog);
    }

    @Override
    public void rebuildIndex(String field, boolean isAsync) {
        super.rebuildIndex(field, isAsync);

        ChangeLog changeLog = new ChangeLog();
        changeLog.setChangeType(ChangeType.RebuildIndex);
        changeLog.setObject(new Pair<>(field, isAsync));
        changeLogs.add(changeLog);
    }

    @Override
    public void dropIndex(String field) {
        super.dropIndex(field);

        ChangeLog changeLog = new ChangeLog();
        changeLog.setChangeType(ChangeType.DropIndex);
        changeLog.setObject(field);
        changeLogs.add(changeLog);
    }

    @Override
    public void dropAllIndices() {
        super.dropAllIndices();

        ChangeLog changeLog = new ChangeLog();
        changeLog.setChangeType(ChangeType.DropIndex);
        changeLog.setObject(null);
        changeLogs.add(changeLog);
    }

    @Override
    public void drop() {
        super.drop();

        ChangeLog changeLog = new ChangeLog();
        changeLog.setChangeType(ChangeType.DropCollection);
        changeLog.setObject(getName());
        changeLogs.add(changeLog);

    }

    @Override
    public long size() {
        return find().size();
    }

    @Override
    public boolean isOpen() {
        boolean result = super.isOpen();
        if (result) {
            return transactionOpened;
        }
        return false;
    }

    @Override
    public void close() {
        transactionOpened = false;

        // clear comm
        changeLogs.clear();

        // release the transaction lock on primary
        unlock();

        // close the secondary collection
        super.close();
    }

    @Override
    public void commit() {
        checkOpened();

        int count = changeLogs.size();
        for (int i = 0; i < count; i++) {
            ChangeLog changeLog = changeLogs.poll();
            if (changeLog != null) {
                switch (changeLog.getChangeType()) {
                    case Insert:
                        mergeInsert(changeLog);
                        break;
                    case Update:
                        mergeUpdate(changeLog);
                        break;
                    case Remove:
                        mergeRemove(changeLog);
                        break;
                    case Clear:
                        primary.clear();
                        break;
                    case CreateIndex:
                        createIndex(changeLog);
                        break;
                    case RebuildIndex:
                        rebuildIndex(changeLog);
                        break;
                    case DropIndex:
                        dropIndex(changeLog);
                        break;
                    case DropCollection:
                        primary.drop();
                        break;
                }
            }
        }

        close();
    }

    @Override
    public void rollback() {
        checkOpened();
        // just close to discard changes
        close();
    }

    private void unlock() {
        ReentrantReadWriteLock.WriteLock wLock = (ReentrantReadWriteLock.WriteLock) primaryWriteLock;
        if (wLock.getHoldCount() != 0) {
            primaryWriteLock.unlock();
        }
    }

    private void mergeInsert(ChangeLog changeLog) {
        Document[] documents = (Document[]) changeLog.getObject();
        for (Document document : documents) {
            primary.update(document, true);
        }
    }

    @SuppressWarnings("unchecked")
    private void mergeUpdate(ChangeLog changeLog) {
        Object args = changeLog.getObject();
        if (args instanceof Triplet) {
            Triplet<Filter, Document, UpdateOptions> tripletArgs = (Triplet<Filter, Document, UpdateOptions>) args;
            primary.update(tripletArgs.getFirst(), tripletArgs.getSecond(), tripletArgs.getThird());
        }
    }

    @SuppressWarnings("unchecked")
    private void mergeRemove(ChangeLog changeLog) {
        Object item = changeLog.getObject();
        if (item instanceof Document) {
            Document document = (Document) item;
            primary.remove(document);
        } else if (item instanceof Pair) {
            Pair<Filter, Boolean> pairs = (Pair<Filter, Boolean>) item;
            primary.remove(pairs.getFirst(), pairs.getSecond());
        }
    }

    @SuppressWarnings("unchecked")
    private void createIndex(ChangeLog changeLog) {
        Pair<String, IndexOptions> arg = (Pair<String, IndexOptions>) changeLog.getObject();
        if (!primary.hasIndex(arg.getFirst())) {
            primary.createIndex(arg.getFirst(), arg.getSecond());
        }
    }

    @SuppressWarnings("unchecked")
    private void rebuildIndex(ChangeLog changeLog) {
        Pair<String, Boolean> arg = (Pair<String, Boolean>) changeLog.getObject();
        if (!primary.isIndexing(arg.getFirst())) {
            primary.rebuildIndex(arg.getFirst(), arg.getSecond());
        }
    }

    private void dropIndex(ChangeLog changeLog) {
        String field = (String) changeLog.getObject();
        if (field == null) {
            primary.dropAllIndices();
            return;
        }

        if (primary.hasIndex(field) && !primary.isIndexing(field)) {
            primary.dropIndex(field);
        }
    }
}
