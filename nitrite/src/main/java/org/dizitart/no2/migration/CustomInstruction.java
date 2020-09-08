package org.dizitart.no2.migration;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.store.NitriteStore;

/**
 * @author Anindya Chatterjee
 */
public interface CustomInstruction {
    void perform(NitriteStore<?> nitriteStore, NitriteConfig nitriteConfigs);
}
