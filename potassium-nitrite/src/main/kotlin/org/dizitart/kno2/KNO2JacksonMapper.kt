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
import org.dizitart.no2.mapper.GenericMapper
import org.dizitart.no2.mapper.MapperFacade

/**
 * Default [GenericMapper] for potassium nitrite.
 *
 * @author Anindya Chatterjee
 * @author Stefan Mandel
 * @since 2.1.0
 */
open class KNO2JacksonMapper(mapperFacade: MapperFacade) : GenericMapper(mapperFacade) {
    constructor() : this(KNO2JacksonFacade())
    constructor(modules: Set<Module>) : this(KNO2JacksonFacade(modules))
}