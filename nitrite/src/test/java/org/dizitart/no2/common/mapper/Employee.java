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

    public static class Converter implements EntityConverter<Employee> {

        @Override
        public Class<Employee> getEntityType() {
            return Employee.class;
        }

        @Override
        public Document toDocument(Employee entity, NitriteMapper nitriteMapper) {
            Document document = Document.createDocument("empId", entity.getEmpId())
                .put("name", entity.getName())
                .put("joiningDate", entity.getJoiningDate());

            if (entity.getBoss() != null) {
                document.put("boss", nitriteMapper.convert(entity.getBoss(), Document.class));
            }
            return document;
        }

        @Override
        public Employee fromDocument(Document document, NitriteMapper nitriteMapper) {
            Employee entity = new Employee();
            entity.setEmpId(document.get("empId", String.class));
            entity.setName(document.get("name", String.class));
            entity.setJoiningDate(document.get("joiningDate", Date.class));

            Document bossDoc = document.get("boss", Document.class);
            if (bossDoc != null) {
                Employee boss = nitriteMapper.convert(bossDoc, Employee.class);
                entity.setBoss(boss);
            }
            return entity;
        }
    }
}
