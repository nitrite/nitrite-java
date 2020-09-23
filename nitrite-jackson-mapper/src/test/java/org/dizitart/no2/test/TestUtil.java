/*
 * Copyright (c) 2019-2020. Nitrite author or authors.
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

package org.dizitart.no2.test;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.module.NitriteModule;
import org.dizitart.no2.mvstore.MVStoreModule;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class TestUtil {

    public static Nitrite createDb(NitriteModule module) {
        MVStoreModule storeModule = MVStoreModule.withConfig()
            .build();

        return Nitrite.builder()
            .loadModule(storeModule)
            .loadModule(module)
            .fieldSeparator(".")
            .openOrCreate();
    }

    public static Nitrite createDb(String user, String password, NitriteModule module) {
        MVStoreModule storeModule = MVStoreModule.withConfig()
            .build();

        return Nitrite.builder()
            .loadModule(storeModule)
            .loadModule(module)
            .fieldSeparator(".")
            .openOrCreate(user, password);
    }

    public static Nitrite createDb(String filePath, NitriteModule module) {
        return Nitrite.builder()
            .loadModule(new MVStoreModule(filePath))
            .loadModule(module)
            .fieldSeparator(".")
            .openOrCreate();
    }

    public static Nitrite createDb(String filePath, String user, String password, NitriteModule module) {
        MVStoreModule storeModule = MVStoreModule.withConfig()
            .filePath(filePath)
            .compress(true)
            .build();

        return Nitrite.builder()
            .loadModule(storeModule)
            .loadModule(module)
            .fieldSeparator(".")
            .openOrCreate(user, password);
    }
}
