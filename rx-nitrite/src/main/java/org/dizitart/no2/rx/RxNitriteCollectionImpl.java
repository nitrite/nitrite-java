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
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.UpdateOptions;
import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.collection.events.EventType;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexEntry;
import org.dizitart.no2.index.IndexOptions;

import java.util.Collection;

/**
 * @author Anindya Chatterjee
 */
class RxNitriteCollectionImpl implements RxNitriteCollection {
    private final NitriteCollection nitriteCollection;
    private final PublishSubject<CollectionEventInfo<?>> updates;

    RxNitriteCollectionImpl(NitriteCollection collection) {
        this.nitriteCollection = collection;
        this.updates = PublishSubject.create();
        initializeUpdateObserver();
    }

    @Override
    public FlowableWriteResult insert(Document document, Document... documents) {
        return new FlowableWriteResult(() -> nitriteCollection.insert(document, documents));
    }

    @Override
    public FlowableWriteResult update(Filter filter, Document update) {
        return new FlowableWriteResult(() -> nitriteCollection.update(filter, update));
    }

    @Override
    public FlowableWriteResult update(Filter filter, Document update, UpdateOptions updateOptions) {
        return new FlowableWriteResult(() -> nitriteCollection.update(filter, update, updateOptions));
    }

    @Override
    public FlowableWriteResult remove(Filter filter) {
        return new FlowableWriteResult(() -> nitriteCollection.remove(filter));
    }

    @Override
    public FlowableWriteResult remove(Filter filter, boolean justOne) {
        return new FlowableWriteResult(() -> nitriteCollection.remove(filter, justOne));
    }

    @Override
    public FlowableDocumentCursor find() {
        return new FlowableDocumentCursor(nitriteCollection::find);
    }

    @Override
    public FlowableDocumentCursor find(Filter filter) {
        return new FlowableDocumentCursor(() -> nitriteCollection.find(filter));
    }

    @Override
    public Completable createIndex(String field, IndexOptions indexOptions) {
        return Completable.fromAction(() -> nitriteCollection.createIndex(field, indexOptions));
    }

    @Override
    public Completable rebuildIndex(String field, boolean async) {
        return Completable.fromAction(() -> nitriteCollection.rebuildIndex(field, async));
    }

    @Override
    public Single<Collection<IndexEntry>> listIndices() {
        return Single.fromCallable(nitriteCollection::listIndices);
    }

    @Override
    public Single<Boolean> hasIndex(String field) {
        return Single.fromCallable(() -> nitriteCollection.hasIndex(field));
    }

    @Override
    public Single<Boolean> isIndexing(String field) {
        return Single.fromCallable(() -> nitriteCollection.isIndexing(field));
    }

    @Override
    public Completable dropIndex(String field) {
        return Completable.fromAction(() -> nitriteCollection.dropIndex(field));
    }

    @Override
    public Completable dropAllIndices() {
        return Completable.fromAction(nitriteCollection::dropAllIndices);
    }

    @Override
    public FlowableWriteResult insert(Document[] items) {
        return new FlowableWriteResult(() -> nitriteCollection.insert(items));
    }

    @Override
    public FlowableWriteResult update(Document element) {
        return new FlowableWriteResult(() -> nitriteCollection.update(element));
    }

    @Override
    public FlowableWriteResult update(Document element, boolean upsert) {
        return new FlowableWriteResult(() -> nitriteCollection.update(element, upsert));
    }

    @Override
    public FlowableWriteResult remove(Document element) {
        return new FlowableWriteResult(() -> nitriteCollection.remove(element));
    }

    @Override
    public Single<Document> getById(NitriteId nitriteId) {
        return Single.fromCallable(() -> nitriteCollection.getById(nitriteId));
    }

    @Override
    public Completable drop() {
        return Completable.fromAction(nitriteCollection::drop);
    }

    @Override
    public Single<Boolean> isDropped() {
        return Single.fromCallable(nitriteCollection::isDropped);
    }

    @Override
    public Single<Boolean> isOpen() {
        return Single.fromCallable(nitriteCollection::isOpen);
    }

    @Override
    public Completable close() {
        return Completable.fromAction(nitriteCollection::close);
    }

    @Override
    public String getName() {
        return nitriteCollection.getName();
    }

    @Override
    public Single<Long> size() {
        return Single.fromCallable(nitriteCollection::size);
    }

    @Override
    public Observable<CollectionEventInfo<?>> observe() {
        return updates;
    }

    @Override
    public Observable<CollectionEventInfo<?>> observe(EventType eventType) {
        return updates.filter(changedItem -> changedItem.getEventType() == eventType);
    }

    @Override
    public Single<Attributes> getAttributes() {
        return Single.fromCallable(nitriteCollection::getAttributes);
    }

    @Override
    public Completable setAttributes(Attributes attributes) {
        return Completable.fromAction(() -> nitriteCollection.setAttributes(attributes));
    }

    private void initializeUpdateObserver() {
        nitriteCollection.subscribe(changedItem -> {
            if (changedItem != null) {
                updates.onNext(changedItem);
            }
        });
    }
}
