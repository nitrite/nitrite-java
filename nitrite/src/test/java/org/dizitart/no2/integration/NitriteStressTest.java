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

package org.dizitart.no2.integration;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.common.mapper.EntityConverterMapper;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.repository.annotations.Id;
import org.junit.Rule;
import org.junit.Test;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.util.ArrayList;
import java.util.List;

import static org.dizitart.no2.integration.TestUtil.createDb;


/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class NitriteStressTest {
    private static final int TEST_SET_COUNT = 15000;
    private final PodamFactory podamFactory = new PodamFactoryImpl();

    @Rule
    public Retry retry = new Retry(3);

    @Test
    public void stressTest() {
        Nitrite database = createDb();
        EntityConverterMapper documentMapper = (EntityConverterMapper) database.getConfig().nitriteMapper();
        documentMapper.registerEntityConverter(new TestDto.Converter());
        ObjectRepository<TestDto> testRepository = database.getRepository(TestDto.class);
        testRepository.createIndex(IndexOptions.indexOptions(IndexType.FULL_TEXT), "lastName");
        testRepository.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "birthDate");

        int counter = 0;
        try {
            for (TestDto testDto : createTestSet()) {
                testRepository.insert(testDto);
                counter++;
            }
        } catch (Throwable t) {
            log.error("Error occurred at " + counter, t);
            throw t;
        }
    }

    private List<TestDto> createTestSet() {
        List<TestDto> testData = new ArrayList<>();
        for (int i = 0; i < TEST_SET_COUNT; i++) {
            TestDto testRecords = podamFactory.manufacturePojo(TestDto.class);
            testData.add(testRecords);
        }
        return testData;
    }

    @Data
    public static class TestDto {

        @XmlElement(
            name = "StudentNumber",
            required = true
        )
        @Id
        protected String studentNumber;

        @XmlElement(
            name = "LastName",
            required = true
        )
        protected String lastName;

        @XmlElement(
            name = "Prefixes"
        )
        protected String prefixes;

        @XmlElement(
            name = "Initials",
            required = true
        )
        protected String initials;

        @XmlElement(
            name = "FirstNames"
        )
        protected String firstNames;
        @XmlElement(
            name = "Nickname"
        )
        protected String nickName;

        @XmlElement(
            name = "BirthDate",
            required = true
        )
        @XmlSchemaType(
            name = "date"
        )
        protected String birthDate;


        public TestDto() {
        }

        public static class Converter implements EntityConverter<TestDto> {

            @Override
            public Class<TestDto> getEntityType() {
                return TestDto.class;
            }

            @Override
            public Document toDocument(TestDto entity, NitriteMapper nitriteMapper) {
                return Document.createDocument()
                    .put("studentNumber", entity.studentNumber)
                    .put("lastName", entity.lastName)
                    .put("prefixes", entity.prefixes)
                    .put("initials", entity.initials)
                    .put("firstNames", entity.firstNames)
                    .put("nickName", entity.nickName)
                    .put("birthDate", entity.birthDate);
            }

            @Override
            public TestDto fromDocument(Document document, NitriteMapper nitriteMapper) {
                TestDto entity = new TestDto();
                entity.studentNumber = document.get("studentNumber", String.class);
                entity.lastName = document.get("lastName", String.class);
                entity.prefixes = document.get("prefixes", String.class);
                entity.initials = document.get("initials", String.class);
                entity.firstNames = document.get("firstNames", String.class);
                entity.nickName = document.get("nickName", String.class);
                entity.birthDate = document.get("birthDate", String.class);
                return entity;
            }
        }
    }
}
