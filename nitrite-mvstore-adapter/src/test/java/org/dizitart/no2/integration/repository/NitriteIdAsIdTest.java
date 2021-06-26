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

import org.dizitart.no2.integration.Retry;
import org.dizitart.no2.integration.repository.data.WithNitriteId;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.common.util.Iterables;
import org.dizitart.no2.exceptions.InvalidIdException;
import org.dizitart.no2.integration.TestUtil;
import org.dizitart.no2.repository.Cursor;
import org.dizitart.no2.repository.ObjectRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import static org.dizitart.no2.integration.TestUtil.deleteDb;
import static org.dizitart.no2.integration.TestUtil.getRandomTempDbFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Anindya Chatterjee
 */
public class NitriteIdAsIdTest {
    private final String fileName = getRandomTempDbFile();
    private Nitrite db;
    private ObjectRepository<WithNitriteId> repo;

    @Rule
    public Retry retry = new Retry(3);

    @Before
    public void before() {
        db = TestUtil.createDb(fileName);
        repo = db.getRepository(WithNitriteId.class);
    }

    @After
    public void after() throws IOException {
        db.close();
        deleteDb(fileName);
    }

    @Test
    public void testNitriteIdField() {
        WithNitriteId item1 = new WithNitriteId();
        item1.name = "first";

        WithNitriteId item2 = new WithNitriteId();
        item2.name = "second";

        repo.insert(item1, item2);

        Cursor<WithNitriteId> cursor = repo.find();
        for (WithNitriteId withNitriteId : cursor) {
            System.out.println(withNitriteId.name);
            assertNotNull(withNitriteId.idField);
        }

        WithNitriteId withNitriteId = cursor.firstOrNull();
        withNitriteId.name = "third";

        NitriteId id = withNitriteId.idField;
        repo.update(withNitriteId);

        WithNitriteId byId = repo.getById(id);
        assertEquals(withNitriteId, byId);
        assertEquals(repo.size(), 2);
    }

    @Test(expected = InvalidIdException.class)
    public void setIdDuringInsert() {
        WithNitriteId item1 = new WithNitriteId();
        item1.name = "first";
        item1.idField = NitriteId.newId();

        repo.insert(item1);
    }

    @Test
    public void changeIdDuringUpdate() {
        WithNitriteId item2 = new WithNitriteId();
        item2.name = "second";
        WriteResult result = repo.insert(item2);
        NitriteId nitriteId = Iterables.firstOrNull(result);
        WithNitriteId byId = repo.getById(nitriteId);
        byId.idField = NitriteId.newId();

        result = repo.update(byId);
        assertEquals(result.getAffectedCount(), 0);
        assertEquals(repo.size(), 1);
    }

}
