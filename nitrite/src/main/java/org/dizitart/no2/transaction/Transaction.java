package org.dizitart.no2.transaction;

import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.repository.ObjectRepository;

import java.io.Closeable;

/**
 * @author Anindya Chatterjee
 */
public interface Transaction extends Closeable {
    String getId();

    State getState();

    NitriteCollection getCollection(String name);

    <T> ObjectRepository<T> getRepository(Class<T> type);

    <T> ObjectRepository<T> getRepository(Class<T> type, String key);

    void commit();

    void rollback();

    void close();
}
