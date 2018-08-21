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

import org.dizitart.no2.collection.IndexOptions;
import org.dizitart.no2.collection.IndexType;
import org.dizitart.no2.collection.objects.ObjectRepository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a field to be indexed.
 *
 * [[app-listing]]
 * [source,java]
 * .Example of Index annotation
 * --
 *  @Index(value = "companyName")
 *  public class Company implements Serializable {
 *
 *      @Id
 *      private long companyId;
 *
 *      private String companyName;
 *
 *  }
 * --
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 * @see ObjectRepository#createIndex(String, IndexOptions)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Index {
    /**
     * The field name to be indexed.
     *
     * @return the field name
     */
    String value();

    /**
     * Type of the index.
     *
     * @return the index type
     */
    IndexType type() default IndexType.Unique;
}
