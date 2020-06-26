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

import java.util.Date;

/**
 * @author Anindya Chatterjee
 */
@Data
@ToString
public class Employee {
    private String empId;
    private String name;
    private Date joiningDate;
    private Employee boss;

    public static TypeConverter<Employee> getConverter() {
        return new TypeConverter<>(
            Employee.class,
            (source, mapper) -> {
                if (source != null) {
                    Document document = Document.createDocument("empId", source.getEmpId())
                        .put("name", source.getName())
                        .put("joiningDate", source.getJoiningDate());

                    if (source.getBoss() != null) {
                        document.put("boss", mapper.convert(source.getBoss(), Document.class));
                    }
                    return document;
                }
                return null;
            },
            (source, mapper) -> {
                Employee employee = new Employee();
                employee.setEmpId(source.get("empId", String.class));
                employee.setName(source.get("name", String.class));
                employee.setJoiningDate(source.get("joiningDate", Date.class));

                Document bossDoc = source.get("boss", Document.class);
                if (bossDoc != null) {
                    Employee boss = mapper.convert(bossDoc, Employee.class);
                    employee.setBoss(boss);
                }
                return employee;
            }
        );
    }
}
