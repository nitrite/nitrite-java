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
import org.dizitart.no2.exceptions.ObjectMappingException;

/**
 * Represents an object mapper for nitrite database. It
 * converts an object into a Nitrite {@link Document}.
 *
 * @author Anindya Chatterjee
 * @author Stefan Mandel
 * @since 1.0
 */
public interface NitriteMapper {
    /**
     * Converts and `object` to a {@link Document}.
     *
     * @param <T>    the type parameter
     * @param object the object
     * @return the document
     * @throws ObjectMappingException if circular reference found.
     */
    <T> Document asDocument(T object);

    /**
     * Tries to convert a `document` to an object of type `T`.
     *
     * @param <T>      the type parameter
     * @param document the document
     * @param type     the type of the object
     * @return the object
     * @throws IllegalArgumentException if conversion fails due to incompatible type.
     * @throws ObjectMappingException   if no public parameter-less constructor found.
     */
    <T> T asObject(Document document, Class<T> type);

    /**
     * Determines if an object would be stored as a value type.
     *
     * @param object the object to check
     * @return `true` of `object` would be stored as a value type; `false` otherwise.
     */
    boolean isValueType(Object object);

    /**
     * Tries to convert an `object` to a value type, which will be stored in
     * the document.
     * 
     * [icon="{@docRoot}/note.png"]
     * NOTE: As an example, a {@link java.util.Date} object is stored
     * as a {@link java.lang.Long} value in the document. This operation
     * will return the long value of a {@link java.util.Date} object.
     *
     * @param object the object to convert
     * @return the object as a value type.
     */
    Object asValue(Object object);

}
