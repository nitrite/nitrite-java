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

package org.dizitart.no2.repository;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.collection.*;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexEntry;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.store.NitriteStore;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertNotNull;

/**
 * @author Anindya Chatterjee
 */
public class RepositoryFactoryTest {
    @Test
    public void testRepositoryFactory() {
        RepositoryFactory factory = new RepositoryFactory(new CollectionFactory());
        assertNotNull(factory);
    }

    @Test(expected = ValidationException.class)
    public void testNullType() {
        RepositoryFactory factory = new RepositoryFactory(new CollectionFactory());
        Nitrite db = NitriteBuilder.get().openOrCreate();
        factory.getRepository(db.getConfig(), null, "dummy");
    }

    @Test
    public void testNullCollection() {
        RepositoryFactory factory = new RepositoryFactory(new CollectionFactory());
        Nitrite db = NitriteBuilder.get().openOrCreate();
        factory.getRepository(db.getConfig(), DummyCollection.class, null);
    }

    @Test(expected = ValidationException.class)
    public void testNullContext() {
        RepositoryFactory factory = new RepositoryFactory(new CollectionFactory());
        factory.getRepository(null, DummyCollection.class, "dummy");
    }

    private static class DummyCollection implements NitriteCollection {

        @Override
        public WriteResult update(Filter filter, Document update, UpdateOptions updateOptions) {
            return null;
        }

        @Override
        public WriteResult remove(Filter filter, boolean justOne) {
            return null;
        }

        @Override
        public DocumentCursor find() {
            return null;
        }

        @Override
        public DocumentCursor find(Filter filter) {
            return null;
        }

        @Override
        public Document getById(NitriteId nitriteId) {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public void createIndex(String field, IndexOptions indexOptions) {

        }

        @Override
        public void rebuildIndex(String field, boolean isAsync) {

        }

        @Override
        public Collection<IndexEntry> listIndices() {
            return null;
        }

        @Override
        public boolean hasIndex(String field) {
            return false;
        }

        @Override
        public boolean isIndexing(String field) {
            return false;
        }

        @Override
        public void dropIndex(String field) {

        }

        @Override
        public void dropAllIndices() {

        }

        @Override
        public WriteResult insert(Document[] elements) {
            return null;
        }

        @Override
        public WriteResult update(Document element, boolean insertIfAbsent) {
            return null;
        }

        @Override
        public WriteResult remove(Document element) {
            return null;
        }

        @Override
        public void drop() {

        }

        @Override
        public boolean isDropped() {
            return false;
        }

        @Override
        public boolean isOpen() {
            return false;
        }

        @Override
        public void close() {

        }

        @Override
        public long size() {
            return 0;
        }

        @Override
        public NitriteStore getStore() {
            return null;
        }

        @Override
        public void subscribe(CollectionEventListener listener) {

        }

        @Override
        public void unsubscribe(CollectionEventListener listener) {

        }

        @Override
        public Attributes getAttributes() {
            return null;
        }

        @Override
        public void setAttributes(Attributes attributes) {

        }
    }
}
