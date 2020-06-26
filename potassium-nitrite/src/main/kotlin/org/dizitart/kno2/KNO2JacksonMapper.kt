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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.dizitart.no2.mapper.JacksonMapper
import org.dizitart.no2.mapper.JacksonModule
import org.dizitart.no2.spatial.mapper.GeometryModule
import java.time.ZoneId
import java.time.chrono.ChronoPeriod
import java.time.temporal.Temporal
import java.time.temporal.TemporalAdjuster
import java.time.temporal.TemporalAmount
import java.util.*

/**
 * Default [JacksonMapper] for potassium nitrite.
 *
 * @author Anindya Chatterjee
 * @author Stefan Mandel
 * @since 2.1.0
 */
open class KNO2JacksonMapper(vararg modules: JacksonModule) : JacksonMapper(GeometryModule(), *modules) {

    override fun createObjectMapper(): ObjectMapper {
        // add value types from JavaTimeModule
        addValueType(Temporal::class.java)
        addValueType(TemporalAdjuster::class.java)
        addValueType(TemporalAmount::class.java)
        addValueType(ChronoPeriod::class.java)
        addValueType(ZoneId::class.java)

        // add value types from Jdk8Module
        addValueType(OptionalInt::class.java)
        addValueType(OptionalLong::class.java)
        addValueType(OptionalDouble::class.java)

        val objectMapper = super.createObjectMapper()
        objectMapper.registerModule(KotlinModule())
        objectMapper.registerModule(Jdk8Module())
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

        return objectMapper
    }
}

