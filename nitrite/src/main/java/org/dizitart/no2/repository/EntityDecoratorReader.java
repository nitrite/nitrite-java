/*
 * Copyright (c) 2017-2022 Nitrite author or authors.
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

import lombok.AccessLevel;
import lombok.Getter;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.index.IndexFields;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.dizitart.no2.index.IndexOptions.indexOptions;

/**
 * @author Anindya Chatterjee
 */
public class EntityDecoratorReader {
    private final EntityDecorator<?> entityDecorator;
    private final NitriteCollection collection;
    private final NitriteMapper nitriteMapper;
    private final IndexValidator indexValidator;

    private final Reflector reflector;

    @Getter(AccessLevel.PACKAGE)
    private final Set<IndexFields> indices;

    @Getter
    private ObjectIdField objectIdField;

    public EntityDecoratorReader(EntityDecorator<?> entityDecorator,
                                 NitriteCollection collection,
                                 NitriteMapper nitriteMapper) {
        this.entityDecorator = entityDecorator;
        this.collection = collection;
        this.nitriteMapper = nitriteMapper;
        this.indices = new HashSet<>();
        this.indexValidator = new IndexValidator();
        this.reflector = new Reflector();
    }

    public void readEntity() {
        readIndices();
        readIdField();
    }

    public void createIndices() {
        for (IndexFields index : indices) {
            String[] fields = index.getFieldNames().toArray(new String[0]);
            if (!collection.hasIndex(fields)) {
                collection.createIndex(indexOptions(index.getIndexType()), fields);
            }
        }
    }

    public void createIdIndex() {
        if (objectIdField != null) {
            String[] fieldNames = objectIdField.getEmbeddedFieldNames();
            if (!collection.hasIndex(fieldNames)) {
                collection.createIndex(fieldNames);
            }
        }
    }

    private void readIndices() {
        if (entityDecorator.getIndexFields() != null) {
            for (IndexFields indexField : entityDecorator.getIndexFields()) {
                List<String> names = indexField.getFieldNames();
                List<Field> entityFields = new ArrayList<>();

                for (String name : names) {
                    Field field = reflector.getField(entityDecorator.getEntityType(), name);
                    if (field != null) {
                        entityFields.add(field);
                        indexValidator.validate(field.getType(), field.getName(), nitriteMapper);
                    }
                }

                if (entityFields.size() == names.size()) {
                    // validation for all field are success
                    indices.add(indexField);
                }
            }
        }
    }

    private void readIdField() {
        if (entityDecorator != null) {
            EntityId entityId = entityDecorator.getIdField();
            if (entityId != null) {
                String idFieldName = entityId.getFieldName();
                if (!StringUtils.isNullOrEmpty(idFieldName)) {
                    Field field = reflector.getField(entityDecorator.getEntityType(), idFieldName);
                    indexValidator.validateId(entityId, field.getType(), idFieldName, nitriteMapper);

                    objectIdField = new ObjectIdField();
                    objectIdField.setField(field);
                    objectIdField.setIdFieldName(idFieldName);
                    objectIdField.setEmbedded(entityId.isEmbedded());
                    objectIdField.setFieldNames(entityId.getSubFields());
                }
            }
        }
    }
}
