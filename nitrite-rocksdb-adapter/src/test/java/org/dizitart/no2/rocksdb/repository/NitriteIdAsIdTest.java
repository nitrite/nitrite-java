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

package org.dizitart.no2.rocksdb.repository;

import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.common.util.Iterables;
import org.dizitart.no2.exceptions.InvalidIdException;
import org.dizitart.no2.rocksdb.AbstractTest;
import org.dizitart.no2.rocksdb.repository.data.WithNitriteId;
import org.dizitart.no2.repository.Cursor;
import org.dizitart.no2.repository.ObjectRepository;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Anindya Chatterjee
 */
public class NitriteIdAsIdTest extends AbstractTest {
    private ObjectRepository<WithNitriteId> repo;

    @Before
    public void before() {
        repo = db.getRepository(WithNitriteId.class);
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
