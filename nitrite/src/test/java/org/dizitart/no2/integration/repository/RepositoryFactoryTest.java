/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.integration.repository;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.integration.Retry;
import org.dizitart.no2.integration.TestUtil;
import org.dizitart.no2.collection.*;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.common.meta.Attributes;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.common.concurrent.LockService;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.common.processors.Processor;
import org.dizitart.no2.repository.RepositoryFactory;
import org.dizitart.no2.store.NitriteStore;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertNotNull;

/**
 * @author Anindya Chatterjee
 */
public class RepositoryFactoryTest {
    private Nitrite db;

    @Rule
    public Retry retry = new Retry(3);

    @Test
    public void testRepositoryFactory() {
        RepositoryFactory factory = new RepositoryFactory(new CollectionFactory(new LockService()));
        assertNotNull(factory);
    }

    @Test(expected = ValidationException.class)
    public void testNullType() {
        RepositoryFactory factory = new RepositoryFactory(new CollectionFactory(new LockService()));
        db = TestUtil.createDb();
        factory.getRepository(db.getConfig(), (Class<?>) null, "dummy");
    }

    @Test
    public void testNullCollection() {
        RepositoryFactory factory = new RepositoryFactory(new CollectionFactory(new LockService()));
        db = TestUtil.createDb();
        factory.getRepository(db.getConfig(), DummyCollection.class, null);
    }

    @Test(expected = ValidationException.class)
    public void testNullContext() {
        RepositoryFactory factory = new RepositoryFactory(new CollectionFactory(new LockService()));
        factory.getRepository(null, DummyCollection.class, "dummy");
    }

    @After
    public void cleanUp() throws Exception {
        if (db != null && !db.isClosed()) {
            db.close();
        }
    }

    private static class DummyCollection implements NitriteCollection {

        @Override
        public WriteResult insert(Document document, Document... documents) {
            return null;
        }

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
        public DocumentCursor find(Filter filter, FindOptions findOptions) {
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
        public void addProcessor(Processor processor) {

        }

        @Override
        public void createIndex(IndexOptions indexOptions, String... fields) {

        }

        @Override
        public void rebuildIndex(String... fields) {

        }

        @Override
        public Collection<IndexDescriptor> listIndices() {
            return null;
        }

        @Override
        public boolean hasIndex(String... fields) {
            return false;
        }

        @Override
        public boolean isIndexing(String... fields) {
            return false;
        }

        @Override
        public void dropIndex(String... fields) {

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
        public void clear() {

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
        public NitriteStore<?> getStore() {
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
