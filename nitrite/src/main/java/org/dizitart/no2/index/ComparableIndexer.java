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

package org.dizitart.no2.index;

import org.dizitart.no2.NitriteId;

import java.util.List;
import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
public interface ComparableIndexer extends Indexer<Comparable> {

    /**
     * Finds with equal filer using index.
     *
     * @param field the field
     * @param value the value
     * @return the result set.
     */
    Set<NitriteId> findEqual(String field, Object value);

    /**
     * Finds with greater than filer using index.
     *
     * @param field the field
     * @param comparable the value
     * @return the result set
     */
    Set<NitriteId> findGreaterThan(String field, Comparable comparable);

    /**
     * Finds with greater and equal filer using index.
     *
     * @param field the field
     * @param comparable the value
     * @return the result set
     */
    Set<NitriteId> findGreaterEqual(String field, Comparable comparable);

    /**
     * Finds with lesser filer using index.
     *
     * @param field the field
     * @param comparable the value
     * @return the result set
     */
    Set<NitriteId> findLesserThan(String field, Comparable comparable);

    /**
     * Finds with lesser equal filer using index.
     *
     * @param field the field
     * @param comparable the value
     * @return the result set
     */
    Set<NitriteId> findLesserEqual(String field, Comparable comparable);

    /**
     * Finds with in filer using index.
     *
     * @param field  the value
     * @param values the values
     * @return the result set
     */
    Set<NitriteId> findIn(String field, List<Object> values);
}
