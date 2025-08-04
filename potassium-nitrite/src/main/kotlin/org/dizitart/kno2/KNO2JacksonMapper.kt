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

package org.dizitart.kno2

import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.dizitart.no2.mapper.jackson.JacksonMapper
import org.dizitart.no2.spatial.jackson.GeometryModule

/**
 * A custom Jackson mapper for Potassium Nitrite (KNO2) library that extends [JacksonMapper] class.
 * 
 * @param modules a vararg of Jackson modules to be registered with the mapper. 
 * @author Anindya Chatterjee
 * @author Stefan Mandel
 * @since 2.1.0
 */
open class KNO2JacksonMapper(private vararg val modules: Module) : JacksonMapper() {

    override fun getObjectMapper(): ObjectMapper {
        val objectMapper = super.getObjectMapper()
        objectMapper.registerModule(KotlinModule.Builder().build())
        objectMapper.registerModule(Jdk8Module())
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.registerModule(GeometryModule())
        for (module in modules) {
            objectMapper.registerModule(module)
        }

        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

        return objectMapper
    }
}

