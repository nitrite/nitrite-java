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
 * Represents the configuration interface of a {@link NitriteStore}.
 *
 * @author Anindya Chatterjee.
 * @since 4.0
 */
public interface StoreConfig {
    /**
     * Gets the file path of the store.
     *
     * @return the file path of the store.
     */
    String filePath();

    /**
     * Returns a boolean indicating whether the store is read-only or not.
     *
     * @return a boolean indicating whether the store is read-only or not
     */
    Boolean isReadOnly();

    /**
     * Adds a {@link StoreEventListener} to the store configuration. 
     * The listener will be notified of any store events.
     *
     * @param listener the listener to add
     */
    void addStoreEventListener(StoreEventListener listener);

    /**
     * Checks if the store is in-memory.
     *
     * @return {@code true} if the store is in-memory; {@code false} otherwise.
     */
    default boolean isInMemory() {
        return StringUtils.isNullOrEmpty(filePath());
    }
}
