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

package org.dizitart.no2.common.module;

import org.dizitart.no2.NitriteConfig;

/**
 * Represents a plugin for working with Nitrite 
 * database and provides methods for initializing and closing the plugin.
 *
 * @author Anindya Chatterjee.
 * @since 4.0
 */
public interface NitritePlugin extends AutoCloseable {
    /**
     * Initializes the plugin instance.
     *
     * @param nitriteConfig the nitrite config
     */
    void initialize(NitriteConfig nitriteConfig);

    /**
     * Closes the plugin instance.
     */
    default void close() {
        // this is to make NitritePlugin a functional interface
        // and make close() not throw checked exception
    }
}
