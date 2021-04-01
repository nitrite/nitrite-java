package org.dizitart.no2.transaction;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.processors.Processor;
import org.dizitart.no2.repository.Cursor;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.repository.RepositoryOperations;
import org.dizitart.no2.store.NitriteStore;

import java.util.Collection;

import static org.dizitart.no2.collection.UpdateOptions.updateOptions;
import static org.dizitart.no2.common.util.ValidationUtils.containsNull;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * @author Anindya Chatterjee
 * @since 4.0
 */
class DefaultTransactionalRepository<T> implements ObjectRepository<T> {
    private final Class<T> type;
    private final ObjectRepository<T> primary;
    private final NitriteCollection backingCollection;
    private final NitriteConfig nitriteConfig;
    private RepositoryOperations operations;

    public DefaultTransactionalRepository(Class<T> type,
                                          ObjectRepository<T> primary,
                                          NitriteCollection backingCollection,
                                          NitriteConfig nitriteConfig) {
        this.type = type;
        this.primary = primary;
        this.backingCollection = backingCollection;
        this.nitriteConfig = nitriteConfig;
        initialize();
    }

    @Override
    public void addProcessor(Processor processor) {
        backingCollection.addProcessor(processor);
    }

    @Override
    public void removeProcessor(Processor processor) {
        backingCollection.removeProcessor(processor);
    }

    @Override
    public void createIndex(IndexOptions indexOptions, String... fieldNames) {
        backingCollection.createIndex(indexOptions, fieldNames);
    }

    @Override
    public void rebuildIndex(String... fieldNames) {
        backingCollection.rebuildIndex(fieldNames);
    }

    @Override
    public Collection<IndexDescriptor> listIndices() {
        return backingCollection.listIndices();
    }

    @Override
    public boolean hasIndex(String... fieldNames) {
        return backingCollection.hasIndex(fieldNames);
    }

    @Override
    public boolean isIndexing(String... fieldNames) {
        return backingCollection.isIndexing(fieldNames);
    }

    @Override
    public void dropIndex(String... fieldNames) {
        backingCollection.dropIndex(fieldNames);
    }

    @Override
    public void dropAllIndices() {
        backingCollection.dropAllIndices();
    }

    @Override
    public WriteResult insert(T[] elements) {
        notNull(elements, "a null object cannot be inserted");
        containsNull(elements, "a null object cannot be inserted");

        return backingCollection.insert(operations.toDocuments(elements));
    }

    @Override
    public WriteResult update(T element, boolean insertIfAbsent) {
        notNull(element, "a null object cannot be used for update");

        return update(operations.createUniqueFilter(element), element, insertIfAbsent);
    }

    @Override
    public WriteResult update(Filter filter, T update, boolean insertIfAbsent) {
        notNull(update, "a null object cannot be used for update");

        Document updateDocument = operations.toDocument(update, true);
        if (!insertIfAbsent) {
            operations.removeNitriteId(updateDocument);
        }
        return backingCollection.update(operations.asObjectFilter(filter), updateDocument,
            updateOptions(insertIfAbsent, true));
    }

    @Override
    public WriteResult update(Filter filter, Document update, boolean justOnce) {
        notNull(update, "a null document cannot be used for update");
        operations.removeNitriteId(update);
        operations.serializeFields(update);

        return backingCollection.update(operations.asObjectFilter(filter), update, updateOptions(false, justOnce));
    }

    @Override
    public WriteResult remove(T element) {
        notNull(element, "a null object cannot be removed");
        return remove(operations.createUniqueFilter(element));
    }

    @Override
    public WriteResult remove(Filter filter, boolean justOne) {
        return backingCollection.remove(operations.asObjectFilter(filter), justOne);
    }

    @Override
    public void clear() {
        backingCollection.clear();
    }

    @Override
    public Cursor<T> find(Filter filter, FindOptions findOptions) {
        return operations.find(filter, findOptions, type);
    }

    @Override
    public <I> T getById(I id) {
        T item = primary == null ? null : primary.getById(id);
        if (item == null) {
            Filter idFilter = operations.createIdFilter(id);
            return find(idFilter).firstOrNull();
        }
        return item;
    }

    @Override
    public void drop() {
        backingCollection.drop();
    }

    @Override
    public boolean isDropped() {
        return backingCollection.isDropped();
    }

    @Override
    public boolean isOpen() {
        return backingCollection.isOpen();
    }

    @Override
    public void close() throws Exception {
        backingCollection.close();
    }

    @Override
    public long size() {
        return backingCollection.size();
    }

    @Override
    public NitriteStore<?> getStore() {
        return backingCollection.getStore();
    }

    @Override
    public void subscribe(CollectionEventListener listener) {
        backingCollection.subscribe(listener);
    }

    @Override
    public void unsubscribe(CollectionEventListener listener) {
        backingCollection.unsubscribe(listener);
    }

    @Override
    public Attributes getAttributes() {
        return backingCollection.getAttributes();
    }

    @Override
    public void setAttributes(Attributes attributes) {
        backingCollection.setAttributes(attributes);
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public NitriteCollection getDocumentCollection() {
        return backingCollection;
    }

    private void initialize() {
        NitriteMapper nitriteMapper = nitriteConfig.nitriteMapper();
        this.operations = new RepositoryOperations(type, nitriteMapper, backingCollection);
        this.operations.createIndexes();
    }
}
