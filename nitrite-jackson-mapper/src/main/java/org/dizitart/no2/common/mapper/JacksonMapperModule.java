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

package org.dizitart.no2.common.mapper;

import com.fasterxml.jackson.databind.Module;
import org.dizitart.no2.common.module.NitriteModule;
import org.dizitart.no2.common.module.NitritePlugin;

import java.util.Set;

import static org.dizitart.no2.common.util.Iterables.setOf;

/**
 * A Nitrite module that provides a jackson based {@link NitriteMapper}
 * implementation for object to document conversion.
 * 
 * @since 4.0
 * @see NitriteMapper
 * @see JacksonMapper
 * @author Anindya Chatterjee
 */
public class JacksonMapperModule implements NitriteModule {
    private final JacksonMapper jacksonMapper;

    /**
     * Instantiates a new {@link JacksonMapperModule}.
     */
    public JacksonMapperModule() {
        jacksonMapper = new JacksonMapper();
    }

    /**
     * Instantiates a new {@link JacksonMapperModule} with custom
     * jackson modules.
     *
     * @param jacksonModules the jackson modules
     */
    public JacksonMapperModule(Module... jacksonModules) {
        jacksonMapper = new JacksonMapper();
        for (Module jacksonModule : jacksonModules) {
            jacksonMapper.registerJacksonModule(jacksonModule);
        }
    }

    @Override
    public Set<NitritePlugin> plugins() {
        return setOf(jacksonMapper);
    }
}
