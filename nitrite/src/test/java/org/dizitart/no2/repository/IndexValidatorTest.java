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

import org.dizitart.no2.NitriteBuilderTest;
import org.dizitart.no2.exceptions.IndexingException;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertThrows;

public class IndexValidatorTest {

    @Test
    public void testValidate() {
        IndexValidator indexValidator = new IndexValidator(new Reflector());
        Class<?> fieldType = Object.class;
        assertThrows(IndexingException.class,
            () -> indexValidator.validate(fieldType, "Field", new NitriteBuilderTest.CustomNitriteMapper()));
    }

    @Test
    public void testValidate3() {
        IndexValidator indexValidator = new IndexValidator(new Reflector());
        Class<?> fieldType = Field.class;
        assertThrows(IndexingException.class,
            () -> indexValidator.validate(fieldType, "Field", new NitriteBuilderTest.CustomNitriteMapper()));
    }

    @Test
    public void testValidate4() {
        IndexValidator indexValidator = new IndexValidator(new Reflector());
        Class<?> fieldType = Object.class;
        assertThrows(IndexingException.class, () -> indexValidator.validate(fieldType, "invalid type specified ",
            new NitriteBuilderTest.CustomNitriteMapper()));
    }
}

