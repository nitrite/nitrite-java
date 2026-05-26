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

import org.dizitart.no2.mapper.jackson.JacksonMapper
import org.dizitart.no2.spatial.jackson.GeometryModule
import tools.jackson.databind.JacksonModule
import tools.jackson.module.kotlin.KotlinModule

/**
 * A custom Jackson mapper for Potassium Nitrite (KNO2) library that extends [JacksonMapper] class.
 * 
 * @param modules a vararg of Jackson modules to be registered with the mapper. 
 * @author Anindya Chatterjee
 * @author Stefan Mandel
 * @since 2.1.0
 */
open class KNO2JacksonMapper(private vararg val modules: JacksonModule) : JacksonMapper() {

    init {
        registerJacksonModule(KotlinModule.Builder().build())
        registerJacksonModule(GeometryModule())
        for (module in modules) {
            registerJacksonModule(module)
        }
    }
}

