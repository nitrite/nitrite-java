/*
 * Copyright (c) 2017-2022 Nitrite author or authors.
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

import lombok.Data;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.common.mapper.SimpleDocumentMapper;
import org.dizitart.no2.index.IndexFields;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.integration.repository.data.ClassA;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class EntityDecoratorReaderTest {

    private EntityDecoratorReader reader;
    private NitriteCollection collection;

    @Before
    public void setUp() {
        NitriteMapper nitriteMapper = new SimpleDocumentMapper();
        collection = Nitrite.builder().openOrCreate().getCollection("test");
        reader = new EntityDecoratorReader(new EntityADecorator(), collection, nitriteMapper);
    }

    @Test
    public void testReadEntity() {
        assertNull(reader.getObjectIdField());
        assertTrue(reader.getIndices().isEmpty());

        reader.readEntity();
        assertNotNull(reader.getObjectIdField());
        assertFalse(reader.getIndices().isEmpty());

        ObjectIdField idField = reader.getObjectIdField();
        assertEquals(idField.getIdFieldName(), "id");
        assertArrayEquals(idField.getFieldNames(), new String[] {"uid", "string"});
        assertArrayEquals(idField.getEmbeddedFieldNames(), new String[] {"id.uid", "id.string"});

        Set<IndexFields> indexFields = reader.getIndices();
        assertEquals(indexFields.size(), 2);
    }

    @Test
    public void testCreateIndices() {
        assertFalse(collection.hasIndex("name", "age"));
        assertFalse(collection.hasIndex("address"));
        assertFalse(collection.hasIndex("id.uid", "id.string"));

        reader.readEntity();
        reader.createIndices();

        assertTrue(collection.hasIndex("name", "age"));
        assertTrue(collection.hasIndex("address"));
        assertFalse(collection.hasIndex("id.uid", "id.string"));
    }

    @Test
    public void testCreateIdIndex() {
        assertNull(reader.getObjectIdField());
        assertFalse(collection.hasIndex("name", "age"));
        assertFalse(collection.hasIndex("address"));
        assertFalse(collection.hasIndex("id.uid", "id.string"));

        reader.readEntity();
        reader.createIdIndex();

        assertNotNull(reader.getObjectIdField());
        assertFalse(collection.hasIndex("name", "age"));
        assertFalse(collection.hasIndex("address"));
        assertTrue(collection.hasIndex("id.uid", "id.string"));
    }

    @Data
    public static class EntityA {
        private ClassA id;
        private String name;
        private String age;
        private String address;
    }

    public static class EntityADecorator implements EntityDecorator<EntityA> {

        @Override
        public Class<EntityA> getEntityType() {
            return EntityA.class;
        }

        @Override
        public EntityId getIdField() {
            return new EntityId("id", "uid", "string");
        }

        @Override
        public List<IndexFields> getIndexFields() {
            List<IndexFields> list = new ArrayList<>();
            IndexFields fieldsA = IndexFields.create(IndexType.UNIQUE, "name", "age");
            IndexFields fieldsB = IndexFields.create(IndexType.NON_UNIQUE, "address");
            list.add(fieldsA);
            list.add(fieldsB);
            return list;
        }

        @Override
        public String getEntityName() {
            return "a";
        }
    }
}
