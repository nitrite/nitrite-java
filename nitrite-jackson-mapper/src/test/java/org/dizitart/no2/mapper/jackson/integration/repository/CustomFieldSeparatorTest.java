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

package org.dizitart.no2.mapper.jackson.integration.repository;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.mapper.jackson.JacksonMapperModule;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.mapper.jackson.integration.repository.data.Company;
import org.dizitart.no2.mapper.jackson.integration.repository.data.Note;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.repository.annotations.Id;
import org.dizitart.no2.repository.annotations.Index;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.Date;

import static org.dizitart.no2.filters.FluentFilter.where;
import static org.dizitart.no2.mapper.jackson.integration.repository.BaseObjectRepositoryTest.getRandomTempDbFile;
import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee
 */
public class CustomFieldSeparatorTest {
    private Nitrite db;
    private ObjectRepository<EmployeeForCustomSeparator> repository;
    private final String fileName = getRandomTempDbFile();

//    @Rule
    public Retry retry = new Retry(3);

    @Before
    public void setUp() {
        db = Nitrite.builder()
            .loadModule(new MVStoreModule(fileName))
            .loadModule(new JacksonMapperModule())
            .fieldSeparator(":")
            .openOrCreate();
        repository = db.getRepository(EmployeeForCustomSeparator.class);
    }

    @After
    public void reset() {
        (new NitriteConfig()).fieldSeparator(".");
        if (db != null && !db.isClosed()) {
            db.close();
        }

        if (db != null && !db.isClosed()) {
            db.commit();
            db.close();
        }

        TestUtil.deleteDb(fileName);
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
    @Index(fields = "joinDate", type = IndexType.NON_UNIQUE)
    @Index(fields = "address", type = IndexType.FULL_TEXT)
    @Index(fields = "employeeNote:text", type = IndexType.FULL_TEXT)
    public static class EmployeeForCustomSeparator implements Serializable {
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
    }

}
