package org.dizitart.no2.mapper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.dizitart.no2.NitriteId;

import java.io.IOException;

/**
 * @author Anindya Chatterjee
 */
class NitriteIdSerializer extends StdScalarSerializer<NitriteId> {

    protected NitriteIdSerializer() {
        super(NitriteId.class);
    }

    @Override
    public void serialize(NitriteId value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value.getIdValue() != null) {
            gen.writeNumber(value.getIdValue());
        }
    }
}
