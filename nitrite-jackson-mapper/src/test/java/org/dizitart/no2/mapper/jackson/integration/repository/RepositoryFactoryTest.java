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

package org.dizitart.no2.mapper.jackson.integration.repository;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.*;
import org.dizitart.no2.common.concurrent.LockService;
import org.dizitart.no2.mapper.jackson.JacksonMapper;
import org.dizitart.no2.common.module.NitriteModule;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.repository.RepositoryFactory;
import org.dizitart.no2.mapper.jackson.integration.repository.data.Book;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import static org.dizitart.no2.mapper.jackson.integration.repository.BaseObjectRepositoryTest.getRandomTempDbFile;
import static org.junit.Assert.assertNotNull;

/**
 * @author Anindya Chatterjee
 */
public class RepositoryFactoryTest {
    private final String fileName = getRandomTempDbFile();
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
        JacksonMapper mapper = new JacksonMapper();
        db = TestUtil.createDb(fileName, NitriteModule.module(mapper));
        factory.getRepository(db.getConfig(), (Class<?>) null, "dummy");
    }

    @Test
    public void testNullCollection() {
        RepositoryFactory factory = new RepositoryFactory(new CollectionFactory(new LockService()));
        JacksonMapper mapper = new JacksonMapper();
        db = TestUtil.createDb(fileName, NitriteModule.module(mapper));
        factory.getRepository(db.getConfig(), Book.class, null);
    }

    @Test(expected = ValidationException.class)
    public void testNullContext() {
        RepositoryFactory factory = new RepositoryFactory(new CollectionFactory(new LockService()));
        factory.getRepository(null, Book.class, "dummy");
    }

    @After
    public void cleanUp() {
        if (db != null && !db.isClosed()) {
            db.close();
        }
    }
}
