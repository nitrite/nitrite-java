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

import org.dizitart.no2.mapper.JacksonModule
import org.dizitart.no2.module.NitriteModule
import org.dizitart.no2.module.NitritePlugin
import org.dizitart.no2.spatial.SpatialIndexer

/**
 *
 * @author Anindya Chatterjee
 */
open class KNO2Module(private vararg val modules: JacksonModule) : NitriteModule {

    override fun plugins(): MutableSet<NitritePlugin> {
        return setOf(KNO2JacksonMapper(*modules), SpatialIndexer())
    }
}