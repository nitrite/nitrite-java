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

import org.dizitart.no2.exceptions.ValidationException;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class ReflectorTest {
    @Test
    public void testFindInheritedAnnotations() {
        Reflector reflector = new Reflector();
        Class<Annotation> annotation = Annotation.class;
        assertTrue(reflector.<Annotation>findInheritedAnnotations(annotation, Object.class).isEmpty());
    }

    @Test
    public void testFindInheritedAnnotations3() {
        Reflector reflector = new Reflector();
        Class<Annotation> annotation = Annotation.class;
        assertTrue(reflector.<Annotation>findInheritedAnnotations(annotation, Field.class).isEmpty());
    }

    @Test
    public void testGetEmbeddedField() {
        Reflector reflector = new Reflector();
        assertThrows(ValidationException.class, () -> reflector.getEmbeddedField(Object.class, "Embedded Field"));
    }

    @Test
    public void testGetEmbeddedField2() {
        Reflector reflector = new Reflector();
        assertThrows(ValidationException.class, () -> reflector.getEmbeddedField(Object.class, "\\{0}"));
    }

    @Test
    public void testGetEmbeddedField3() {
        Reflector reflector = new Reflector();
        assertThrows(ValidationException.class, () -> reflector.getEmbeddedField(Object.class, ""));
    }

    @Test
    public void testGetEmbeddedField4() {
        Reflector reflector = new Reflector();
        assertThrows(ValidationException.class, () -> reflector.getEmbeddedField(Object.class, "java.lang.Object"));
    }

    @Test
    public void testGetFieldsUpto() {
        Reflector reflector = new Reflector();
        Class<?> startClass = Object.class;
        assertTrue(reflector.getFieldsUpto(startClass, Object.class).isEmpty());
    }


    @Test
    public void testGetField() {
        Reflector reflector = new Reflector();
        assertThrows(ValidationException.class, () -> reflector.getField(Object.class, "Name"));
    }


    @Test
    public void testGetField3() {
        Reflector reflector = new Reflector();
        assertThrows(ValidationException.class,
            () -> reflector.getField(Object.class, "startClass cannot be null"));
    }

    @Test
    public void testGetField4() {
        Reflector reflector = new Reflector();
        assertThrows(ValidationException.class, () -> reflector.getField(Object.class, "no such field '"));
    }

    @Test
    public void testGetField5() {
        Reflector reflector = new Reflector();
        assertThrows(ValidationException.class, () -> reflector.getField(Object.class, "java.lang.Object"));
    }

    @Test
    public void testGetAllFields() {
        Reflector reflector = new Reflector();
        assertTrue(reflector.getAllFields(Object.class).isEmpty());
    }
}

