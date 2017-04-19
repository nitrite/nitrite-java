package org.dizitart.no2;

/**
 * An {@link Iterable} that can be reset.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
public interface Resettable<T> extends Iterable<T> {
    /**
     * Resets the iterator to the beginning after reading it.
     *
     * */
    void reset();
}
