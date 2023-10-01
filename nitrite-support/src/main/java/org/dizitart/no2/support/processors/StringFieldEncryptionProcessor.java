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

package org.dizitart.no2.support.processors;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.processors.Processor;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.support.crypto.AESEncryptor;
import org.dizitart.no2.support.crypto.Encryptor;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A processor class which is responsible for encrypting and
 * decrypting string fields in a Nitrite database document.
 * 
 * @author Anindya Chatterjee
 * @since 4.0
 */
@Slf4j(topic = "nitrite-support")
public class StringFieldEncryptionProcessor implements Processor {
    private final Encryptor encryptor;
    private final List<String> fields;

    /**
     * Instantiates a new {@link StringFieldEncryptionProcessor}.
     *
     * @param password the password
     */
    public StringFieldEncryptionProcessor(String password) {
        this(new AESEncryptor(password));
    }

    /**
     * Instantiates a new {@link StringFieldEncryptionProcessor}.
     *
     * @param encryptor the encryptor
     */
    public StringFieldEncryptionProcessor(Encryptor encryptor) {
        this.encryptor = encryptor;
        this.fields = new ArrayList<>();
    }

    /**
     * Adds one or more field names to the list of fields that should be encrypted.
     *
     * @param fields the names of the fields to be encrypted
     */
    public void addFields(String... fields) {
        this.fields.addAll(Arrays.asList(fields));
    }

    /**
     * Processes the document before writing to the database. Encrypts the values of the specified fields
     * using the provided encryptor.
     *
     * @param document the document to be processed
     * @return a new document with encrypted values for the specified fields
     * @throws NitriteIOException if there is an error while processing the document
     */
    @Override
    public Document processBeforeWrite(Document document) {
        try {
            Document copy = document.clone();
            if (!fields.isEmpty()) {
                for (String field : fields) {
                    String value = copy.get(field, String.class);
                    if (!StringUtils.isNullOrEmpty(value)) {
                        // encrypt
                        value = encryptor.encrypt(value.getBytes(StandardCharsets.UTF_8));

                        // set the value
                        copy.put(field, value);
                    }
                }
            }
            return copy;
        } catch (Exception e) {
            log.error("Error while processing document before write", e);
            throw new NitriteIOException("Failed to process document before write", e);
        }
    }

    /**
     * Processes the document after reading from the database. Decrypts the encrypted fields
     * and returns a new document with decrypted values.
     *
     * @param document the document to be processed
     * @return a new document with decrypted values
     * @throws NitriteIOException if there is an error while processing the document
     */
    @Override
    public Document processAfterRead(Document document) {
        try {
            Document copy = document.clone();
            if (!fields.isEmpty()) {
                for (String field : fields) {
                    String value = copy.get(field, String.class);
                    if (!StringUtils.isNullOrEmpty(value)) {
                        // decrypt
                        value = encryptor.decrypt(value);

                        // set the value
                        copy.put(field, value);
                    }
                }
            }
            return copy;
        } catch (Exception e) {
            log.error("Error while processing document after read", e);
            throw new NitriteIOException("Failed to process document after read", e);
        }
    }
}
