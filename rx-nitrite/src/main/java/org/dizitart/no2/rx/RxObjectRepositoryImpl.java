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
import io.reactivex.subjects.PublishSubject;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.collection.events.EventType;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexEntry;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.repository.ObjectRepository;

import java.util.Collection;


/**
 * @author Anindya Chatterjee
 */
@Slf4j
class RxObjectRepositoryImpl<T> implements RxObjectRepository<T> {
    private final ObjectRepository<T> repository;
    private final NitriteConfig nitriteConfig;
    private PublishSubject<CollectionEventInfo<?>> updates;

    RxObjectRepositoryImpl(ObjectRepository<T> repository, NitriteConfig nitriteConfig) {
        this.repository = repository;
        this.nitriteConfig = nitriteConfig;
        this.updates = PublishSubject.create();
        initializeUpdateObserver();
    }

    @Override
    @SafeVarargs
    public final FlowableWriteResult insert(T object, T... others) {
        return new FlowableWriteResult(() -> repository.insert(object, others));
    }

    @Override
    public FlowableWriteResult update(Filter filter, T update) {
        return new FlowableWriteResult(() -> repository.update(filter, update));
    }

    @Override
    public FlowableWriteResult update(Filter filter, T update, boolean upsert) {
        return new FlowableWriteResult(() -> repository.update(filter, update, upsert));
    }

    @Override
    public FlowableWriteResult update(Filter filter, Document update) {
        return new FlowableWriteResult(() -> repository.update(filter, update));
    }

    @Override
    public FlowableWriteResult update(Filter filter, Document update, boolean justOnce) {
        return new FlowableWriteResult(() -> repository.update(filter, update, justOnce));
    }

    @Override
    public FlowableWriteResult remove(Filter filter) {
        return new FlowableWriteResult(() -> repository.remove(filter));
    }

    @Override
    public FlowableWriteResult remove(Filter filter, boolean justOne) {
        return new FlowableWriteResult(() -> repository.remove(filter, justOne));
    }

    @Override
    public FlowableCursor<T> find() {
        return new FlowableCursor<>(repository::find);
    }

    @Override
    public FlowableCursor<T> find(Filter filter) {
        return new FlowableCursor<>(() -> repository.find(filter));
    }

    @Override
    public <I> Single<T> getById(I id) {
        return Single.fromCallable(() -> repository.getById(id));
    }

    @Override
    public Class<T> getType() {
        return repository.getType();
    }

    @Override
    public RxNitriteCollection getDocumentCollection() {
        return new RxNitriteCollectionImpl(repository.getDocumentCollection());
    }

    @Override
    public Completable createIndex(String field, IndexOptions indexOptions) {
        return Completable.fromAction(() -> repository.createIndex(field, indexOptions));
    }

    @Override
    public Completable rebuildIndex(String field, boolean async) {
        return Completable.fromAction(() -> repository.rebuildIndex(field, async));
    }

    @Override
    public Single<Collection<IndexEntry>> listIndices() {
        return Single.fromCallable(repository::listIndices);
    }

    @Override
    public Single<Boolean> hasIndex(String field) {
        return Single.fromCallable(() -> repository.hasIndex(field));
    }

    @Override
    public Single<Boolean> isIndexing(String field) {
        return Single.fromCallable(() -> repository.isIndexing(field));
    }

    @Override
    public Completable dropIndex(String field) {
        return Completable.fromAction(() -> repository.dropIndex(field));
    }

    @Override
    public Completable dropAllIndices() {
        return Completable.fromAction(repository::dropAllIndices);
    }

    @Override
    public FlowableWriteResult insert(T[] items) {
        return new FlowableWriteResult(() -> repository.insert(items));
    }

    @Override
    public FlowableWriteResult update(T element) {
        return new FlowableWriteResult(() -> repository.update(element));
    }

    @Override
    public FlowableWriteResult update(T element, boolean insertIfAbsent) {
        return new FlowableWriteResult(() -> repository.update(element, insertIfAbsent));
    }

    @Override
    public FlowableWriteResult remove(T element) {
        return new FlowableWriteResult(() -> repository.remove(element));
    }

    @Override
    public Completable drop() {
        return Completable.fromAction(repository::drop);
    }

    @Override
    public Single<Boolean> isDropped() {
        return Single.fromCallable(repository::isDropped);
    }

    @Override
    public Single<Boolean> isOpen() {
        return Single.fromCallable(repository::isOpen);
    }

    @Override
    public Completable close() {
        return Completable.fromAction(repository::close);
    }

    @Override
    public Single<Long> size() {
        return Single.fromCallable(repository::size);
    }

    @Override
    public Observable<CollectionEventInfo<?>> observe() {
        return updates;
    }

    @Override
    public Observable<CollectionEventInfo<?>> observe(EventType eventType) {
        return updates.filter(item -> item.getEventType() == eventType);
    }

    @Override
    public Single<Attributes> getAttributes() {
        return Single.fromCallable(repository::getAttributes);
    }

    @Override
    public Completable setAttributes(Attributes attributes) {
        return Completable.fromAction(() -> repository.setAttributes(attributes));
    }

    private void initializeUpdateObserver() {
        repository.subscribe(eventInfo -> {
            try {
                if (eventInfo != null) {
                    Object item = eventInfo.getItem();
                    NitriteMapper nitriteMapper = nitriteConfig.nitriteMapper();
                    Object object = nitriteMapper.convert(item, getType());
                    CollectionEventInfo<?> collectionEventInfo = new CollectionEventInfo<>(object,
                        eventInfo.getEventType(),
                        eventInfo.getTimestamp(), eventInfo.getOriginator());
                    updates.onNext(collectionEventInfo);
                }
            } catch (Exception e) {
                log.error("Error while listening to repository events", e);
            }
        });
    }
}
