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

package org.dizitart.no2.mapper;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dizitart.no2.Document;

import java.util.Set;

/**
 * A jackson based {@link GenericMapper} implementation. It uses
 * {@link JacksonFacade} to convert an object into a
 * Nitrite {@link Document}.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 */
public class JacksonMapper extends GenericMapper {

    /**
     * Instantiates a new {@link JacksonMapper} with a {@link JacksonFacade}.
     */
    public JacksonMapper() {
        this(new JacksonFacade());
    }

    /**
     * Instantiates a new {@link JacksonMapper} with a {@link JacksonFacade}
     * and register jackson {@link Module} specified by `modules`.
     *
     * @param modules jackson {@link Module} to register
     */
    public JacksonMapper(Set<Module> modules) {
        this(new JacksonFacade(modules));
    }

    /**
     * Instantiates a new {@link JacksonMapper} with a {@link MapperFacade}.
     *
     * @param mapperFacade the mapper facade
     */
    public JacksonMapper(MapperFacade mapperFacade) {
        super(mapperFacade);
    }

    /**
     * Gets the underlying {@link ObjectMapper} instance to configure.
     *
     * @return the object mapper instance.
     */
    public ObjectMapper getObjectMapper() {
        JacksonFacade jacksonFacade = (JacksonFacade) getMapperFacade();
        return jacksonFacade.getObjectMapper();
    }
}
