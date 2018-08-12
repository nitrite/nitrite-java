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

package org.dizitart.kno2

import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.dizitart.no2.mapper.JacksonFacade

/**
 * Default [JacksonFacade] for potassium nitrite.
 *
 * @author Stefan Mandel
 * @author Anindya Chatterjee
 * @since 3.0.1
 */
open class KNO2JacksonFacade(modules: Set<Module>? = setOf()) : JacksonFacade(modules) {

    override fun createObjectMapper(): ObjectMapper {
        val objectMapper = super.createObjectMapper()
        objectMapper.registerModule(KotlinModule())
        objectMapper.registerModule(Jdk8Module())
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        return objectMapper
    }
}