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

package org.dizitart.no2.common;

import org.dizitart.no2.common.util.Iterables;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Anindya Chatterjee.
 */
public interface RecordStream<T> extends Iterable<T> {
    static <T> RecordStream<T> fromIterable(Iterable<T> iterable) {
        return iterable::iterator;
    }

    static <T> RecordStream<T> fromIterator(Iterator<T> iterator) {
        return () -> iterator;
    }

    default long size() {
        return Iterables.size(this);
    }

    default List<T> toList() {
        return Iterables.toList(this);
    }

    default Set<T> toSet() {
        return Iterables.toSet(this);
    }

    default boolean isEmpty() {
        return !iterator().hasNext();
    }

    /**
     * Gets the first element of the result or
     * `null` if it is empty.
     *
     * @return the first element or `null`
     */
    default T firstOrNull() {
        return Iterables.firstOrNull(this);
    }
}