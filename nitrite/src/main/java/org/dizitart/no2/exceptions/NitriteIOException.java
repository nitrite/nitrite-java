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

/**
 * Exception thrown when there is an IO error while performing an operation 
 * in Nitrite database.
 *
 * @author Anindya Chatterjee.
 * @since 1.0
 */
public class NitriteIOException extends NitriteException {
    /**
     * Instantiates a new {@link NitriteIOException}.
     *
     * @param errorMessage the error message
     */
    public NitriteIOException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Instantiates a new {@link NitriteIOException}.
     *
     * @param message the message
     * @param cause   the cause
     */
    public NitriteIOException(String message, Throwable cause) {
        super(message, cause);
    }
}
