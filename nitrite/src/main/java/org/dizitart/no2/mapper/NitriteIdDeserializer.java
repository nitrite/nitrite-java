package org.dizitart.no2.mapper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import org.dizitart.no2.NitriteId;

import java.io.IOException;

/**
 * @author Anindya Chatterjee
 */
class NitriteIdDeserializer extends StdScalarDeserializer<NitriteId> {

    NitriteIdDeserializer() {
        super(NitriteId.class);
    }

    @Override
    public NitriteId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return NitriteId.createId(p.getLongValue());
    }
}
