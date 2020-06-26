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

package org.dizitart.no2.mapper;

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

    @SuppressWarnings("unchecked")
    public static TypeConverter<Department> getConverter() {
        TypeConverter<Department> converter = new TypeConverter<>();
        converter.setSourceType(Department.class);
        converter.setSourceConverter((source, mapper) -> {
            Document document = Document.createDocument("name", source.name);
            List<Document> list = new ArrayList<>();
            if (source.employeeList != null) {
                for (MappableEmployee employee : source.employeeList) {
                    list.add(mapper.convert(employee, Document.class));
                }
            }
            document.put("employeeList", list);
            return document;
        });
        converter.setTargetConverter((document, mapper) -> {
            Department department = new Department();
            department.name = document.get("name", String.class);
            List<Document> list = document.get("employeeList", List.class);
            if (list != null) {
                department.employeeList = new ArrayList<>();
                for (Document doc : list) {
                    MappableEmployee mappableEmployee = mapper.convert(doc, MappableEmployee.class);
                    department.employeeList.add(mappableEmployee);
                }
            }
            return department;
        });
        return converter;
    }
}
