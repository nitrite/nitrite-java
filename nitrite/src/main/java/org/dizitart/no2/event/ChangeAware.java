package org.dizitart.no2.event;

/**
 * Interface to be implemented by collections that wish to be aware
 * of any data modification.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 * @see ChangeType
 */
public interface ChangeAware {
    /**
     * Registers a {@link ChangeListener} instance to listen to any
     * changes.
     *
     * @param listener the listener
     */
    void register(ChangeListener listener);

    /**
     * De-registers an already registered {@link ChangeListener} instance
     * to listen to any changes.
     *
     * @param listener the listener
     */
    void deregister(ChangeListener listener);
}
