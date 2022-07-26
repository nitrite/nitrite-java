package org.dizitart.no2.transaction;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.common.mapper.NitriteMapper;

/**
 * @author Anindya Chatterjee
 * @since 4.0
 */
@Slf4j
class TransactionConfig extends NitriteConfig {
    private final NitriteConfig config;

    public TransactionConfig(NitriteConfig config) {
        super();
        this.config = config;
    }

    @Override
    public void fieldSeparator(String separator) {
        config.fieldSeparator(separator);
    }

    @Override
    public NitriteMapper nitriteMapper() {
        return config.nitriteMapper();
    }

    @Override
    public void initialize() {
        super.initialize();
    }
}
