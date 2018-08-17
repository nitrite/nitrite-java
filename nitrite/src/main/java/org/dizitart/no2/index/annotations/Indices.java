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

package org.dizitart.no2.index.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies multiple indexed fields for a class.
 * 
 * [[app-listing]]
 * [source,java]
 * .Example of Indices annotation
 * --
 *
 * @Indices({
 *      @Index(value = "joinDate", type = IndexType.NonUnique),
 *      @Index(value = "address", type = IndexType.Fulltext)
 * })
 * public class Employee implements Serializable {
 *
 *      @Id private long empId;
 *      private Date joinDate;
 *      private String address;
 * }
 *
 * --
 *
 * @author Anindya Chatterjee.
 * @see Index
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Indices {
    /**
     * Returns an array of {@link Index}.
     *
     * @return the array of {@link Index}.
     */
    Index[] value();
}
