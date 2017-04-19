package org.dizitart.no2;

/**
 * An interface to represent the result of a modification operation
 * on {@link NitriteCollection}. It provides a means to iterate over
 * all affected ids in the collection.
 *
 * @author Anindya Chatterjee.
 * @since 1.0
 */
public interface WriteResult extends Iterable<NitriteId> {

    /**
     * Gets the count of affected document in the collection.
     *
     * @return the affected document count.
     */
    int getAffectedCount();
}
