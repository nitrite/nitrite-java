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

import lombok.experimental.UtilityClass;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.exceptions.ErrorMessage;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.mapper.NitriteMapper;

import java.lang.reflect.Modifier;

import static org.dizitart.no2.common.Constants.RESERVED_NAMES;
import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.*;
import static org.dizitart.no2.util.DocumentUtils.dummyDocument;
import static org.dizitart.no2.util.StringUtils.isNullOrEmpty;

/**
 * A validation utility class.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
@UtilityClass
public class ValidationUtils {
    /**
     * Validates if a string is empty or `null`.
     *
     * @param value   the string value
     * @param message the error message
     */
    public static void notEmpty(String value, ErrorMessage message) {
        if (isNullOrEmpty(value)) {
            throw new ValidationException(message);
        }
    }

    /**
     * Validates if a {@link CharSequence} is empty or `null`.
     *
     * @param value   the value
     * @param message the message
     */
    public static void notEmpty(CharSequence value, ErrorMessage message) {
        if (isNullOrEmpty(value)) {
            throw new ValidationException(message);
        }
    }

    /**
     * Validates if an object is `null`.
     *
     * @param value   the object
     * @param message the message
     */
    public static void notNull(Object value, ErrorMessage message) {
        if (value == null) {
            throw new ValidationException(message);
        }
    }

    /**
     * Validates if an object array is empty.
     *
     * @param array   the object array
     * @param message the message
     */
    public static void notEmpty(Object[] array, ErrorMessage message) {
        if (array == null) throw new ValidationException(message);
        if (array.length == 0) throw new ValidationException(message);
    }

    /**
     * Validates a collection name.
     *
     * @param name the name
     */
    public static void validateCollectionName(String name) {
        notNull(name, errorMessage("name can not be null", VE_COLLECTION_NULL_NAME));
        notEmpty(name, errorMessage("name can not be empty", VE_COLLECTION_EMPTY_NAME));

        for (String reservedName : RESERVED_NAMES) {
            if (name.contains(reservedName)) {
                throw new ValidationException(errorMessage(
                        "name can not contains " + reservedName, VE_COLLECTION_NAME_RESERVED));
            }
        }
    }

    /**
     * Determines if a string is a valid collection name.
     *
     * @param name the name
     * @return `true` if valid; `false` otherwise.
     */
    public static boolean isValidCollectionName(String name) {
        if (isNullOrEmpty(name)) return false;
        for (String reservedName : RESERVED_NAMES) {
            if (name.contains(reservedName)) return false;
        }
        return true;
    }

    /**
     * Validates pagination limit in {@link FindOptions} against the total size.
     *
     * @param findOptions the {@link FindOptions}
     * @param totalSize   the total size
     */
    public static void validateLimit(FindOptions findOptions, long totalSize) {
        if (findOptions.getSize() < 0) {
            throw new ValidationException(PAGINATION_SIZE_CAN_NOT_BE_NEGATIVE);
        }

        if (findOptions.getOffset() < 0) {
            throw new ValidationException(PAGINATION_OFFSET_CAN_NOT_BE_NEGATIVE);
        }

        if (totalSize < findOptions.getOffset()) {
            throw new ValidationException(PAGINATION_OFFSET_GREATER_THAN_SIZE);
        }
    }

    /**
     * Validates if a field of a document can be indexed.
     *
     * @param fieldValue the field value
     * @param field      the field
     */
    public static void validateDocumentIndexField(Object fieldValue, String field) {
        if (fieldValue instanceof Document) {
            throw new InvalidOperationException(errorMessage(
                    "compound index on field " + field + " is not supported",
                    IOE_COMPOUND_INDEX));
        }

        if (fieldValue instanceof Iterable || fieldValue.getClass().isArray()) {
            throw new IndexingException(errorMessage("indexing on arrays or collections " +
                    "are not supported for field " + field, IE_INDEX_ON_ARRAY_NOT_SUPPORTED));
        }

        if (!(fieldValue instanceof Comparable)) {
            throw new IndexingException(errorMessage("can not index on non comparable field " + field,
                    IE_INDEX_ON_NON_COMPARABLE_FIELD));
        }
    }

    /**
     * Validates a search term.
     *
     * @param nitriteMapper the {@link NitriteMapper}
     * @param field        the field
     * @param value        the value
     */
    public static void validateSearchTerm(NitriteMapper nitriteMapper, String field, Object value) {
        notNull(field, errorMessage("field can not be null", VE_SEARCH_TERM_NULL_FIELD));
        notEmpty(field, errorMessage("field can not be empty", VE_SEARCH_TERM_EMPTY_FIELD));

        if (value != null) {
            if (!nitriteMapper.isValueType(value) && !(value instanceof Comparable)) {
                throw new ValidationException(errorMessage("search term is not comparable " + value,
                        FE_SEARCH_TERM_NOT_COMPARABLE));
            }
        }
    }

    /**
     * Validates an In filter value.
     *
     * @param field  the field
     * @param values the values
     */
    public static void validateInFilterValue(String field, Object[] values) {
        notNull(field, errorMessage("field can not be null", VE_IN_FILTER_NULL_FIELD));
        notEmpty(field, errorMessage("field can not be empty", VE_IN_FILTER_EMPTY_FIELD));
        notNull(values, errorMessage("values can not be null", VE_IN_FILTER_NULL_VALUES));
        if (values.length == 0) {
            throw new ValidationException(errorMessage("values can not be empty", VE_IN_FILTER_EMPTY_VALUES));
        }
    }

    /**
     * Validates if a field of an object can be indexed.
     *
     * @param nitriteMapper the {@link NitriteMapper}
     * @param fieldType     the field type
     * @param field         the field
     */
    static void validateObjectIndexField(NitriteMapper nitriteMapper, Class<?> fieldType, String field) {
        if (!Comparable.class.isAssignableFrom(fieldType) && !fieldType.isPrimitive()) {
            throw new IndexingException(errorMessage("can not index on non comparable field " + field,
                    IE_OBJ_INDEX_ON_NON_COMPARABLE_FIELD));
        }

        if (Iterable.class.isAssignableFrom(fieldType) || fieldType.isArray()) {
            throw new IndexingException(errorMessage("indexing on arrays or collections for field " + field
                    + " are not supported", IE_OBJ_INDEX_ON_ARRAY_NOT_SUPPORTED));
        }

        if (fieldType.isPrimitive()
                || fieldType == NitriteId.class
                || fieldType.isInterface()
                || Modifier.isAbstract(fieldType.getModifiers())) {
            // we will validate the solid class during insertion/update
            return;
        }

        Document document;
        try {
            document = dummyDocument(nitriteMapper, fieldType);
        } catch (Throwable e) {
            throw new IndexingException(errorMessage(
                    "invalid type specified " + fieldType.getName() + " for indexing",
                    IE_INVALID_TYPE_FOR_INDEX), e);
        }

        if (document == null || document.size() > 0) {
            throw new InvalidOperationException(errorMessage(
                    "compound index on field " + field + " is not supported",
                    IOE_OBJ_COMPOUND_INDEX));
        }
    }
}
