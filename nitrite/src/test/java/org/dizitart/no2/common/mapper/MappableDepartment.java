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
public class MappableDepartment {
    private String name;
    private List<MappableEmployee> employeeList;


    private List<MappableEmployee> getEmployeeList() {
        if (employeeList == null) {
            employeeList = new ArrayList<>();
        }
        return employeeList;
    }

    public static class Converter implements EntityConverter<MappableDepartment> {

        @Override
        public Class<MappableDepartment> getEntityType() {
            return MappableDepartment.class;
        }

        @Override
        public Document toDocument(MappableDepartment entity, NitriteMapper nitriteMapper) {
            Document document = Document.createDocument();

            document.put("name", entity.getName());
            List<Document> employees = new ArrayList<>();
            for (MappableEmployee employee : entity.getEmployeeList()) {
                employees.add((Document) nitriteMapper.tryConvert(employee, Document.class));
            }
            document.put("employeeList", employees);

            return document;
        }

        @Override
        public MappableDepartment fromDocument(Document document, NitriteMapper nitriteMapper) {
            MappableDepartment entity = new MappableDepartment();
            if (document != null) {
                entity.setName((String) document.get("name"));
                for (Document doc : (List<Document>) document.get("employeeList")) {
                    MappableEmployee me = (MappableEmployee) nitriteMapper.tryConvert(doc, MappableEmployee.class);
                    entity.getEmployeeList().add(me);
                }
            }
            return entity;
        }
    }
}
