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

import org.dizitart.no2.Document;

/**
 * A generic {@link NitriteMapper} implementation. It uses
 * a {@link MapperFacade} implementation to convert an object into a
 * Nitrite {@link Document}.
 *
 * @author Anindya Chatterjee
 * @author Stefan Mandel
 * @since 3.0.1
 */
public class GenericMapper extends AbstractMapper {

    private MapperFacade mapperFacade;

    /**
     * Instantiate a new {@link GenericMapper} with a
     * {@link MapperFacade} instance.
     *
     * @param facade a {@link MapperFacade} implementation
     */
    public GenericMapper(MapperFacade facade) {
        this.mapperFacade = facade;
    }

    @Override
    public <T> Document asDocumentAuto(T object) {
        return mapperFacade.asDocument(object);
    }

    @Override
    public <T> T asObjectAuto(Document document, Class<T> type) {
        return mapperFacade.asObject(document, type);
    }

    @Override
    public boolean isValueType(Object object) {
        return mapperFacade.isValueType(object);
    }

    @Override
    public Object asValue(Object object) {
        return mapperFacade.asValue(object);
    }

    /**
     * Gets the underlying {@link MapperFacade} instance to configure.
     *
     * @return the facade instance.
     */
    public MapperFacade getMapperFacade() {
        return mapperFacade;
    }
}
