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

package org.dizitart.no2.common.processors;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.crypto.AESEncryptor;
import org.dizitart.no2.common.crypto.Encryptor;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.exceptions.NitriteIOException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A string field encryption processor. It encrypts the field value
 * of type {@link String} in a nitrite document using the provided {@link Encryptor}.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
@Slf4j
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
     * Adds fields for encryption.
     *
     * @param fields the fields
     */
    public void addFields(String... fields){
        this.fields.addAll(Arrays.asList(fields));
    }

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
            throw new NitriteIOException("failed to process document before write", e);
        }
    }

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
            throw new NitriteIOException("failed to process document after read", e);
        }
    }
}
