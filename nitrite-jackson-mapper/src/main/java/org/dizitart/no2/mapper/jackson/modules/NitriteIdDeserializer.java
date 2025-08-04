/*
 * Copyright (c) 2019-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.mapper.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import org.dizitart.no2.collection.NitriteId;

import java.io.IOException;

/**
 * @since 4.0
 * @author Anindya Chatterjee
 */
class NitriteIdDeserializer extends StdScalarDeserializer<NitriteId> {

    NitriteIdDeserializer() {
        super(NitriteId.class);
    }

    @Override
    public NitriteId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return NitriteId.createId(p.getValueAsString());
    }
}
