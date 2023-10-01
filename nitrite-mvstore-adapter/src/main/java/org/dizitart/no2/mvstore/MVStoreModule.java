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

package org.dizitart.no2.mvstore;

import lombok.AccessLevel;
import lombok.Setter;
import org.dizitart.no2.common.module.NitritePlugin;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.store.StoreModule;

import java.util.Set;

import static org.dizitart.no2.common.util.Iterables.setOf;

/**
 * A Nitrite module that provides a Nitrite store implementation using H2 MVStore.
 * 
 * @since 4.0
 * @see NitriteStore
 * @author Anindya Chatterjee
 */
public class MVStoreModule implements StoreModule {
    @Setter(AccessLevel.PACKAGE)
    /**
     * The configuration object for the MVStore.
     */
    private MVStoreConfig storeConfig;

    /**
     * Constructs a new instance of {@link MVStoreModule} with the specified file path.
     * 
     * @param path the file path for the MVStore database.
     */
    public MVStoreModule(String path) {
        this.storeConfig = new MVStoreConfig();
        this.storeConfig.filePath(path);
    }

    @Override
    public Set<NitritePlugin> plugins() {
        return setOf(getStore());
    }

    /**
     * Returns a new instance of {@link MVStoreModuleBuilder} to configure the MVStore module.
     *
     * @return a new instance of {@link MVStoreModuleBuilder}.
     */
    public static MVStoreModuleBuilder withConfig() {
        return new MVStoreModuleBuilder();
    }

    /**
     * Returns a new instance of {@link NitriteStore} with the configured {@link StoreConfig}.
     *
     * @return a new instance of {@link NitriteStore}.
     */
    public NitriteStore<?> getStore() {
        NitriteMVStore store = new NitriteMVStore();
        store.setStoreConfig(storeConfig);
        return store;
    }
}
