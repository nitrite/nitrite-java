package org.dizitart.no2.store;

import org.dizitart.no2.module.NitriteModule;

/**
 * Represents a nitrite store module to load as a storage engine for the database.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
public interface StoreModule extends NitriteModule {
    /**
     * Gets the {@link NitriteStore} instance from this module.
     *
     * @return the store
     */
    NitriteStore<?> getStore();
}
