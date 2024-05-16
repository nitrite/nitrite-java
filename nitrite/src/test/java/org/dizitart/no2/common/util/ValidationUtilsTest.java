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

package org.dizitart.no2.common.util;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.common.mapper.SimpleNitriteMapper;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.integration.Retry;
import org.dizitart.no2.integration.repository.data.ClassA;
import org.dizitart.no2.integration.repository.data.ClassBConverter;
import org.dizitart.no2.integration.repository.data.ClassC;
import org.dizitart.no2.integration.repository.data.EmptyClass;
import org.junit.Rule;
import org.junit.Test;

import static org.dizitart.no2.common.util.ValidationUtils.*;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee.
 */
public class ValidationUtilsTest {
    @Rule
    public Retry retry = new Retry(3);

    @Test
    public void testNotEmpty() {
        boolean exception = false;
        try {
            notEmpty("", "empty string");
        } catch (ValidationException e) {
            exception = true;
            assertEquals(e.getMessage(), "empty string");
        } finally {
            assertTrue(exception);
        }
    }

    @Test
    public void testNotNull() {
        boolean exception = false;
        String a = null;
        try {
            notNull(a, "null string");
        } catch (ValidationException e) {
            exception = true;
            assertEquals(e.getMessage(), "null string");
        } finally {
            assertTrue(exception);
        }
    }

    @Test
    public void testCharSequenceNotEmpty() {
        CharSequence cs = "";
        boolean invalid = false;
        try {
            notEmpty(cs, "test");
        } catch (ValidationException iae) {
            invalid = true;
            assertEquals(iae.getMessage(), "test");
        } finally {
            assertTrue(invalid);
        }
    }

    @Test
    public void testValidateProjectionType() {
        SimpleNitriteMapper documentMapper = new SimpleNitriteMapper();
        documentMapper.registerEntityConverter(new ClassA.ClassAConverter());
        documentMapper.registerEntityConverter(new ClassBConverter());
        documentMapper.registerEntityConverter(new EmptyClass.Converter());

        validateProjectionType(ClassA.class, documentMapper);

        assertThrows(ValidationException.class, () -> validateProjectionType(EmptyClass.class, documentMapper));
        assertThrows(ValidationException.class, () -> validateProjectionType(ClassC.class, documentMapper));
        assertThrows(ValidationException.class, () -> validateProjectionType(String.class, documentMapper));
        assertThrows(ValidationException.class, () -> validateProjectionType(Number.class, documentMapper));
        assertThrows(ValidationException.class, () -> validateProjectionType(Integer.class, documentMapper));
        assertThrows(ValidationException.class, () -> validateProjectionType(Object.class, documentMapper));
    }

    @Test
    public void testValidateRepositoryType() {
        NitriteConfig nitriteConfig = new NitriteConfig();
        nitriteConfig.registerEntityConverter(new ClassA.ClassAConverter());
        nitriteConfig.registerEntityConverter(new ClassBConverter());
        nitriteConfig.registerEntityConverter(new EmptyClass.Converter());
        nitriteConfig.autoConfigure();

        validateRepositoryType(ClassA.class, nitriteConfig);

        assertThrows(ValidationException.class, () -> validateRepositoryType(EmptyClass.class, nitriteConfig));
        assertThrows(ValidationException.class, () -> validateRepositoryType(ClassC.class, nitriteConfig));
        assertThrows(ValidationException.class, () -> validateRepositoryType(String.class, nitriteConfig));
        assertThrows(ValidationException.class, () -> validateRepositoryType(Number.class, nitriteConfig));
        assertThrows(ValidationException.class, () -> validateRepositoryType(Integer.class, nitriteConfig));
        assertThrows(ValidationException.class, () -> validateRepositoryType(Object.class, nitriteConfig));
    }
}
