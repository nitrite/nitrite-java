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

package org.dizitart.no2.repository;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.CollectionFactory;
import org.dizitart.no2.common.concurrent.LockService;
import org.dizitart.no2.exceptions.ValidationException;
import org.junit.Test;

import static org.junit.Assert.assertThrows;

public class RepositoryFactoryTest {

    @Test
    public void testGetRepository() {
        RepositoryFactory repositoryFactory = new RepositoryFactory(new CollectionFactory(new LockService()));
        assertThrows(ValidationException.class, () -> repositoryFactory.getRepository(null, Object.class));
    }

    @Test
    public void testGetRepository2() {
        RepositoryFactory repositoryFactory = new RepositoryFactory(new CollectionFactory(new LockService()));
        assertThrows(ValidationException.class, () -> repositoryFactory.getRepository(new NitriteConfig(), null));
    }

    @Test
    public void testGetRepository3() {
        RepositoryFactory repositoryFactory = new RepositoryFactory(new CollectionFactory(new LockService()));
        assertThrows(ValidationException.class, () -> repositoryFactory.getRepository(null, Object.class, "Key"));
    }

    @Test
    public void testGetRepository4() {
        RepositoryFactory repositoryFactory = new RepositoryFactory(new CollectionFactory(new LockService()));
        assertThrows(ValidationException.class,
            () -> repositoryFactory.getRepository(new NitriteConfig(), null, "Key"));
    }
}

