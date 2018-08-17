/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
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
