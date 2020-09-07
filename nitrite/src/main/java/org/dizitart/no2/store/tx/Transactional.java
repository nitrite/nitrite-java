package org.dizitart.no2.store.tx;

/**
 * @author Anindya Chatterjee
 */
public interface Transactional {
    void commit();
    void rollback();
}
