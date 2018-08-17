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
import org.dizitart.no2.collection.Filter;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.mapper.NitriteMapper;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.dizitart.no2.common.Constants.DOC_ID;
import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.*;
import static org.dizitart.no2.filters.Filters.eq;
import static org.dizitart.no2.util.StringUtils.isNullOrEmpty;

/**
 * A utility class for {@link Document}.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
@UtilityClass
public class DocumentUtils {
    /**
     * Field separator.
     * */
    static String FIELD_SEPARATOR = ".";

    private static PodamFactory factory = new PodamFactoryImpl();

    /**
     * Gets all first level fields of a document.
     *
     * @param document the document.
     * @return the fields of the document.
     */
    public static Set<String> getFields(Document document) {
        return getFieldsInternal(document, "");
    }

    /**
     * Gets the value of a value inside a document.
     *
     * @param document the document
     * @param field    the value
     * @return the value of the value.
     */
    public static Object getFieldValue(Document document, String field) {
        Object fieldValue;
        if (field.contains(FIELD_SEPARATOR)) {
            fieldValue = getEmbeddedValue(document, field);
        } else {
            fieldValue = document.get(field);
        }
        return fieldValue;
    }

    /**
     * Creates an empty document from a {@link Class} definition. All value values
     * are initialized to `null`. Such empty document is used for projection purpose.
     *
     * @param <T>           the type parameter
     * @param nitriteMapper the {@link NitriteMapper} to create the document.
     * @param type          the class definition.
     * @return the empty document
     */
    public static <T> Document emptyDocument(NitriteMapper nitriteMapper, Class<T> type) {
        if (type.isPrimitive()) {
            throw new ValidationException(CAN_NOT_PROJECT_TO_PRIMITIVE);
        } else if (type.isInterface()) {
            throw new ValidationException(CAN_NOT_PROJECT_TO_INTERFACE);
        } else if (type.isArray()) {
            throw new ValidationException(CAN_NOT_PROJECT_TO_ARRAY);
        } else if (Modifier.isAbstract(type.getModifiers())) {
            throw new ValidationException(CAN_NOT_PROJECT_TO_ABSTRACT);
        }

        Document dummyDoc = dummyDocument(nitriteMapper, type);
        Document filtered = removeValues(dummyDoc);
        if (filtered == null) {
            throw new ValidationException(CAN_NOT_PROJECT_TO_EMPTY_TYPE);
        } else {
            return filtered;
        }
    }

    /**
     * Determines whether a document has recently been updated/created than the other.
     *
     * @param recent the recent document
     * @param older  the older document
     * @return the boolean value
     */
    public static boolean isRecent(Document recent, Document older) {
        if (recent.getRevision() == older.getRevision()) {
            return recent.getLastModifiedTime() >= older.getLastModifiedTime();
        }
        return recent.getRevision() > older.getRevision();
    }

    /**
     * Create unique filter to identify the `document`.
     *
     * @param document the document
     * @return the unique filter
     */
    public static Filter createUniqueFilter(Document document) {
        return eq(DOC_ID, document.getId().getIdValue());
    }

    static <T> Document dummyDocument(NitriteMapper nitriteMapper, Class<T> type) {
        T dummy = factory.manufacturePojo(type);
        return nitriteMapper.asDocument(dummy);
    }

    private static Set<String> getFieldsInternal(Document document, String prefix) {
        Set<String> fields = new TreeSet<>();
        if (document == null) return fields;

        for (KeyValuePair entry : document) {
            Object value = entry.getValue();
            if (value instanceof Document) {
                if (isNullOrEmpty(prefix)) {
                    fields.addAll(getFieldsInternal((Document) value, entry.getKey()));
                } else {
                    fields.addAll(getFieldsInternal((Document) value, prefix + FIELD_SEPARATOR + entry.getKey()));
                }
            } else if (!(value instanceof Iterable)) {
                if (StringUtils.isNullOrEmpty(prefix)) {
                    fields.add(entry.getKey());
                } else {
                    fields.add(prefix + FIELD_SEPARATOR + entry.getKey());
                }
            }
        }
        return fields;
    }

    @SuppressWarnings("unchecked")
    private static Object getEmbeddedValue(Document document, String embeddedField) {
        String regex = "\\" + FIELD_SEPARATOR;
        String[] split = embeddedField.split(regex, 2);
        String key = split[0];

        if (isNullOrEmpty(key)) {
            throw new ValidationException(INVALID_EMBEDDED_FIELD);
        }

        Object object = document.get(key);
        if (object == null) return null;

        String remainingKey = split[1];

        if (object instanceof Document) {
            return getFieldValue((Document) object, remainingKey);
        } else if (object instanceof List) {
            int index = asInteger(remainingKey);
            if (index == -1) {
                throw new ValidationException(errorMessage(
                        "invalid index " + remainingKey + " for collection",
                        VE_NEGATIVE_LIST_INDEX_FIELD));
            }
            List collection = (List) object;
            if (index >= collection.size()) {
                throw new ValidationException(errorMessage("index = " + remainingKey +
                        " is not less than the size of the collection '" + key +
                        "' = " + collection.size(), VE_INVALID_LIST_INDEX_FIELD));
            }
            return collection.get(index);
        } else if (object.getClass().isArray()) {
            int index = asInteger(remainingKey);
            if (index == -1) {
                throw new ValidationException(errorMessage(
                        "invalid index " + remainingKey + " for collection",
                        VE_NEGATIVE_ARRAY_INDEX_FIELD));
            }
            Object[] array = getArray(object);
            if (index >= array.length) {
                throw new ValidationException(errorMessage("index = " + remainingKey +
                        " is not less than the size of the collection '" + key +
                        "' = " + array.length, VE_INVALID_ARRAY_INDEX_FIELD));
            }
            return array[index];
        } else {
            throw new ValidationException(errorMessage("invalid remaining field "
                    + remainingKey, VE_INVALID_REMAINING_FIELD));
        }
    }

    private static int asInteger(String number) {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private Object[] getArray(Object val){
        int length = Array.getLength(val);
        Object[] outputArray = new Object[length];
        for(int i = 0; i < length; ++i){
            outputArray[i] = Array.get(val, i);
        }
        return outputArray;
    }

    private static Document removeValues(Document dummyDoc) {
        if (dummyDoc == null) return null;
        for (KeyValuePair entry : dummyDoc) {
            if (entry.getValue() instanceof Map) {
                dummyDoc.put(entry.getKey(), removeValues((Document) entry.getValue()));
            } else {
                dummyDoc.put(entry.getKey(), null);
            }
        }
        return dummyDoc;
    }
}
