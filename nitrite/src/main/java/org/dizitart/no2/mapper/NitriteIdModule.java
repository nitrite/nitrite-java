package org.dizitart.no2.mapper;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.dizitart.no2.NitriteId;

/**
 * @author Anindya Chatterjee
 */
public class NitriteIdModule extends SimpleModule {

    @Override
    public void setupModule(SetupContext context) {
        addSerializer(NitriteId.class, new NitriteIdSerializer());
        addDeserializer(NitriteId.class, new NitriteIdDeserializer());
        super.setupModule(context);
    }
}
