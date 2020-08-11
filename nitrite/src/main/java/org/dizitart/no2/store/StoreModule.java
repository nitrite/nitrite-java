package org.dizitart.no2.store;

import org.dizitart.no2.module.NitriteModule;

/**
 * @author Anindya Chatterjee
 */
public interface StoreModule extends NitriteModule {
    NitriteStore<?> getStore();
}
