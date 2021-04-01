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

package org.dizitart.no2.module;

import java.util.Set;

import static org.dizitart.no2.common.util.Iterables.setOf;

/**
 * Represents a nitrite plugin modules which may contains
 * one or more nitrite plugins.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
public interface NitriteModule {
    /**
     * Creates a {@link NitriteModule} from a set of {@link NitritePlugin}s.
     *
     * @param plugins the plugins
     * @return the nitrite module
     */
    static NitriteModule module(NitritePlugin... plugins) {
        return () -> setOf(plugins);
    }

    /**
     * Returns the set of {@link NitritePlugin} encapsulated by this module.
     *
     * @return the set
     */
    Set<NitritePlugin> plugins();

    /**
     * Creates a {@link ModuleConfig} to configure a {@link NitriteModule}.
     *
     * @param <T> the type parameter
     * @return the module config
     */
    static <T extends ModuleConfig> T withConfig() {
        return null;
    }

}
