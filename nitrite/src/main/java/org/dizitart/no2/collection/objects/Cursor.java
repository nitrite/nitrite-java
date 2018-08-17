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

import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.collection.Lookup;
import org.dizitart.no2.collection.RecordIterable;

/**
 * A collection of {@link NitriteId}s of the database records,
 * as a result of a find operation.
 *
 * @author Anindya Chatterjee
 * @see ObjectRepository#find(ObjectFilter)
 * @see ObjectRepository#find(ObjectFilter, FindOptions)
 * @see ObjectRepository#find()
 * @see ObjectRepository#find(FindOptions)
 * @since 1.0
 */
public interface Cursor<T> extends RecordIterable<T> {

    /**
     * Projects the result of one type into an {@link Iterable} of other type.
     *
     * @param <P>               the type of the target objects.
     * @param projectionType    the projection type.
     * @return `Iterable` of projected objects.
     */
    <P> RecordIterable<P> project(Class<P> projectionType);

    /**
     * Performs a left outer join with a foreign cursor with the specified lookup parameters.
     *
     * It performs an equality match on the localField to the foreignField from the objects of the foreign cursor.
     * If an input object does not contain the localField, the join treats the field as having a value of `null`
     * for matching purposes.
     *
     * @param <Foreign> the type of the foreign object.
     * @param <Joined> the type of the joined object.
     * @param foreignCursor the foreign cursor for the join.
     * @param lookup the lookup parameter for the join operation.
     * @param type the type of the joined record.
     *
     * @return a lazy iterable of joined objects.
     * @since 2.1.0
     */
    <Foreign, Joined> RecordIterable<Joined> join(Cursor<Foreign> foreignCursor, Lookup lookup, Class<Joined> type);
}
