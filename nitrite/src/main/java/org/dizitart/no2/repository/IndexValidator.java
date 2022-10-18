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

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.common.util.ObjectUtils;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.repository.annotations.Id;

import java.lang.reflect.Modifier;

/**
 * @author Anindya Chatterjee
 */
public class IndexValidator {

    /**
     * Validate an index field of an {@link org.dizitart.no2.repository.annotations.Entity} object.
     *
     * @param fieldType     the field type
     * @param field         the field
     * @param nitriteMapper the nitrite mapper
     */
    public void validate(Class<?> fieldType, String field, NitriteMapper nitriteMapper) {
        if (fieldType.isPrimitive()
            || fieldType == NitriteId.class
            || fieldType.isInterface()
//            || ObjectUtils.isValueType(fieldType, nitriteMapper)
            || Modifier.isAbstract(fieldType.getModifiers())
            || fieldType.isArray()
            || Iterable.class.isAssignableFrom(fieldType)) {
            // we will validate the solid class during insertion/update
            return;
        }

        if (!Comparable.class.isAssignableFrom(fieldType)) {
            throw new IndexingException("Cannot create index on non comparable field " + field);
        }
    }

    public void validateId(Id id, Class<?> fieldType, String field, NitriteMapper nitriteMapper) {
        if (fieldType.isPrimitive()
            || fieldType == NitriteId.class) {
            return;
        }

        Object dummyValue = ObjectUtils.newInstance(fieldType, true, nitriteMapper);
        Document dummyDocument = nitriteMapper.convert(dummyValue, Document.class);

        if (dummyDocument != null && dummyDocument.size() != 0 && id.embeddedFields().length == 0) {
            throw new IndexingException("Invalid Id field " + field);
        }
    }

    public void validateId(EntityId entityId, Class<?> fieldType, String field, NitriteMapper nitriteMapper) {
        if (fieldType.isPrimitive()
            || fieldType == NitriteId.class) {
            return;
        }

        Object dummyValue = ObjectUtils.newInstance(fieldType, true, nitriteMapper);
        Document dummyDocument = nitriteMapper.convert(dummyValue, Document.class);

        if (dummyDocument.size() != 0 && entityId.getEmbeddedFieldNames().size() == 0) {
            throw new IndexingException("Invalid Id field " + field);
        }
    }
}
