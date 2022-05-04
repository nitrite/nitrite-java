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

package org.dizitart.no2.integration.collection;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.common.crypto.AESEncryptor;
import org.dizitart.no2.common.crypto.Encryptor;
import org.dizitart.no2.common.processors.Processor;
import org.dizitart.no2.common.processors.StringFieldEncryptionProcessor;
import org.dizitart.no2.exceptions.NitriteSecurityException;
import org.dizitart.no2.store.NitriteMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.dizitart.no2.common.util.Iterables.toList;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee
 */
public class FieldProcessorTest extends BaseCollectionTest {

    private Encryptor encryptor;
    private NitriteCollection collection;
    private Processor cvvProcessor;

    @Before
    public void setUp() {
        super.setUp();

        encryptor = new AESEncryptor("s3k4e8");
        cvvProcessor = new Processor() {
            @Override
            public Document processBeforeWrite(Document document) {
                String cvv = document.get("cvv", String.class);
                String encryptedCvv = encryptor.encrypt(cvv.getBytes(StandardCharsets.UTF_8));
                document.put("cvv", encryptedCvv);
                return document;
            }

            @Override
            public Document processAfterRead(Document document) {
                String encryptedCvv = document.get("cvv", String.class);
                String cvv = encryptor.decrypt(encryptedCvv);
                document.put("cvv", cvv);
                return document;
            }
        };
        StringFieldEncryptionProcessor creditCardProcessor = new StringFieldEncryptionProcessor(encryptor);
        creditCardProcessor.addFields("creditCardNumber");

        collection = db.getCollection("encryption-test");
        collection.addProcessor(creditCardProcessor);

        Document document = Document.createDocument("name", "John Doe")
            .put("creditCardNumber", "5548960345687452")
            .put("cvv", "007")
            .put("expiryDate", new Date());
        collection.insert(document);

        document = Document.createDocument("name", "Jane Doe")
            .put("creditCardNumber", "5500960345687452")
            .put("cvv", "008")
            .put("expiryDate", new Date());
        collection.insert(document);

        cvvProcessor.process(collection);
        collection.addProcessor(cvvProcessor);
    }

    @Test
    public void testFieldEncryptionInNitriteMap() {
        NitriteMap<Object, Document> nitriteMap = collection.getStore().openMap("encryption-test",
            NitriteId.class, Document.class);

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
        Document document = collection.find(where("name").eq("Jane Doe")).firstOrNull();
        assertNotNull(document);

        assertEquals(document.get("creditCardNumber", String.class), "5500960345687452");
        assertEquals(document.get("cvv", String.class), "008");

        document = collection.find(where("name").eq("John Doe")).firstOrNull();
        assertNotNull(document);

        assertEquals(document.get("creditCardNumber", String.class), "5548960345687452");
        assertEquals(document.get("cvv", String.class), "007");
    }

    @Test(expected = NitriteSecurityException.class)
    public void testFailedDecryption() {
        Encryptor wrongEncryptor = new AESEncryptor("secret");

        collection = db.getCollection("encryption-test");
        collection.addProcessor(new Processor() {
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

        Document document = Document.createDocument("name", "John Doe")
            .put("creditCardNumber", "5548960345687452")
            .put("cvv", "007")
            .put("expiryDate", new Date());
        collection.insert(document);

        document = Document.createDocument("name", "Jane Doe")
            .put("creditCardNumber", "5500960345687452")
            .put("cvv", "008")
            .put("expiryDate", new Date());
        collection.insert(document);

        collection.find(where("name").eq("Jane Doe")).firstOrNull();
    }

    @Test
    public void testSearchOnEncryptedField() {
        Document document = collection.find(where("cvv").eq("008")).firstOrNull();
        assertNull(document);
    }

    @Test
    public void testUpdateEncryptedField() {
        Document document = Document.createDocument("name", "John Doe")
            .put("creditCardNumber", "00000000000000")
            .put("cvv", "007")
            .put("expiryDate", new Date());

        WriteResult writeResult = collection.update(where("name").eq("John Doe"), document);
        assertEquals(writeResult.getAffectedCount(), 1);

        document = collection.find(where("name").eq("John Doe")).firstOrNull();
        assertNotNull(document);

        assertEquals(document.get("creditCardNumber", String.class), "00000000000000");
        assertEquals(document.get("cvv", String.class), "007");
    }

    @Test
    public void testIndexOnEncryptedField() {
        collection.createIndex("cvv");
        Document document = collection.find(where("cvv").eq("008")).firstOrNull();
        assertNull(document);
    }
}
