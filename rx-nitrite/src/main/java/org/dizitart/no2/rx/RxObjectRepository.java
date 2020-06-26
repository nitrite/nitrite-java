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

package org.dizitart.no2.rx;

import io.reactivex.Single;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.util.Iterables;
import org.dizitart.no2.filters.Filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.dizitart.no2.common.util.ValidationUtils.containsNull;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * @author Anindya Chatterjee
 */
public interface RxObjectRepository<T> extends RxPersistentCollection<T> {

    default FlowableWriteResult insert(T object, T... others) {
        notNull(object, "a null object cannot be inserted");
        if (others != null) {
            containsNull(others, "a null object cannot be inserted");
        }

        List<T> itemList = new ArrayList<>();
        itemList.add(object);

        if (others != null && itemList.size() > 0) {
            Collections.addAll(itemList, others);
        }

        return insert(Iterables.toArray(itemList, getType()));
    }

    default FlowableWriteResult update(Filter filter, T update) {
        return update(filter, update, false);
    }

    FlowableWriteResult update(Filter filter, T update, boolean insertIfAbsent);

    default FlowableWriteResult update(Filter filter, Document update) {
        return update(filter, update, false);
    }

    FlowableWriteResult update(Filter filter, Document update, boolean justOnce);

    default FlowableWriteResult remove(Filter filter) {
        return remove(filter, false);
    }

    FlowableWriteResult remove(Filter filter, boolean justOne);

    FlowableCursor<T> find();

    FlowableCursor<T> find(Filter filter);

    <I> Single<T> getById(I id);

    Class<T> getType();

    RxNitriteCollection getDocumentCollection();
}
