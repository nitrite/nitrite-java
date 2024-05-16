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
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.ValidationException;

import java.lang.reflect.Modifier;
import java.util.Collection;

import static org.dizitart.no2.common.util.ObjectUtils.*;
import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;

/**
 * @author Anindya Chatterjee
 * @since 1.0
 */
public class ValidationUtils {
    private ValidationUtils() {
    }

    public static void notEmpty(String value, String message) {
        if (isNullOrEmpty(value)) {
            throw new ValidationException(message);
        }
    }

    public static void notEmpty(CharSequence value, String message) {
        if (isNullOrEmpty(value)) {
            throw new ValidationException(message);
        }
    }

    public static void notEmpty(Collection<?> value, String message) {
        if (value.isEmpty()) {
            throw new ValidationException(message);
        }
    }

    public static <T> void notEmpty(T[] value, String message) {
        if (value.length == 0) {
            throw new ValidationException(message);
        }
    }

    public static void notNull(Object value, String message) {
        if (value == null) {
            throw new ValidationException(message);
        }
    }

    public static <T> void containsNull(T[] array, String message) {
        for (T element : array) {
            if (element == null) {
                throw new ValidationException(message);
            }
        }
    }

    public static void validateIterableIndexField(Iterable<?> fieldValue, String field) {
        if (fieldValue != null) {
            for (Object value : fieldValue) {
                if (value == null) continue;
                validateArrayIndexItem(value, field);
            }
        }
    }

    public static void validateStringIterableIndexField(Iterable<?> fieldValue, String field) {
        if (fieldValue != null) {
            for (Object value : fieldValue) {
                if (value == null) continue;
                validateStringArrayItem(value, field);
            }
        }
    }

    public static void validateArrayIndexField(Object arrayValue, String field) {
        if (arrayValue != null) {
            Object[] array = convertToObjectArray(arrayValue);
            for (Object value : array) {
                if (value == null) continue;
                validateArrayIndexItem(value, field);
            }
        }
    }

    public static void validateStringArrayIndexField(Object arrayValue, String field) {
        if (arrayValue != null) {
            Object[] array = convertToObjectArray(arrayValue);
            for (Object value : array) {
                if (value == null) continue;
                validateStringArrayItem(value, field);
            }
        }
    }

    public static void validateFilterArrayField(Object arrayValue, String field) {
        if (arrayValue != null) {
            Object[] array = convertToObjectArray(arrayValue);
            for (Object value : array) {
                if (value == null) continue;
                validateArrayFilterItem(value, field);
            }
        }
    }

    public static void validateFilterIterableField(Iterable<?> fieldValue, String field) {
        if (fieldValue != null) {
            for (Object value : fieldValue) {
                if (value == null) continue;
                validateArrayFilterItem(value, field);
            }
        }
    }

    public static void validateProjectionType(Class<?> type, NitriteMapper nitriteMapper) {
        Object value;
        try {
            value = newInstance(type, false, nitriteMapper);
        } catch (Exception e) {
            throw new ValidationException("Invalid projection type", e);
        }

        if (value == null) {
            throw new ValidationException("Invalid projection type");
        }

        Document document = (Document) nitriteMapper.tryConvert(value, Document.class);
        if (document == null || document.size() == 0) {
            throw new ValidationException("Cannot project to empty type " + type);
        }
    }

    public static void validateRepositoryType(Class<?> type, NitriteConfig nitriteConfig) {
        Object value;
        try {
            if (type.isInterface() || (Modifier.isAbstract(type.getModifiers()) && !isBuiltInValueType(type))) {
                // defer validation during insertion
                return;
            }

            if (!nitriteConfig.isRepositoryTypeValidationDisabled()) {
                NitriteMapper nitriteMapper = nitriteConfig.nitriteMapper();
                value = newInstance(type, false, nitriteMapper);
                if (value == null) {
                    throw new ValidationException("Cannot create new instance of type " + type);
                }

                Document document = (Document) nitriteMapper.tryConvert(value, Document.class);
                if (document == null || document.size() == 0) {
                    throw new ValidationException("Cannot convert to document from type " + type);
                }
            }
        } catch (Exception e) {
            throw new ValidationException("Invalid repository type", e);
        }
    }

    private static void validateArrayIndexItem(Object value, String field) {
        if (value instanceof Iterable || value.getClass().isArray()) {
            throw new InvalidOperationException("Nested iterables are not supported");
        }

        if (!(value instanceof Comparable)) {
            throw new IndexingException("Each value in the iterable field " + field + " must implement Comparable");
        }
    }

    private static void validateStringArrayItem(Object value, String field) {
        if (!(value instanceof String) && (value instanceof Iterable || value.getClass().isArray())) {
            throw new InvalidOperationException("Nested iterables are not supported");
        }

        if (!(value instanceof String)) {
            throw new IndexingException("Each value in the iterable field " + field + " must be a string");
        }
    }

    private static void validateArrayFilterItem(Object value, String field) {
        if (value instanceof Iterable || value.getClass().isArray()) {
            throw new InvalidOperationException("Nested array is not supported");
        }

        if (!(value instanceof Comparable)) {
            throw new IndexingException("Cannot filter using non comparable values " + field);
        }
    }
}
