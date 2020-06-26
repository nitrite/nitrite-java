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

package org.dizitart.no2.mapper;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.dizitart.no2.collection.NitriteId;

import java.util.List;

import static org.dizitart.no2.common.util.Iterables.listOf;

/**
 * Class that registers capability of serializing {@code NitriteId} with the Jackson core.
 *
 * @author Anindya Chatterjee
 * @since 1.0.0
 */
public class NitriteIdModule implements JacksonModule {

    @Override
    public List<Class<?>> getDataTypes() {
        return listOf(NitriteId.class);
    }

    @Override
    public Module getModule() {
        return new SimpleModule() {
            @Override
            public void setupModule(SetupContext context) {
                addSerializer(NitriteId.class, new NitriteIdSerializer());
                addDeserializer(NitriteId.class, new NitriteIdDeserializer());
                super.setupModule(context);
            }
        };
    }
}
