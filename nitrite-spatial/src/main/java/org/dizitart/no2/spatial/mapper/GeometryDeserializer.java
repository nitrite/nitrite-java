/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
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

package org.dizitart.no2.spatial.mapper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.io.IOException;

import static org.dizitart.no2.spatial.mapper.GeometryModule.GEOMETRY_ID;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
class GeometryDeserializer extends StdScalarDeserializer<Geometry> {

    protected GeometryDeserializer() {
        super(Geometry.class);
    }

    @Override
    public Geometry deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        WKTReader reader = new WKTReader();
        try {
            if (value.contains(GEOMETRY_ID)) {
                String geometry = value.replace(GEOMETRY_ID, "");
                return reader.read(geometry);
            } else {
                throw new ParseException("Not a valid geometry value " + value);
            }
        } catch (ParseException e) {
            log.error("Error while parsing WKT geometry string", e);
            throw new IOException(e);
        }
    }
}
