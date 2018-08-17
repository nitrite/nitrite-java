/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.collection.objects;

import lombok.experimental.UtilityClass;
import org.dizitart.no2.NitriteContext;
import org.dizitart.no2.collection.NitriteCollection;

/**
 * A factory class to open a {@link ObjectRepository}.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
@UtilityClass
public class RepositoryFactory {

    /**
     * Opens an object repository for a specific `type`.
     *
     * @param <T>               the type of the object to store
     * @param type              the type
     * @param collection        the underlying {@link NitriteCollection}
     * @param nitriteContext    the nitrite context
     * @return the object repository
     */
    public static <T> ObjectRepository<T> open(Class<T> type,
                                               NitriteCollection collection,
                                               NitriteContext nitriteContext) {
        return new DefaultObjectRepository<>(type, collection, nitriteContext);
    }
}
