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
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.filters.NitriteFilter;
import org.dizitart.no2.repository.annotations.Embedded;

import java.lang.reflect.Field;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import static org.dizitart.no2.filters.FluentFilter.where;

/**
 * @author Anindya Chatterjee
 */
class ObjectIdField {
    private final Reflector reflector;
    private final IndexValidator indexValidator;
    private String[] embeddedFieldNames;

    @Getter
    @Setter
    private Field field;

    @Getter
    @Setter
    private boolean isEmbedded;

    @Getter
    @Setter
    private String idFieldName;

    public ObjectIdField() {
        this.reflector = new Reflector();
        this.indexValidator = new IndexValidator(reflector);
    }

    public String[] getFieldNames(NitriteMapper nitriteMapper) {
        if (embeddedFieldNames != null) {
            return embeddedFieldNames;
        }

        if (!isEmbedded) {
            embeddedFieldNames = new String[]{ idFieldName };
            return embeddedFieldNames;
        }

        List<Field> fieldList = reflector.getAllFields(field.getType());
        NavigableMap<Integer, String> orderedFieldName = new TreeMap<>();

        boolean embeddedFieldFound = false;
        for (Field field : fieldList) {
            if (field.isAnnotationPresent(Embedded.class)) {
                embeddedFieldFound = true;
                Embedded embedded = field.getAnnotation(Embedded.class);
                int order = embedded.order();
                String fieldName = StringUtils.isNullOrEmpty(embedded.fieldName())
                    ? field.getName() : embedded.fieldName();

                String name = this.idFieldName + NitriteConfig.getFieldSeparator() + fieldName;
                indexValidator.validate(field.getType(), name, nitriteMapper);

                orderedFieldName.put(order, name);
            }
        }

        if (!embeddedFieldFound) {
            throw new IndexingException("No embedded field found for " + field.getName());
        }

        embeddedFieldNames = orderedFieldName.values().toArray(new String[0]);
        return embeddedFieldNames;
    }

    public Filter createUniqueFilter(Object value, NitriteMapper nitriteMapper) {
        if (embeddedFieldNames.length == 1) {
            return where(idFieldName).eq(value);
        } else {
            Document document = nitriteMapper.convert(value, Document.class);
            Filter[] filters = new Filter[embeddedFieldNames.length];

            int index = 0;
            for (String field : embeddedFieldNames) {
                String docFieldName = getEmbeddedFieldName(field);
                Object fieldValue = document.get(docFieldName);
                filters[index++] = where(field).eq(fieldValue);
            }

            NitriteFilter nitriteFilter = (NitriteFilter) Filter.and(filters);
            nitriteFilter.setObjectFilter(true);
            return nitriteFilter;
        }
    }

    private String getEmbeddedFieldName(String fieldName) {
        if (fieldName.contains(NitriteConfig.getFieldSeparator())) {
            return fieldName.substring(fieldName.indexOf(NitriteConfig.getFieldSeparator()) + 1);
        } else {
            return fieldName;
        }
    }
}
