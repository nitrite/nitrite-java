/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.util;

import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.mapper.JacksonMapper;
import org.dizitart.no2.mapper.NitriteMapper;
import org.junit.Test;

import java.util.List;

import static org.dizitart.no2.common.Constants.*;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;
import static org.dizitart.no2.util.ValidationUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee.
 */
public class ValidationUtilsTest {

    @Test
    public void testNotEmpty() {
        boolean exception = false;
        try {
            notEmpty("", errorMessage("empty string", 0));
        } catch (ValidationException e) {
            exception = true;
            assertEquals(e.getErrorMessage().getMessage(), "empty string");
        } finally {
            assertTrue(exception);
        }
    }

    @Test
    public void testNotNull() {
        boolean exception = false;
        String a = null;
        try {
            notNull(a, errorMessage("null string", 0));
        } catch (ValidationException e) {
            exception = true;
            assertEquals(e.getErrorMessage().getMessage(), "null string");
        } finally {
            assertTrue(exception);
        }
    }

    @Test
    public void testNotEmptyArray() {
        boolean exception = false;
        Object[] array = null;
        try {
            notEmpty(array, errorMessage("null array", 0));
        } catch (ValidationException e) {
            exception = true;
            assertEquals(e.getErrorMessage().getMessage(), "null array");
        } finally {
            assertTrue(exception);
        }

        exception = false;
        array = new Object[0];
        try {
            notEmpty(array, errorMessage("empty array", 0));
        } catch (ValidationException e) {
            exception = true;
            assertEquals(e.getErrorMessage().getMessage(), "empty array");
        } finally {
            assertTrue(exception);
        }
    }

    @Test
    public void testValidateLimit() {
        boolean exception = false;
        try {
            validateLimit(FindOptions.limit(1, -2), 3);
        } catch (ValidationException e) {
            exception = true;
            assertEquals(e.getErrorMessage().getMessage(), "pagination size can not be negative");
        } finally {
            assertTrue(exception);
        }

        exception = false;
        try {
            validateLimit(FindOptions.limit(-1, 2), 3);
        } catch (ValidationException e) {
            exception = true;
            assertEquals(e.getErrorMessage().getMessage(), "pagination offset can not be negative");
        } finally {
            assertTrue(exception);
        }

        exception = false;
        try {
            validateLimit(FindOptions.limit(4, 2), 3);
        } catch (ValidationException e) {
            exception = true;
            assertEquals(e.getErrorMessage().getMessage(), "pagination offset is greater than total size");
        } finally {
            assertTrue(exception);
        }
    }

    @Test
    public void testValidateObjectIndexField() {
        NitriteMapper nitriteMapper = new JacksonMapper();
        validateObjectIndexField(nitriteMapper, String.class, "dummy");

        boolean invalid = false;
        try {
            validateObjectIndexField(nitriteMapper, IndexUtilsTest.class, "dummy");
        } catch (IndexingException ie) {
            invalid = true;
        } finally {
            assertTrue(invalid);
        }

        invalid = false;
        try {
            validateObjectIndexField(nitriteMapper, List.class, "dummy");
        } catch (IndexingException ie) {
            invalid = true;
        } finally {
            assertTrue(invalid);
        }

        invalid = false;
        try {
            validateObjectIndexField(nitriteMapper, Character[].class, "dummy");
        } catch (IndexingException ie) {
            invalid = true;
        } finally {
            assertTrue(invalid);
        }
    }

    @Test
    public void testCharSequenceNotEmpty() {
        CharSequence cs = "";
        boolean invalid = false;
        try {
            notEmpty(cs, errorMessage("test", 0));
        } catch (ValidationException iae) {
            invalid = true;
            assertEquals(iae.getErrorMessage().getMessage(), "test");
        } finally {
            assertTrue(invalid);
        }
    }

    @Test
    public void testValidateCollectionName() {
        String collectionName = "z" + INTERNAL_NAME_SEPARATOR + "a";
        boolean invalid = false;
        try {
            validateCollectionName(collectionName);
        } catch (ValidationException ve) {
            invalid = true;
        } finally {
            assertTrue(invalid);
        }

        collectionName = "a" + USER_MAP + "b";
        invalid = false;
        try {
            validateCollectionName(collectionName);
        } catch (ValidationException ve) {
            invalid = true;
        } finally {
            assertTrue(invalid);
        }

        collectionName = "a" + INDEX_META_PREFIX + "b";
        invalid = false;
        try {
            validateCollectionName(collectionName);
        } catch (ValidationException ve) {
            invalid = true;
        } finally {
            assertTrue(invalid);
        }

        collectionName = "a" + INDEX_PREFIX + "b";
        invalid = false;
        try {
            validateCollectionName(collectionName);
        } catch (ValidationException ve) {
            invalid = true;
        } finally {
            assertTrue(invalid);
        }

        collectionName = "a" + OBJECT_STORE_NAME_SEPARATOR + "b";
        invalid = false;
        try {
            validateCollectionName(collectionName);
        } catch (ValidationException ve) {
            invalid = true;
        } finally {
            assertTrue(invalid);
        }
    }
}
