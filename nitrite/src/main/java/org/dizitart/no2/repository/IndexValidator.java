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
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.repository.annotations.Order;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import static org.dizitart.no2.common.util.DocumentUtils.skeletonDocument;

/**
 * @author Anindya Chatterjee
 */
public class IndexValidator {
    private final Reflector reflector;

    public IndexValidator(Reflector reflector) {
        this.reflector = reflector;
    }

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
            || nitriteMapper.isValueType(fieldType)
            || Modifier.isAbstract(fieldType.getModifiers())
            || fieldType.isArray()
            || Iterable.class.isAssignableFrom(fieldType)) {
            // we will validate the solid class during insertion/update
            return;
        }

        Document document;
        try {
            document = skeletonDocument(nitriteMapper, fieldType);
            if (document.size() > 1) {
                // compound index
                List<Field> fields = reflector.getAllFields(fieldType);
                for (Field indexField : fields) {
                    if (!indexField.isAnnotationPresent(Order.class)) {
                        throw new IndexingException("@Order must be specified for all fields in the embedded id object");
                    }
                }
            } else {
                if (!Comparable.class.isAssignableFrom(fieldType)) {
                    throw new IndexingException("cannot index on non comparable field " + field);
                }
            }
        } catch (IndexingException ie) {
            throw ie;
        } catch (Throwable e) {
            throw new IndexingException("invalid type specified " + fieldType.getName() + " for indexing", e);
        }
    }
}
