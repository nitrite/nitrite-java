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

package org.dizitart.no2.collection;

import io.reactivex.Observable;
import org.dizitart.no2.util.Iterables;

import java.util.List;

/**
 * An iterable of database records which supports pagination
 * and projection.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 * @see Cursor
 * @see org.dizitart.no2.collection.objects.Cursor
 */
public interface RecordIterable<T> extends Iterable<T> {

    /**
     * Specifies if there are more elements in the database that
     * has not been retrieved yet.
     *
     * @return `true` if the cursor has more elements; otherwise `false`.
     */
    boolean hasMore();

    /**
     * Gets the size of the current record set.
     *
     * @return the size of the current record set.
     */
    int size();

    /**
     * Gets the total count of the records in the database matching a filter criteria.
     *
     * [icon="{@docRoot}/note.png"]
     * [NOTE]
     * ====
     * If pagination is used during find operation, this method
     * and {@link #size()} may not be equal. In that case {@link #size()}
     * denotes the size of the current page and this method denotes
     * the size of all matching results in the database that may or may not
     * be retrieved yet.
     *
     * ====
     *
     * @return total count of matching documents.
     * @see NitriteCollection#find(Filter, FindOptions)
     * @see NitriteCollection#find(FindOptions)
     * @see FindOptions#limit(int, int)
     */
    int totalCount();

    /**
     * Gets the first element of the result or
     * `null` if it is empty.
     *
     * @return the first element or `null`
     */
    default T firstOrDefault() {
        return Iterables.firstOrDefault(this);
    }

    /**
     * Returns a list of all elements.
     *
     * @return list of all elements.
     * */
    default List<T> toList() {
        return Iterables.toList(this);
    }

    /**
     * Returns an {@link Observable} on the records.
     *
     * @return an {@link Observable} on the records
     * */
    default Observable<T> toObservable() {
        return Observable.fromIterable(this);
    }
}
