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

import org.dizitart.no2.integration.repository.data.EncryptedPerson;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.common.crypto.AESEncryptor;
import org.dizitart.no2.common.crypto.Encryptor;
import org.dizitart.no2.common.processors.Processor;
import org.dizitart.no2.common.processors.StringFieldEncryptionProcessor;
import org.dizitart.no2.exceptions.NitriteSecurityException;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.store.NitriteMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.dizitart.no2.common.util.Iterables.toList;
import static org.dizitart.no2.common.util.ObjectUtils.findRepositoryName;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee
 */
public class FieldProcessorTest extends BaseObjectRepositoryTest {
    private ObjectRepository<EncryptedPerson> persons;
    private StringFieldEncryptionProcessor fieldProcessor;

    @Before
    public void setUp() {
        super.setUp();
        persons = db.getRepository(EncryptedPerson.class);
        fieldProcessor = new StringFieldEncryptionProcessor("s3k4e8");
        fieldProcessor.addFields("creditCardNumber", "cvv");

        EncryptedPerson person = new EncryptedPerson();
        person.setName("John Doe");
        person.setCreditCardNumber("5548960345687452");
        person.setCvv("007");
        person.setExpiryDate(new Date());

        persons.insert(person);

        // process existing data
        fieldProcessor.process(persons);

        // add for further changes
        persons.addProcessor(fieldProcessor);

        person = new EncryptedPerson();
        person.setName("Jane Doe");
        person.setCreditCardNumber("5500960345687452");
        person.setCvv("008");
        person.setExpiryDate(new Date());
        persons.insert(person);
    }

    @Test
    public void testFieldEncryptionInNitriteMap() {
        NitriteMap<Object, Document> nitriteMap = persons.getDocumentCollection().getStore()
            .openMap(findRepositoryName(EncryptedPerson.class, null), NitriteId.class, Document.class);

        List<Document> documents = toList(nitriteMap.values());
        for (Document document : documents) {
            if (document.get("creditCardNumber", String.class).equalsIgnoreCase("5548960345687452")) {
                Assert.fail("unencrypted secret text found");
            }

            if (document.get("creditCardNumber", String.class).equalsIgnoreCase("5500960345687452")) {
                Assert.fail("unencrypted secret text found");
            }

            if (document.get("cvv", String.class).equalsIgnoreCase("008")) {
                Assert.fail("unencrypted secret text found");
            }

            if (document.get("cvv", String.class).equalsIgnoreCase("007")) {
                Assert.fail("unencrypted secret text found");
            }
        }
    }

    @Test
    public void testSuccessfulDecryption() {
        EncryptedPerson person = persons.find(where("name").eq("Jane Doe")).firstOrNull();
        assertNotNull(person);

        assertEquals(person.getCreditCardNumber(), "5500960345687452");
        assertEquals(person.getCvv(), "008");

        person = persons.find(where("name").eq("John Doe")).firstOrNull();
        assertNotNull(person);

        assertEquals(person.getCreditCardNumber(), "5548960345687452");
        assertEquals(person.getCvv(), "007");
    }

    @Test(expected = NitriteSecurityException.class)
    public void testFailedDecryption() {
        ObjectRepository<EncryptedPerson> testPersons = db.getRepository(EncryptedPerson.class, "test");

        Encryptor encryptor = new AESEncryptor("secret");
        Encryptor wrongEncryptor = new AESEncryptor("secret", "AES/GCM/NoPadding",
            5, 5, 5);

        testPersons.addProcessor(new Processor() {
            @Override
            public Document processBeforeWrite(Document document) {
                String creditCardNumber = document.get("creditCardNumber", String.class);
                String encryptedCreditCardNumber = encryptor.encrypt(creditCardNumber.getBytes(StandardCharsets.UTF_8));
                document.put("creditCardNumber", encryptedCreditCardNumber);
                return document;
            }

            @Override
            public Document processAfterRead(Document document) {
                String encryptedCreditCardNumber = document.get("creditCardNumber", String.class);
                String creditCardNumber = wrongEncryptor.decrypt(encryptedCreditCardNumber);
                document.put("creditCardNumber", creditCardNumber);
                return document;
            }
        });

        EncryptedPerson person = new EncryptedPerson();
        person.setName("John Doe");
        person.setCreditCardNumber("5548960345687452");
        person.setCvv("007");
        person.setExpiryDate(new Date());

        testPersons.insert(person);

        person = new EncryptedPerson();
        person.setName("Jane Doe");
        person.setCreditCardNumber("5500960345687452");
        person.setCvv("008");
        person.setExpiryDate(new Date());
        testPersons.insert(person);

        testPersons.find(where("name").eq("Jane Doe")).firstOrNull();
    }

    @Test
    public void testSearchOnEncryptedField() {
        EncryptedPerson person = persons.find(where("cvv").eq("008")).firstOrNull();
        assertNull(person);
    }

    @Test
    public void testUpdateEncryptedField() {
        EncryptedPerson person = new EncryptedPerson();
        person.setName("John Doe");
        person.setCreditCardNumber("00000000000000");
        person.setCvv("007");
        person.setExpiryDate(new Date());

        WriteResult writeResult = persons.update(where("name").eq("John Doe"), person);
        assertEquals(writeResult.getAffectedCount(), 1);

        person = persons.find(where("name").eq("John Doe")).firstOrNull();
        assertNotNull(person);

        assertEquals(person.getCreditCardNumber(), "00000000000000");
        assertEquals(person.getCvv(), "007");
    }

    @Test
    public void testIndexOnEncryptedField() {
        persons.createIndex("cvv");
        EncryptedPerson person = persons.find(where("cvv").eq("008")).firstOrNull();
        assertNull(person);
    }
}
