package org.dizitart.no2.transaction;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.*;
import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.collection.operation.CollectionOperations;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.common.event.EventBus;
import org.dizitart.no2.common.event.NitriteEventBus;
import org.dizitart.no2.common.processors.Processor;
import org.dizitart.no2.exceptions.*;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.dizitart.no2.collection.UpdateOptions.updateOptions;
import static org.dizitart.no2.common.util.DocumentUtils.createUniqueFilter;
import static org.dizitart.no2.common.util.ValidationUtils.containsNull;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;
import static org.dizitart.no2.index.IndexOptions.indexOptions;

/**
 * @author Anindya Chatterjee
 * @since 4.0
 */
@Getter @Setter
class DefaultTransactionalCollection implements NitriteCollection {
    private final NitriteCollection primary;
    private final TransactionContext transactionContext;
    private final Nitrite nitrite;

    private String collectionName;
    private NitriteMap<NitriteId, Document> nitriteMap;
    private NitriteStore<?> nitriteStore;
    private CollectionOperations collectionOperations;
    private volatile boolean isDropped;
    private volatile boolean isClosed;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Lock writeLock;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Lock readLock;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private EventBus<CollectionEventInfo<?>, CollectionEventListener> eventBus;

    public DefaultTransactionalCollection(NitriteCollection primary,
                                          TransactionContext transactionContext,
                                          Nitrite nitrite) {
        this.primary = primary;
        this.transactionContext = transactionContext;
        this.nitrite = nitrite;

        initialize();
    }

    @Override
    public WriteResult insert(Document[] documents) {
        notNull(documents, "a null document cannot be inserted");
        containsNull(documents, "a null document cannot be inserted");

        for (Document document : documents) {
            // generate ids
            document.getId();
        }

        WriteResult result;
        try {
            writeLock.lock();
            checkOpened();
            result = collectionOperations.insert(documents);
        } finally {
            writeLock.unlock();
        }

        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setChangeType(ChangeType.Insert);
        journalEntry.setCommit(() -> primary.insert(documents));
        journalEntry.setRollback(() -> {
            for (Document document : documents) {
                primary.remove(document);
            }
        });
        transactionContext.getJournal().add(journalEntry);

        return result;
    }

    @Override
    public WriteResult update(Filter filter, Document update, UpdateOptions updateOptions) {
        notNull(update, "a null document cannot be used for update");
        notNull(updateOptions, "updateOptions cannot be null");

        WriteResult result;
        try {
            writeLock.lock();
            checkOpened();
            result = collectionOperations.update(filter, update, updateOptions);
        } finally {
            writeLock.unlock();
        }

        List<Document> documentList = new ArrayList<>();

        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setChangeType(ChangeType.Update);
        journalEntry.setCommit(() -> {
            DocumentCursor cursor = primary.find(filter);

            if (!cursor.isEmpty()) {
                if (updateOptions.isJustOnce()) {
                    documentList.add(cursor.firstOrNull());
                } else {
                    documentList.addAll(cursor.toList());
                }
            }
            primary.update(filter, update, updateOptions);
        });
        journalEntry.setRollback(() -> {
            for (Document document : documentList) {
                primary.remove(document);
                primary.insert(document);
            }
        });
        transactionContext.getJournal().add(journalEntry);

        return result;
    }

    @Override
    public WriteResult update(Document document, boolean insertIfAbsent) {
        notNull(document, "a null document cannot be used for update");

        if (insertIfAbsent) {
            return update(createUniqueFilter(document), document, updateOptions(true));
        } else {
            if (document.hasId()) {
                return update(createUniqueFilter(document), document, updateOptions(false));
            } else {
                throw new NotIdentifiableException("update operation failed as no id value found for the document");
            }
        }
    }

    @Override
    public WriteResult remove(Document document) {
        notNull(document, "a null document cannot be removed");

        WriteResult result;
        if (document.hasId()) {
            try {
                writeLock.lock();
                checkOpened();
                result = collectionOperations.remove(document);
            } finally {
                writeLock.unlock();
            }
        } else {
            throw new NotIdentifiableException("remove operation failed as no id value found for the document");
        }

        AtomicReference<Document> toRemove = new AtomicReference<>();

        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setChangeType(ChangeType.Remove);
        journalEntry.setCommit(() -> {
            toRemove.set(primary.getById(document.getId()));
            primary.remove(document);
        });
        journalEntry.setRollback(() -> {
            if (toRemove.get() != null) {
                primary.insert(toRemove.get());
            }
        });
        transactionContext.getJournal().add(journalEntry);

        return result;
    }

    @Override
    public WriteResult remove(Filter filter, boolean justOne) {
        if ((filter == null || filter == Filter.ALL) && justOne) {
            throw new InvalidOperationException("remove all cannot be combined with just once");
        }

        WriteResult result;
        try {
            writeLock.lock();
            checkOpened();
            result = collectionOperations.remove(filter, justOne);
        } finally {
            writeLock.unlock();
        }

        List<Document> documentList = new ArrayList<>();

        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setChangeType(ChangeType.Remove);
        journalEntry.setCommit(() -> {
            DocumentCursor cursor = primary.find(filter);

            if (!cursor.isEmpty()) {
                if (justOne) {
                    documentList.add(cursor.firstOrNull());
                } else {
                    documentList.addAll(cursor.toList());
                }
            }
            primary.remove(filter, justOne);
        });
        journalEntry.setRollback(() -> {
            for (Document document : documentList) {
                primary.insert(document);
            }
        });
        transactionContext.getJournal().add(journalEntry);

        return result;
    }

    @Override
    public DocumentCursor find(Filter filter, FindOptions findOptions) {
        try {
            readLock.lock();
            checkOpened();
            return collectionOperations.find(filter, findOptions);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Document getById(NitriteId nitriteId) {
        notNull(nitriteId, "nitriteId cannot be null");

        try {
            readLock.lock();
            checkOpened();
            return collectionOperations.getById(nitriteId);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String getName() {
        return collectionName;
    }

    @Override
    public void addProcessor(Processor processor) {
        notNull(processor, "a null processor cannot be added");
        try {
            writeLock.lock();
            checkOpened();
            collectionOperations.addProcessor(processor);
        } finally {
            writeLock.unlock();
        }

        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setChangeType(ChangeType.AddProcessor);
        journalEntry.setCommit(() -> primary.addProcessor(processor));
        journalEntry.setRollback(() -> primary.removeProcessor(processor));
        transactionContext.getJournal().add(journalEntry);
    }

    @Override
    public void removeProcessor(Processor processor) {
        notNull(processor, "a null processor cannot be removed");
        try {
            writeLock.lock();
            checkOpened();
            collectionOperations.addProcessor(processor);
        } finally {
            writeLock.unlock();
        }

        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setChangeType(ChangeType.RemoveProcessor);
        journalEntry.setCommit(() -> primary.removeProcessor(processor));
        journalEntry.setRollback(() -> primary.addProcessor(processor));
        transactionContext.getJournal().add(journalEntry);
    }

    @Override
    public void createIndex(IndexOptions indexOptions, String... fieldNames) {
        notNull(fieldNames, "fieldNames cannot be null");

        // by default async is false while creating index
        try {
            Fields fields = Fields.withNames(fieldNames);
            writeLock.lock();
            checkOpened();
            if (indexOptions == null) {
                collectionOperations.createIndex(fields, IndexType.UNIQUE);
            } else {
                collectionOperations.createIndex(fields, indexOptions.getIndexType());
            }
        } finally {
            writeLock.unlock();
        }

        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setChangeType(ChangeType.CreateIndex);
        journalEntry.setCommit(() -> primary.createIndex(indexOptions, fieldNames));
        journalEntry.setRollback(() -> primary.dropIndex(fieldNames));
        transactionContext.getJournal().add(journalEntry);
    }

    @Override
    public void rebuildIndex(String... fieldNames) {
        notNull(fieldNames, "fieldNames cannot be null");

        IndexDescriptor indexDescriptor;
        try {
            Fields fields = Fields.withNames(fieldNames);
            readLock.lock();
            checkOpened();
            indexDescriptor = collectionOperations.findIndex(fields);
        } finally {
            readLock.unlock();
        }

        if (indexDescriptor != null) {
            validateRebuildIndex(indexDescriptor);

            try {
                writeLock.lock();
                checkOpened();
                collectionOperations.rebuildIndex(indexDescriptor);
            } finally {
                writeLock.unlock();
            }
        } else {
            throw new IndexingException(Arrays.toString(fieldNames) + " is not indexed");
        }

        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setChangeType(ChangeType.RebuildIndex);
        journalEntry.setCommit(() -> primary.rebuildIndex(fieldNames));
        journalEntry.setRollback(() -> primary.rebuildIndex(fieldNames));
        transactionContext.getJournal().add(journalEntry);
    }

    @Override
    public Collection<IndexDescriptor> listIndices() {
        try {
            readLock.lock();
            checkOpened();
            return collectionOperations.listIndexes();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean hasIndex(String... fieldNames) {
        notNull(fieldNames, "fieldNames cannot be null");

        try {
            Fields fields = Fields.withNames(fieldNames);
            readLock.lock();
            checkOpened();
            return collectionOperations.hasIndex(fields);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean isIndexing(String... fieldNames) {
        notNull(fieldNames, "fieldNames cannot be null");

        try {
            Fields fields = Fields.withNames(fieldNames);
            readLock.lock();
            checkOpened();
            return collectionOperations.isIndexing(fields);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void dropIndex(String... fieldNames) {
        notNull(fieldNames, "fieldNames cannot be null");

        Fields fields = Fields.withNames(fieldNames);
        try {
            writeLock.lock();
            checkOpened();
            collectionOperations.dropIndex(fields);
        } finally {
            writeLock.unlock();
        }

        final AtomicReference<IndexDescriptor> indexEntry = new AtomicReference<>();

        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setChangeType(ChangeType.DropIndex);
        journalEntry.setCommit(() -> {
            for (IndexDescriptor entry : primary.listIndices()) {
                if (entry.getIndexFields().equals(fields)) {
                    indexEntry.set(entry);
                    break;
                }
            }
            primary.dropIndex(fieldNames);
        });
        journalEntry.setRollback(() -> {
            if (indexEntry.get() != null) {
                primary.createIndex(indexOptions(indexEntry.get().getIndexType()), fieldNames);
            }
        });
        transactionContext.getJournal().add(journalEntry);
    }

    @Override
    public void dropAllIndices() {
        try {
            writeLock.lock();
            checkOpened();
            collectionOperations.dropAllIndices();
        } finally {
            writeLock.unlock();
        }

        List<IndexDescriptor> indexEntries = new ArrayList<>();

        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setChangeType(ChangeType.DropAllIndices);
        journalEntry.setCommit(() -> {
            indexEntries.addAll(primary.listIndices());
            primary.dropAllIndices();
        });
        journalEntry.setRollback(() -> {
            for (IndexDescriptor indexDescriptor : indexEntries) {
                String[] fieldNames = indexDescriptor.getIndexFields().getFieldNames().toArray(new String[0]);
                primary.createIndex(indexOptions(indexDescriptor.getIndexType()), fieldNames);
            }
        });
        transactionContext.getJournal().add(journalEntry);
    }

    @Override
    public void clear() {
        try {
            writeLock.lock();
            checkOpened();
            nitriteMap.clear();
        } finally {
            writeLock.unlock();
        }

        List<Document> documentList = new ArrayList<>();

        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setChangeType(ChangeType.Clear);
        journalEntry.setCommit(() -> {
            documentList.addAll(primary.find().toList());
            primary.clear();
        });
        journalEntry.setRollback(() -> {
            for (Document document : documentList) {
                primary.insert(document);
            }
        });
        transactionContext.getJournal().add(journalEntry);
    }

    @Override
    public void drop() {
        try {
            writeLock.lock();
            checkOpened();
            collectionOperations.dropCollection();
        } finally {
            writeLock.unlock();
        }
        isDropped = true;

        List<Document> documentList = new ArrayList<>();
        List<IndexDescriptor> indexEntries = new ArrayList<>();

        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setChangeType(ChangeType.DropCollection);
        journalEntry.setCommit(() -> {
            documentList.addAll(primary.find().toList());
            indexEntries.addAll(primary.listIndices());
            primary.drop();
        });
        journalEntry.setRollback(() -> {
            NitriteCollection collection = nitrite.getCollection(collectionName);

            for (IndexDescriptor indexDescriptor : indexEntries) {
                String[] fieldNames = indexDescriptor.getIndexFields().getFieldNames().toArray(new String[0]);
                collection.createIndex(indexOptions(indexDescriptor.getIndexType()), fieldNames);
            }

            for (Document document : documentList) {
                collection.insert(document);
            }
        });
        transactionContext.getJournal().add(journalEntry);
    }

    @Override
    public boolean isDropped() {
        return isDropped;
    }

    @Override
    public boolean isOpen() {
        if (nitriteStore == null || nitriteStore.isClosed() || isDropped) {
            try {
                close();
            } catch (Exception e) {
                throw new NitriteIOException("failed to close the database", e);
            }
            return false;
        } else return true;
    }

    @Override
    public synchronized void close() {
        if (collectionOperations != null) {
            collectionOperations.close();
        }
        closeEventBus();
        isClosed = true;
    }

    @Override
    public long size() {
        return find().size();
    }

    @Override
    public NitriteStore<?> getStore() {
        return nitriteStore;
    }

    @Override
    public void subscribe(CollectionEventListener listener) {
        notNull(listener, "listener cannot be null");
        try {
            writeLock.lock();
            checkOpened();
            eventBus.register(listener);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void unsubscribe(CollectionEventListener listener) {
        notNull(listener, "listener cannot be null");
        try {
            writeLock.lock();
            checkOpened();
            if (eventBus != null) {
                eventBus.deregister(listener);
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Attributes getAttributes() {
        try {
            readLock.lock();
            checkOpened();
            return collectionOperations.getAttributes();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void setAttributes(Attributes attributes) {
        notNull(attributes, "attributes cannot be null");

        try {
            writeLock.lock();
            checkOpened();
            collectionOperations.setAttributes(attributes);
        } finally {
            writeLock.unlock();
        }

        AtomicReference<Attributes> original = new AtomicReference<>();

        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setChangeType(ChangeType.SetAttribute);
        journalEntry.setCommit(() -> {
            original.set(primary.getAttributes());
            primary.setAttributes(attributes);
        });
        journalEntry.setRollback(() -> {
            if (original.get() != null) {
                primary.setAttributes(original.get());
            }
        });
        transactionContext.getJournal().add(journalEntry);
    }

    private void initialize() {
        this.collectionName = transactionContext.getCollectionName();
        this.nitriteMap = transactionContext.getNitriteMap();
        NitriteConfig nitriteConfig = transactionContext.getConfig();
        this.nitriteStore = nitriteConfig.getNitriteStore();
        this.isDropped = false;

        ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
        this.readLock = rwLock.readLock();
        this.writeLock = rwLock.writeLock();

        this.eventBus = new CollectionEventBus();
        this.collectionOperations = new CollectionOperations(collectionName, nitriteMap, nitriteConfig, eventBus);
    }

    private static class CollectionEventBus extends NitriteEventBus<CollectionEventInfo<?>, CollectionEventListener> {

        public void post(CollectionEventInfo<?> collectionEventInfo) {
            for (final CollectionEventListener listener : getListeners()) {
                getEventExecutor().submit(() -> listener.onEvent(collectionEventInfo));
            }
        }
    }

    private void checkOpened() {
        if (isClosed) {
            throw new TransactionException("collection is closed");
        }

        if (!primary.isOpen()) {
            throw new NitriteIOException("store is closed");
        }

        if (isDropped()) {
            throw new NitriteIOException("collection has been dropped");
        }

        if (!transactionContext.getActive().get()) {
            throw new TransactionException("transaction is closed");
        }
    }

    private void closeEventBus() {
        if (eventBus != null) {
            eventBus.close();
        }
        eventBus = null;
    }

    private void validateRebuildIndex(IndexDescriptor indexDescriptor) {
        notNull(indexDescriptor, "indexEntry cannot be null");

        String[] fieldNames = indexDescriptor.getIndexFields().getFieldNames().toArray(new String[0]);
        if (isIndexing(fieldNames)) {
            throw new IndexingException("indexing on value " + indexDescriptor.getIndexFields() + " is currently running");
        }
    }
}
