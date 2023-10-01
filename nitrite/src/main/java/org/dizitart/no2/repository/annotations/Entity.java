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

package org.dizitart.no2.repository.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as an entity. An entity is a persistent 
 * class which can be stored in an {@link org.dizitart.no2.repository.ObjectRepository}.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Entity {
    /**
     * Name of the {@link org.dizitart.no2.repository.ObjectRepository}. By default,
     * the name would be the class name of the entity.
     *
     * @return the name
     */
    String value() default "";

    /**
     * A list of indices for the repository.
     *
     * @return the index definitions
     */
    Index[] indices() default {};
}
