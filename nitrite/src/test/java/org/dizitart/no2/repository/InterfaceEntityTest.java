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

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.mapper.SimpleNitriteMapper;
import org.dizitart.no2.index.IndexType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test for interface entity support using EntityDecorator
 */
public class InterfaceEntityTest {
    
    private Nitrite db;
    private EntityDecoratorScanner scanner;
    private NitriteCollection collection;
    
    // Interface entity definition
    public interface Animal {
        String getId();
        String getName();
    }
    
    // Concrete implementation
    public static class Dog implements Animal {
        private String id;
        private String name;
        
        public Dog() {}
        
        public Dog(String id, String name) {
            this.id = id;
            this.name = name;
        }
        
        @Override
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    // EntityDecorator for the interface
    public static class AnimalDecorator implements EntityDecorator<Animal> {
        
        @Override
        public Class<Animal> getEntityType() {
            return Animal.class;
        }
        
        @Override
        public EntityId getIdField() {
            return new EntityId("id");
        }
        
        @Override
        public List<EntityIndex> getIndexFields() {
            List<EntityIndex> list = new ArrayList<>();
            EntityIndex nameIndex = new EntityIndex(IndexType.NON_UNIQUE, "name");
            list.add(nameIndex);
            return list;
        }
        
        @Override
        public String getEntityName() {
            return "animal";
        }
    }
    
    @Before
    public void setUp() {
        SimpleNitriteMapper nitriteMapper = new SimpleNitriteMapper();
        db = Nitrite.builder()
            .fieldSeparator(".")
            .openOrCreate();
        collection = db.getCollection("test");
        scanner = new EntityDecoratorScanner(new AnimalDecorator(), collection, nitriteMapper);
    }
    
    @After
    public void tearDown() {
        if (db != null && !db.isClosed()) {
            db.close();
        }
    }
    
    @Test
    public void testReadEntityWithInterface() {
        assertNull(scanner.getObjectIdField());
        assertTrue(scanner.getIndices().isEmpty());
        
        // This should not throw ValidationException
        scanner.readEntity();
        
        assertNotNull(scanner.getObjectIdField());
        assertFalse(scanner.getIndices().isEmpty());
        
        ObjectIdField idField = scanner.getObjectIdField();
        assertEquals("id", idField.getIdFieldName());
    }
    
    @Test
    public void testCreateIndicesWithInterface() {
        assertFalse(collection.hasIndex("name"));
        assertFalse(collection.hasIndex("id"));
        
        scanner.readEntity();
        scanner.createIndices();
        
        assertTrue(collection.hasIndex("name"));
    }
    
    @Test
    public void testCreateIdIndexWithInterface() {
        assertFalse(collection.hasIndex("id"));
        
        scanner.readEntity();
        scanner.createIdIndex();
        
        assertTrue(collection.hasIndex("id"));
    }
}
