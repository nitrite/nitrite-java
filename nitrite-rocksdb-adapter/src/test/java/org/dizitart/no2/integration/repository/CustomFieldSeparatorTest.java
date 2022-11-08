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

package org.dizitart.no2.integration.repository;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.common.mapper.SimpleDocumentMapper;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.integration.Retry;
import org.dizitart.no2.integration.repository.data.Company;
import org.dizitart.no2.integration.repository.data.Note;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.repository.annotations.Id;
import org.dizitart.no2.repository.annotations.Index;
import org.dizitart.no2.repository.annotations.Indices;
import org.dizitart.no2.rocksdb.RocksDBModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.Serializable;
import java.util.Date;

import static org.dizitart.no2.filters.FluentFilter.where;
import static org.dizitart.no2.integration.TestUtil.deleteDb;
import static org.dizitart.no2.integration.TestUtil.getRandomTempDbFile;
import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee
 */
public class CustomFieldSeparatorTest {
    private final String fileName = getRandomTempDbFile();
    private ObjectRepository<EmployeeForCustomSeparator> repository;
    private Nitrite db;

    @Rule
    public Retry retry = new Retry(3);

    @Before
    public void setUp() {
        RocksDBModule storeModule = RocksDBModule.withConfig()
            .filePath(fileName)
            .build();

        db = Nitrite.builder()
            .loadModule(storeModule)
            .fieldSeparator(":")
            .openOrCreate();

        SimpleDocumentMapper mapper = (SimpleDocumentMapper) db.getConfig().nitriteMapper();
        mapper.registerEntityConverter(new Company.CompanyConverter());
        mapper.registerEntityConverter(new EmployeeForCustomSeparator.EmployeeForCustomSeparatorConverter());
        mapper.registerEntityConverter(new Note.NoteConverter());

        repository = db.getRepository(EmployeeForCustomSeparator.class);
    }

    @After
    public void reset() {
        (new NitriteConfig()).fieldSeparator(".");
        if (db != null && !db.isClosed()) {
            db.close();
			deleteDb(fileName);
        }
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
        @Index(fields = "joinDate", type = IndexType.NON_UNIQUE),
        @Index(fields = "address", type = IndexType.FULL_TEXT),
        @Index(fields = "employeeNote:text", type = IndexType.FULL_TEXT)
    })
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

        public static class EmployeeForCustomSeparatorConverter implements EntityConverter<EmployeeForCustomSeparator> {

            @Override
            public Class<EmployeeForCustomSeparator> getEntityType() {
                return EmployeeForCustomSeparator.class;
            }

            @Override
            public Document toDocument(EmployeeForCustomSeparator entity, NitriteMapper nitriteMapper) {
                return Document.createDocument().put("empId", entity.empId)
                    .put("joinDate", entity.joinDate)
                    .put("address", entity.address)
                    .put("blob", entity.blob)
                    .put("company", nitriteMapper.convert(entity.company, Document.class))
                    .put("employeeNote", nitriteMapper.convert(entity.employeeNote, Document.class));
            }

            @Override
            public EmployeeForCustomSeparator fromDocument(Document document, NitriteMapper nitriteMapper) {
                EmployeeForCustomSeparator entity = new EmployeeForCustomSeparator();

                entity.empId = document.get("empId", Long.class);
                entity.joinDate = document.get("joinDate", Date.class);
                entity.address = document.get("address", String.class);
                entity.blob = document.get("blob", byte[].class);

                Document doc = document.get("employeeNote", Document.class);
                entity.employeeNote = nitriteMapper.convert(doc, Note.class);

                doc = document.get("company", Document.class);
                entity.company = nitriteMapper.convert(doc, Company.class);
                return entity;
            }
        }
    }

}
