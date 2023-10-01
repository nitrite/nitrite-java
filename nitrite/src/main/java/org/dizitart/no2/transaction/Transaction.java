package org.dizitart.no2.transaction;

import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.repository.EntityDecorator;
import org.dizitart.no2.index.IndexOptions;

/**
 * Represents a transaction in Nitrite database. It provides methods to perform
 * ACID operations
 * on Nitrite database collections and repositories.
 * <p>
 * A transaction can be committed or rolled back. Once a transaction is
 * committed, all changes
 * made during the transaction are persisted to the underlying store. If a
 * transaction is
 * rolled back, all changes made during the transaction are discarded.
 * <p>
 * 
 * NOTE: Certain operations are auto-committed in Nitrite database. Those
 * operations are not
 * part of a transaction and cannot be rolled back. The following operations are
 * auto-committed:
 * 
 * <ul>
 * <li>{@link NitriteCollection#createIndex(String...)}</li>
 * <li>{@link NitriteCollection#createIndex(IndexOptions, String...)}</li>
 * <li>{@link NitriteCollection#rebuildIndex(String...)}</li>
 * <li>{@link NitriteCollection#dropIndex(String...)}</li>
 * <li>{@link NitriteCollection#dropAllIndices()}</li>
 * <li>{@link NitriteCollection#clear()}</li>
 * <li>{@link NitriteCollection#drop()}</li>
 * <li>{@link NitriteCollection#close()}</li>
 * 
 * <li>{@link ObjectRepository#createIndex(String...)}</li>
 * <li>{@link ObjectRepository#createIndex(IndexOptions, String...)}</li>
 * <li>{@link ObjectRepository#rebuildIndex(String...)}</li>
 * <li>{@link ObjectRepository#dropIndex(String...)}</li>
 * <li>{@link ObjectRepository#dropAllIndices()}</li>
 * <li>{@link ObjectRepository#clear()}</li>
 * <li>{@link ObjectRepository#drop()}</li>
 * <li>{@link ObjectRepository#close()}</li>
 * 
 * 
 * @author Anindya Chatterjee
 * @since 4.0
 */
public interface Transaction extends AutoCloseable {
    /**
     * Gets the unique identifier of the transaction.
     *
     * @return the unique identifier of the transaction.
     */
    String getId();

    /**
     * Returns the current state of the transaction.
     *
     * @return the current state of the transaction.
     */
    TransactionState getState();

    /**
     * Gets a {@link NitriteCollection} to perform ACID operations on it.
     *
     * @param name the name
     * @return the collection
     */
    NitriteCollection getCollection(String name);

    /**
     * Gets an {@link ObjectRepository} to perform ACID operations on it.
     *
     * @param <T>  the type parameter
     * @param type the type
     * @return the repository
     */
    <T> ObjectRepository<T> getRepository(Class<T> type);

    /**
     * Gets an {@link ObjectRepository} to perform ACID operations on it.
     *
     * @param <T>  the type parameter
     * @param type the type
     * @param key  the key
     * @return the repository
     */
    <T> ObjectRepository<T> getRepository(Class<T> type, String key);

    /**
     * Gets an {@link ObjectRepository} to perform ACID operations on it.
     *
     * @param <T>             the type parameter
     * @param entityDecorator the entityDecorator
     * @return the repository
     */
    <T> ObjectRepository<T> getRepository(EntityDecorator<T> entityDecorator);

    /**
     * Gets an {@link ObjectRepository} to perform ACID operations on it.
     *
     * @param <T>             the type parameter
     * @param entityDecorator the entityDecorator
     * @param key             the key
     * @return the repository
     */
    <T> ObjectRepository<T> getRepository(EntityDecorator<T> entityDecorator, String key);

    /**
     * Completes the transaction and commits the data to the underlying store.
     */
    void commit();

    /**
     * Rolls back the transaction, discarding any changes made during the transaction.
     */
    void rollback();

    /**
     * Closes this {@link Transaction}.
     */
    void close();
}
