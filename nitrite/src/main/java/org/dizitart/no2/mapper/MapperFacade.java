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
 * A facade for serializers to convert an object to a {@link Document}
 * and vice versa.
 *
 * @author Stefan Mandel
 * @author Anindya Chatterjee
 * @since 3.0.1
 */
public interface MapperFacade extends NitriteMapper {

	/**
	 * Parses a json string into a nitrite {@link Document}.
	 *
	 * @param json the json string to parse
	 * @return the document
	 */
	Document parse(String json);

	/**
	 * Serializes an object to a json string
	 *
	 * @param object the object
	 * @return the json string
	 */
	String toJson(Object object);

}
