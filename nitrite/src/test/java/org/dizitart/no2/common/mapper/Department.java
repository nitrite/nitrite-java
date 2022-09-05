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

package org.dizitart.no2.common.mapper;

import lombok.Data;
import lombok.ToString;
import org.dizitart.no2.collection.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Anindya Chatterjee
 */
@Data
@ToString
public class Department {
    private String name;
    private List<MappableEmployee> employeeList;


    public static class DepartmentConverter implements EntityConverter<Department> {

        @Override
        public Class<Department> getEntityType() {
            return Department.class;
        }

        @Override
        public Document toDocument(Department entity, NitriteMapper nitriteMapper) {
            List<Document> docList = new ArrayList<>();
            if (entity.employeeList != null && !entity.employeeList.isEmpty()) {
                entity.employeeList.stream().map(employee -> nitriteMapper.convert(employee, Document.class))
                    .forEach(docList::add);
            }

            return Document.createDocument().put("name", entity.name)
                .put("employeeList", docList);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Department fromDocument(Document document, NitriteMapper nitriteMapper) {
            Department entity = new Department();
            entity.employeeList = new ArrayList<>();
            List<Document> documentList = (List<Document>) document.get("employeeList", ArrayList.class);
            if (documentList != null && !documentList.isEmpty()) {
                documentList.stream().map(doc -> nitriteMapper.convert(doc, MappableEmployee.class))
                    .forEach(entity.employeeList::add);
            }
            entity.name = document.get("name", String.class);
            return entity;
        }
    }
}
