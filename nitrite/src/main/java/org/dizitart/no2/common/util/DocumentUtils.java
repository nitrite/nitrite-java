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

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.FieldValues;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.common.mapper.NitriteMapper;

import java.util.ArrayList;
import java.util.Objects;

import static org.dizitart.no2.common.util.ObjectUtils.newInstance;

/**
 * @author Anindya Chatterjee
 * @since 1.0
 */
public class DocumentUtils {
    private DocumentUtils(){}

    public static boolean isRecent(Document recent, Document older) {
        if (Objects.deepEquals(recent.getRevision(), older.getRevision())) {
            return recent.getLastModifiedSinceEpoch() >= older.getLastModifiedSinceEpoch();
        }
        return recent.getRevision() > older.getRevision();
    }

    public static Filter createUniqueFilter(Document document) {
        return Filter.byId(document.getId());
    }

    public static <T> Document skeletonDocument(NitriteMapper nitriteMapper, Class<T> type) {
        Object dummy = newInstance(type, true, nitriteMapper);
        Document document = (Document) nitriteMapper.tryConvert(dummy, Document.class);
        return removeValues(document);
    }

    public static boolean isSimilar(Document document, Document other, String... fields) {
        boolean result = true;
        if (document == null && other != null) return false;
        if (document != null && other == null) return false;
        if (document == null) return true;

        for (String field : fields) {
            result = result && Objects.deepEquals(document.get(field), other.get(field));
        }
        return result;
    }

    public static FieldValues getValues(Document document, Fields fields) {
        FieldValues fieldValues = new FieldValues();
        fieldValues.setNitriteId(document.getId());
        fieldValues.setFields(fields);
        fieldValues.setValues(new ArrayList<>());

        for (String field : fields.getFieldNames()) {
            Object value = document.get(field);
            fieldValues.getValues().add(new Pair<>(field, value));
        }

        return fieldValues;
    }

    public static boolean isAffectedByUpdate(Fields fields, Document updatedFields) {
        for (String field : fields.getFieldNames()) {
            if (updatedFields.containsKey(field)) {
                return true;
            }
        }
        return false;
    }

    private static Document removeValues(Document document) {
        if (document == null) return null;
        Document newDoc = Document.createDocument();
        for (Pair<String, Object> entry : document) {
            if (entry.getSecond() instanceof Document) {
                newDoc.put(entry.getFirst(), removeValues((Document) entry.getSecond()));
            } else {
                newDoc.put(entry.getFirst(), null);
            }
        }
        return newDoc;
    }
}
