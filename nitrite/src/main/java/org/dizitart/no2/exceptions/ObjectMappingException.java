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

package org.dizitart.no2.exceptions;

import org.dizitart.no2.collection.Document;

/**
 * Exception thrown when there is an error mapping an object to a 
 * document or vice versa.
 *
 * @author Anindya Chatterjee.
 * @since 1.0
 */
public class ObjectMappingException extends NitriteException {

    /**
     * Instantiates a new Object mapping exception.
     *
     * @param message the message
     */
    public ObjectMappingException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Object mapping exception.
     *
     * @param message the message
     * @param cause   the inner cause
     */
    public ObjectMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}
