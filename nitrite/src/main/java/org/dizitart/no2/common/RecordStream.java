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

import java.util.*;

/**
 * An interface representing a stream of records of type T.
 * Provides methods to create, manipulate and iterate over a 
 * stream of records.
 *
 * @param <T> the type parameter for the records in the stream
 * @author Anindya Chatterjee.
 * @since 1.0
 */
public interface RecordStream<T> extends Iterable<T> {
    /**
     * Creates a {@link RecordStream} from an {@link Iterable}.
     *
     * @param <T>      the type parameter
     * @param iterable the iterable
     * @return the record stream
     */
    static <T> RecordStream<T> fromIterable(Iterable<T> iterable) {
        return iterable::iterator;
    }

    /**
     * Creates a {@link RecordStream} by combining two {@link Iterable}s.
     *
     * @param <T>    the type parameter
     * @param first  the first
     * @param second the second
     * @return the record stream
     */
    static <T> RecordStream<T> fromCombined(Iterable<T> first, Iterable<T> second) {
        return RecordStream.fromIterable(() -> new Iterator<T>() {
            private final Iterator<T> firstIterator = first != null ? first.iterator() : Collections.emptyIterator();
            private final Iterator<T> secondIterator = second != null ? second.iterator() : Collections.emptyIterator();

            @Override
            public boolean hasNext() {
                boolean result = firstIterator.hasNext();
                if (!result) {
                    return secondIterator.hasNext();
                }
                return true;
            }

            @Override
            public T next() {
                T next = firstIterator.hasNext() ? firstIterator.next() : null;
                if (next == null) {
                    return secondIterator.next();
                }
                return next;
            }
        });
    }

    /**
     * Creates a {@link RecordStream} by eliminating <code>elements</code> from an {@link Iterable}.
     *
     * @param <T>      the type parameter
     * @param iterable the iterable
     * @param elements the elements
     * @return the record stream
     */
    static <T> RecordStream<T> except(Iterable<T> iterable, Collection<T> elements) {
        return RecordStream.fromIterable(() -> new Iterator<T>() {
            private final Iterator<T> iterator = iterable != null ? iterable.iterator() : Collections.emptyIterator();
            private T nextItem;
            private boolean nextItemSet = false;

            @Override
            public boolean hasNext() {
                return nextItemSet || setNextId();
            }

            @Override
            public T next() {
                if (!nextItemSet && !setNextId()) {
                    throw new NoSuchElementException();
                }
                nextItemSet = false;
                return nextItem;
            }

            private boolean setNextId() {
                while (iterator.hasNext()) {
                    final T item = iterator.next();
                    if (!elements.contains(item)) {
                        nextItem = item;
                        nextItemSet = true;
                        return true;
                    }
                }
                return false;
            }
        });
    }

    /**
     * Creates an empty {@link RecordStream}.
     *
     * @param <V> the type parameter
     * @return the record stream
     */
    static <V> RecordStream<V> empty() {
        return RecordStream.fromIterable(Collections.emptySet());
    }

    /**
     * Creates a {@link RecordStream} with a single element.
     *
     * @param <V> the type parameter
     * @param v   the v
     * @return the record stream
     */
    static <V> RecordStream<V> single(V v) {
        return RecordStream.fromIterable(Collections.singleton(v));
    }

    /**
     * Gets the size of the {@link RecordStream}.
     *
     * @return the long
     */
    default long size() {
        return Iterables.size(this);
    }

    /**
     * Creates a {@link List} from a {@link RecordStream} by iterating it.
     *
     * @return the list
     */
    default List<T> toList() {
        return Collections.unmodifiableList(Iterables.toList(this));
    }

    /**
     * Creates a {@link Set} from a {@link RecordStream} by iterating it.
     *
     * @return the set
     */
    default Set<T> toSet() {
        return Collections.unmodifiableSet(Iterables.toSet(this));
    }

    /**
     * Checks if this {@link RecordStream} has any elements or not.
     *
     * @return the boolean
     */
    default boolean isEmpty() {
        return !iterator().hasNext();
    }

    /**
     * Gets the first element of the result or <code>null</code> if it is empty.
     *
     * @return the first element or <code>null</code>
     */
    default T firstOrNull() {
        return Iterables.firstOrNull(this);
    }
}
