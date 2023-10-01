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

package org.dizitart.no2.repository;

import java.util.List;

/**
 * An interface that can be used to implement a decorator 
 * for an entity class of type <code>T<code>, where annotating 
 * the class with {@link org.dizitart.no2.repository.annotations.Entity}
 * and its friends is not possible.
 *
 * @param <T> the type parameter
 * @see org.dizitart.no2.Nitrite#getRepository(EntityDecorator)
 * @see org.dizitart.no2.Nitrite#getRepository(EntityDecorator, String)
 * @since 4.0
 * @author Anindya Chatterjee
 */
public interface EntityDecorator<T> {
    /**
     * Gets the entity type of the decorator.
     *
     * @return the entity type
     */
    Class<T> getEntityType();

    /**
     * Gets id field declaration.
     *
     * @return the id field
     */
    EntityId getIdField();

    /**
     * Gets index fields declaration.
     *
     * @return the index fields
     */
    List<EntityIndex> getIndexFields();

    /**
     * Gets entity name.
     *
     * @return the entity name
     */
    default String getEntityName() {
        return getEntityType().getName();
    }
}
