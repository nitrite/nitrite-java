/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2;

import org.dizitart.no2.collection.IndexOptions;
import org.dizitart.no2.collection.IndexType;
import org.dizitart.no2.collection.objects.ObjectRepository;
import org.dizitart.no2.index.annotations.Id;
import org.junit.Test;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Anindya Chatterjee
 */
public class NitriteStressTest {
    private PodamFactory podamFactory = new PodamFactoryImpl();

    private static final int TEST_SET_COUNT = 15000;
    private Nitrite database;
    private ObjectRepository<TestDto> testRepository;

    @Test
    public void stressTest() {
        database = Nitrite.builder().openOrCreate();
        testRepository = database.getRepository(TestDto.class);
        testRepository.createIndex("lastName", IndexOptions.indexOptions(IndexType.Fulltext));
        testRepository.createIndex("birthDate", IndexOptions.indexOptions(IndexType.NonUnique));

        int counter = 0;
        try {
            for (TestDto testDto : createTestSet()) {
                testRepository.insert(testDto);
                counter++;
            }
        } catch (Throwable t) {
            System.err.println("Crashed after " + counter + " records");
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

    public class TestDto {

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


        public String getStudentNumber() {
            return this.studentNumber;
        }


        public void setStudentNumber(String value) {
            this.studentNumber = value;
        }


        public String getLastName() {
            return this.lastName;
        }


        public void setLastName(String value) {
            this.lastName = value;
        }


        public String getPrefixes() {
            return this.prefixes;
        }


        public void setPrefixes(String value) {
            this.prefixes = value;
        }


        public String getInitials() {
            return this.initials;
        }


        public void setInitials(String value) {
            this.initials = value;
        }


        public String getFirstNames() {
            return this.firstNames;
        }


        public void setFirstNames(String value) {
            this.firstNames = value;
        }


        public String getNickName() {
            return this.nickName;
        }


        public void setNickName(String value) {
            this.nickName = value;
        }


        public String getBirthDate() {
            return this.birthDate;
        }


        public void setBirthDate(String value) {
            this.birthDate = value;
        }
    }
}
