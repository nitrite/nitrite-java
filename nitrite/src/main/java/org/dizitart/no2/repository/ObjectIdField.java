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

import lombok.Getter;
import lombok.Setter;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.repository.annotations.Order;

import java.lang.reflect.Field;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import static org.dizitart.no2.filters.FluentFilter.where;

/**
 * @author Anindya Chatterjee
 */
class ObjectIdField {
    private final Reflector reflector;
    private final IndexValidator indexValidator;
    private String[] fieldNames;

    @Getter
    @Setter
    private Field field;
    @Getter
    @Setter
    private boolean isEmbedded;

    public ObjectIdField() {
        this.reflector = new Reflector();
        this.indexValidator = new IndexValidator(reflector);
    }

    public String[] getFieldNames(NitriteMapper nitriteMapper) {
        if (fieldNames != null) {
            return fieldNames;
        }

        if (!isEmbedded) {
            fieldNames = new String[]{field.getName()};
            return fieldNames;
        }

        List<Field> fieldList = reflector.getAllFields(field.getType());
        NavigableMap<Integer, String> orderedFieldName = new TreeMap<>();
        for (Field field : fieldList) {
            String name = this.field.getName() + NitriteConfig.getFieldSeparator() + field.getName();
            indexValidator.validate(field.getType(), name, nitriteMapper);

            int order = getOrder(field);
            orderedFieldName.put(order, name);
        }

        fieldNames = orderedFieldName.values().toArray(new String[0]);
        return fieldNames;
    }

    public Filter createUniqueFilter(Object value, NitriteMapper nitriteMapper) {
        if (fieldNames.length == 1) {
            return where(field.getName()).eq(value);
        } else {
            Document document = nitriteMapper.convert(value, Document.class);
            Set<String> fields = document.getFields();
            Filter[] filters = new Filter[fields.size()];

            int index = 0;
            for (String field : fields) {
                Object fieldValue = document.get(field);
                filters[index++] = where(field).eq(fieldValue);
            }

            return Filter.and(filters);
        }
    }

    private int getOrder(Field field) {
        if (field.isAnnotationPresent(Order.class)) {
            Order order = field.getAnnotation(Order.class);
            return order.value();
        }
        throw new IndexingException("no order specified for the field " + field.getName());
    }
}
