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

import lombok.Getter;

/**
 * Represents a generic nitrite database runtime error.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 */
@Getter
public class NitriteException extends RuntimeException {

    /**
     * Instantiates a new Nitrite exception.
     *
     * @param errorMessage the error message
     */
    public NitriteException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Instantiates a new Nitrite exception.
     *
     * @param errorMessage the error message
     * @param cause        the cause
     */
    public NitriteException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}
