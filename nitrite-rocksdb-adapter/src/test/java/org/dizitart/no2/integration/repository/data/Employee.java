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

package org.dizitart.no2.integration.repository.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.repository.annotations.Id;
import org.dizitart.no2.repository.annotations.Index;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Anindya Chatterjee.
 */
@ToString
@EqualsAndHashCode
@Index(fields = "joinDate", type = IndexType.NON_UNIQUE)
@Index(fields = "address", type = IndexType.FULL_TEXT)
@Index(fields = "employeeNote.text", type = IndexType.FULL_TEXT)
public class Employee implements Serializable {
    @Id
    @Getter
    @Setter
    private Long empId;

    @Getter
    @Setter
    private Date joinDate;

    @Getter
    @Setter
    private String address;

    @Getter
    @Setter
    private String emailAddress;

    @Getter
    @Setter
    private transient Company company;

    @Getter
    @Setter
    private byte[] blob;

    @Getter
    @Setter
    private Note employeeNote;

    public Employee() {
    }

    public Employee(Employee copy) {
        empId = copy.empId;
        joinDate = copy.joinDate;
        address = copy.address;
        company = copy.company;
        blob = copy.blob;
        employeeNote = copy.employeeNote;
        emailAddress = copy.emailAddress;
    }

    public static class EmployeeConverter implements EntityConverter<Employee> {

        @Override
        public Class<Employee> getEntityType() {
            return Employee.class;
        }

        @Override
        public Document toDocument(Employee entity, NitriteMapper nitriteMapper) {
            return Document.createDocument()
                .put("empId", entity.empId)
                .put("joinDate", entity.joinDate)
                .put("address", entity.address)
                .put("blob", entity.blob)
                .put("emailAddress", entity.emailAddress)
                .put("employeeNote", nitriteMapper.convert(entity.employeeNote, Document.class));
        }

        @Override
        public Employee fromDocument(Document document, NitriteMapper nitriteMapper) {
            Employee entity = new Employee();

            entity.empId = document.get("empId", Long.class);
            entity.joinDate = document.get("joinDate", Date.class);
            entity.address = document.get("address", String.class);
            entity.blob = document.get("blob", byte[].class);
            entity.emailAddress = document.get("emailAddress", String.class);

            if (document.get("employeeNote") != null) {
                Document doc = document.get("employeeNote", Document.class);
                entity.employeeNote = nitriteMapper.convert(doc, Note.class);;
            }
            return entity;
        }
    }
}
