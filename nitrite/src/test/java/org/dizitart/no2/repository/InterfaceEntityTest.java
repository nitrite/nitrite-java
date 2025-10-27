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
    
    // Another concrete implementation
    public static class Cat implements Animal {
        private String id;
        private String name;
        
        public Cat() {}
        
        public Cat(String id, String name) {
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
    
    // Third concrete implementation with boolean property
    public static class Bird implements Animal {
        private String id;
        private String name;
        private boolean canFly;
        
        public Bird() {}
        
        public Bird(String id, String name, boolean canFly) {
            this.id = id;
            this.name = name;
            this.canFly = canFly;
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
        
        public boolean isCanFly() {
            return canFly;
        }
        
        public void setCanFly(boolean canFly) {
            this.canFly = canFly;
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
    
    @Test
    public void testMultipleImplementationsFieldAccess() throws IllegalAccessException {
        scanner.readEntity();
        ObjectIdField idField = scanner.getObjectIdField();
        assertNotNull(idField);
        
        // Test with Dog instance
        Dog dog = new Dog("dog-1", "Buddy");
        Object dogId = FieldAccessHelper.get(idField.getField(), dog);
        assertEquals("dog-1", dogId);
        
        FieldAccessHelper.set(idField.getField(), dog, "dog-2");
        assertEquals("dog-2", dog.getId());
        
        // Test with Cat instance
        Cat cat = new Cat("cat-1", "Whiskers");
        Object catId = FieldAccessHelper.get(idField.getField(), cat);
        assertEquals("cat-1", catId);
        
        FieldAccessHelper.set(idField.getField(), cat, "cat-2");
        assertEquals("cat-2", cat.getId());
        
        // Test with Bird instance
        Bird bird = new Bird("bird-1", "Tweety", true);
        Object birdId = FieldAccessHelper.get(idField.getField(), bird);
        assertEquals("bird-1", birdId);
        
        FieldAccessHelper.set(idField.getField(), bird, "bird-2");
        assertEquals("bird-2", bird.getId());
    }
    
    @Test
    public void testEdgeCaseEmptyPropertyName() {
        // This tests that our validation works
        scanner.readEntity();
        Dog dog = new Dog("test", "TestDog");
        
        try {
            // Directly test the helper with an empty property name
            // This should fail gracefully
            java.lang.reflect.Field testField = InterfacePropertyHolder.class.getDeclaredField("property");
            InterfacePropertyHolder.registerProperty(testField, "", null);
            FieldAccessHelper.get(testField, dog);
            fail("Should have thrown IllegalAccessException for empty property name");
        } catch (IllegalAccessException e) {
            assertTrue(e.getMessage().contains("Property name cannot be null or empty"));
        } catch (Exception e) {
            // Expected - field access may fail in different ways
        }
    }
    
    @Test
    public void testEdgeCaseNullPropertyName() {
        scanner.readEntity();
        Dog dog = new Dog("test", "TestDog");
        
        try {
            // Directly test the helper with a null property name
            java.lang.reflect.Field testField = InterfacePropertyHolder.class.getDeclaredField("property");
            InterfacePropertyHolder.registerProperty(testField, null, null);
            FieldAccessHelper.get(testField, dog);
            fail("Should have thrown IllegalAccessException for null property name");
        } catch (IllegalAccessException e) {
            assertTrue(e.getMessage().contains("Property name cannot be null or empty"));
        } catch (Exception e) {
            // Expected - field access may fail in different ways
        }
    }
    
    @Test
    public void testBooleanPropertyWithIsPrefix() throws IllegalAccessException {
        scanner.readEntity();
        
        Bird bird = new Bird("bird-1", "Tweety", true);
        
        // Create a synthetic field for a boolean property
        try {
            java.lang.reflect.Method isMethod = Bird.class.getMethod("isCanFly");
            java.lang.reflect.Field syntheticField = InterfacePropertyHolder.class.getDeclaredField("property");
            InterfacePropertyHolder.registerProperty(syntheticField, "canFly", isMethod);
            
            // Test getting boolean property via 'is' prefix
            Object value = FieldAccessHelper.get(syntheticField, bird);
            assertEquals(true, value);
            
            // Test setting boolean property
            FieldAccessHelper.set(syntheticField, bird, false);
            assertEquals(false, bird.isCanFly());
        } catch (Exception e) {
            fail("Should be able to access boolean property with 'is' prefix: " + e.getMessage());
        }
    }
    
    @Test
    public void testMultipleClassesWithSameInterface() {
        // Verify that the scanner can handle multiple different implementations
        scanner.readEntity();
        ObjectIdField idField = scanner.getObjectIdField();
        assertNotNull(idField);
        
        // All three implementations should work with the same scanner
        Dog dog = new Dog("1", "Dog");
        Cat cat = new Cat("2", "Cat");
        Bird bird = new Bird("3", "Bird", true);
        
        try {
            assertEquals("1", FieldAccessHelper.get(idField.getField(), dog));
            assertEquals("2", FieldAccessHelper.get(idField.getField(), cat));
            assertEquals("3", FieldAccessHelper.get(idField.getField(), bird));
        } catch (IllegalAccessException e) {
            fail("Should be able to access id field on all implementations: " + e.getMessage());
        }
    }
}
