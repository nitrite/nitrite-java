/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.mvstore;

import org.h2.mvstore.MVStoreException;

import static org.h2.mvstore.DataUtils.ERROR_SERIALIZATION;

/**
 * Thrown when a stored object is refused by the deserialization allowlist of
 * {@link FilteredObjectDataType}. Unlike other serialization errors this is not
 * a sign of a corrupt file, so the store must not roll back on it. It extends
 * {@link MVStoreException} so that h2 passes it through instead of reporting
 * the page as corrupt.
 *
 * @since 5.4.1
 */
class SerialFilterRejectedException extends MVStoreException {
    private static final long serialVersionUID = 1762953123L;

    SerialFilterRejectedException(String message) {
        super(ERROR_SERIALIZATION, message);
    }

    SerialFilterRejectedException(String message, Throwable cause) {
        this(message);
        initCause(cause);
    }
}
