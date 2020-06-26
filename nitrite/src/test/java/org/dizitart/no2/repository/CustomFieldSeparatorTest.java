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

package org.dizitart.no2.repository;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.repository.annotations.Id;
import org.dizitart.no2.repository.annotations.Index;
import org.dizitart.no2.repository.annotations.Indices;
import org.dizitart.no2.mapper.Mappable;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.repository.data.Company;
import org.dizitart.no2.repository.data.Note;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.Date;

import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee
 */
public class CustomFieldSeparatorTest {
    private ObjectRepository<EmployeeForCustomSeparator> repository;

    @Before
    public void setUp() {
        Nitrite db = NitriteBuilder.get()
            .fieldSeparator(":")
            .openOrCreate();
        repository = db.getRepository(EmployeeForCustomSeparator.class);
    }

    @After
    public void reset() {
        NitriteConfig.create().fieldSeparator(".");
    }

    @Test
    public void testFieldSeparator() {
        assertEquals(NitriteConfig.getFieldSeparator(), ":");
    }

    @Test
    public void testFindByEmbeddedField() {
        EmployeeForCustomSeparator employee = new EmployeeForCustomSeparator();
        employee.setCompany(new Company());
        employee.setEmployeeNote(new Note());

        employee.setEmpId(123L);
        employee.setJoinDate(new Date());
        employee.setBlob(new byte[0]);
        employee.setAddress("Dummy address");

        employee.getCompany().setCompanyId(987L);
        employee.getCompany().setCompanyName("Dummy Company");
        employee.getCompany().setDateCreated(new Date());

        employee.getEmployeeNote().setNoteId(567L);
        employee.getEmployeeNote().setText("Dummy Note");

        repository.insert(employee);

        assertEquals(repository.find(where("employeeNote.text").eq("Dummy Note")).size(), 0);
        assertEquals(repository.find(where("employeeNote:text").text("Dummy Note")).size(), 1);

        assertEquals(repository.find(where("company.companyName").eq("Dummy Company")).size(), 0);
        assertEquals(repository.find(where("company:companyName").eq("Dummy Company")).size(), 1);
    }

    @ToString
    @EqualsAndHashCode
    @Indices({
        @Index(value = "joinDate", type = IndexType.NonUnique),
        @Index(value = "address", type = IndexType.Fulltext),
        @Index(value = "employeeNote:text", type = IndexType.Fulltext)
    })
    public static class EmployeeForCustomSeparator implements Serializable, Mappable {
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
        private Company company;

        @Getter
        @Setter
        private byte[] blob;

        @Getter
        @Setter
        private Note employeeNote;

        EmployeeForCustomSeparator() {
        }

        public EmployeeForCustomSeparator(EmployeeForCustomSeparator copy) {
            empId = copy.empId;
            joinDate = copy.joinDate;
            address = copy.address;
            company = copy.company;
            blob = copy.blob;
            employeeNote = copy.employeeNote;
        }

        @Override
        public Document write(NitriteMapper mapper) {
            return Document.createDocument().put("empId", empId)
                .put("joinDate", joinDate)
                .put("address", address)
                .put("blob", blob)
                .put("company", company.write(mapper))
                .put("employeeNote", employeeNote.write(mapper));
        }

        @Override
        public void read(NitriteMapper mapper, Document document) {
            empId = document.get("empId", Long.class);
            joinDate = document.get("joinDate", Date.class);
            address = document.get("address", String.class);
            blob = document.get("blob", byte[].class);
            employeeNote = new Note();
            Document doc = document.get("employeeNote", Document.class);
            employeeNote.read(mapper, doc);
            company = new Company();
            doc = document.get("company", Document.class);
            company.read(mapper, doc);
        }
    }

}
