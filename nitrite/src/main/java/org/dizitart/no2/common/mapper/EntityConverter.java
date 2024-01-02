/*
 * Copyright (c) 2017-2022 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.common.mapper;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.module.NitritePlugin;

/**
 * The {@link EntityConverter} interface is used to convert 
 * an entity of type {@link T} into a database {@link Document}
 * and vice versa.
 *
 * @since 4.0
 * @author Anindya Chatterjee
 * @param <T> the type parameter
 */
public interface EntityConverter<T> extends NitritePlugin {
    /**
     * Gets the entity type.
     *
     * @return the entity type
     */
    Class<T> getEntityType();

    /**
     * Converts the entity to a {@link Document}.
     *
     * @param entity        the entity
     * @param nitriteMapper the nitrite mapper
     * @return the document
     */
    Document toDocument(T entity, NitriteMapper nitriteMapper);

    /**
     * Converts a {@link Document} to an entity of type {@link T}.
     *
     * @param document      the document
     * @param nitriteMapper the nitrite mapper
     * @return the t
     */
    T fromDocument(Document document, NitriteMapper nitriteMapper);

    @Override
    default void initialize(NitriteConfig nitriteConfig) {}
}
