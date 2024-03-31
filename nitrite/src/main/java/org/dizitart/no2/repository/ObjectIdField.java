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
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.filters.NitriteCommonFilters;
import org.dizitart.no2.filters.NitriteFilter;

import java.lang.reflect.Field;

import static org.dizitart.no2.filters.FluentFilter.where;

/**
 * @author Anindya Chatterjee
 * @since 4.0
 */
@Getter
@Setter
class ObjectIdField {
    // top level field name
    private String idFieldName;
    // child level field names
    private String[] fieldNames;
    // the id field of the entity class
    private Field field;
    // indicates if the id field is embedded
    private boolean isEmbedded;

    // fully qualified field name with embedded field separator
    public String[] getEmbeddedFieldNames() {
        if (!isEmbedded) {
            return new String[]{ idFieldName };
        }

        String[] fieldNames = new String[this.fieldNames.length];
        for (int i = 0; i < this.fieldNames.length; i++) {
            String name = this.idFieldName + NitriteConfig.getFieldSeparator() + this.fieldNames[i];
            fieldNames[i] = name;
        }

        return fieldNames;
    }

    // creates a filter for the id field
    public Filter createUniqueFilter(Object value, NitriteMapper nitriteMapper) {
        if (getEmbeddedFieldNames().length == 1) {
            return where(idFieldName).eq(value);
        } else {
            Document document = (Document) nitriteMapper.tryConvert(value, Document.class);
            Filter[] filters = new Filter[fieldNames.length];

            int index = 0;
            for (String field : fieldNames) {
                String filterField = idFieldName + NitriteConfig.getFieldSeparator() + field;
                Object fieldValue = document.get(field);
                filters[index++] = where(filterField).eq(fieldValue);
            }

            NitriteFilter nitriteFilter = (NitriteFilter) NitriteCommonFilters.and(filters);
            nitriteFilter.setObjectFilter(true);
            return nitriteFilter;
        }
    }
}
