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

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.collection.events.EventType;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.index.IndexEntry;
import org.dizitart.no2.index.IndexOptions;

import java.util.Collection;

/**
 * @author Anindya Chatterjee
 */
public interface RxPersistentCollection<T> {
    Completable createIndex(String field, IndexOptions indexOptions);

    Completable rebuildIndex(String field, boolean async);

    Single<Collection<IndexEntry>> listIndices();

    Single<Boolean> hasIndex(String field);

    Single<Boolean> isIndexing(String field);

    Completable dropIndex(String field);

    Completable dropAllIndices();

    FlowableWriteResult insert(T[] items);

    FlowableWriteResult update(T element);

    FlowableWriteResult update(T element, boolean insertIfAbsent);

    FlowableWriteResult remove(T element);

    Completable drop();

    Single<Boolean> isDropped();

    Single<Boolean> isOpen();

    Completable close();

    Single<Long> size();

    Observable<CollectionEventInfo<?>> observe();

    Observable<CollectionEventInfo<?>> observe(EventType eventType);

    Single<Attributes> getAttributes();

    Completable setAttributes(Attributes attributes);
}
