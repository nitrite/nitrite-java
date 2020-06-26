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

package org.dizitart.no2.store;

import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.store.events.StoreEventListener;

/**
 * Represents a {@link NitriteStore} configuration.
 *
 * @author Anindya Chatterjee.
 * @since 4.0.0
 */
public interface StoreConfig {
    /**
     * Gets file path for the store.
     *
     * @return the file path
     */
    String getFilePath();

    /**
     * Indicates if the {@link NitriteStore} is a readonly store.
     *
     * @return `true`, if readonly store; otherwise `false`.
     */
    boolean isReadOnly();

    /**
     * Adds a {@link StoreEventListener} instance and subscribe it to store event.
     */
    void addStoreEventListener(StoreEventListener listener);

    /**
     * Indicates if the {@link NitriteStore} is an in-memory store.
     *
     * @return `true`, if in-memory store; otherwise `false`.
     */
    default boolean isInMemory() {
        return StringUtils.isNullOrEmpty(getFilePath());
    }
}
