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

package org.dizitart.no2.support.crypto;

/**
 * Represents a symmetric key string encryptor.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
public interface Encryptor {
    /**
     * Returns a base64 encoded encrypted string.
     *
     * @param plainText the plain text
     * @return the encrypted string
     */
    String encrypt(byte[] plainText);

    /**
     * Returns the decrypted string, encoded by this encryptor.
     *
     * @param encryptedText the encrypted text
     * @return the string
     */
    String decrypt(String encryptedText);
}
