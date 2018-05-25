/*
 *
 * Copyright 2017 Nitrite author or authors.
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

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A jackson based {@link NitriteMapper} implementation. It uses
 * jackson's {@link ObjectMapper} to convert an object into a
 * Nitrite {@link Document}.
 *
 * @author Anindya Chatterjee.
 * @since 1.0
 */
public class GenericMapper extends AbstractMapper {

	private MapperFacade facade;
	
    public GenericMapper(MapperFacade facade) {
    	this.facade = facade;
    }

    public GenericMapper() {
    	this(new JacksonFacade());
    }

    @Override
    public <T> Document asDocumentInternal(T object) {
            return facade.asDocument(object);
    }

    @Override
    public <T> T asObjectInternal(Document document, Class<T> type) {
            return facade.asObject(document, type);
    }

    @Override
    public boolean isValueType(Object object) {
    	return facade.isValueType(object);
    }

    @Override
    public Object asValue(Object object) {
    	return facade.asValue(object);
    }


}
