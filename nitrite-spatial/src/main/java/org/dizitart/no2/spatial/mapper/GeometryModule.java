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

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.dizitart.no2.mapper.JacksonModule;
import org.locationtech.jts.geom.Geometry;

import java.util.List;

import static org.dizitart.no2.common.util.Iterables.listOf;

/**
 * Class that registers capability of serializing {@code Geometry} objects with the Jackson core.
 *
 * @author Anindya Chatterjee
 * @since 4.0.0
 */
public class GeometryModule implements JacksonModule {
    /**
     * The constant GEOMETRY_ID
     */
    public static final String GEOMETRY_ID = "geometry:";

    @Override
    public List<Class<?>> getDataTypes() {
        return listOf(Geometry.class);
    }

    @Override
    public Module getModule() {
        return new SimpleModule() {
            @Override
            public void setupModule(SetupContext context) {
                addSerializer(Geometry.class, new GeometrySerializer());
                addDeserializer(Geometry.class, new GeometryDeserializer());
                super.setupModule(context);
            }
        };
    }
}
