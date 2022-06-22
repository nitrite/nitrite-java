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

package org.dizitart.no2.common.crypto;

import org.dizitart.no2.common.util.Base64;
import org.dizitart.no2.common.util.CryptoUtils;
import org.dizitart.no2.common.util.SecureString;
import org.dizitart.no2.exceptions.NitriteSecurityException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A password based AES string encryption utility.
 *
 * <p>
 * NOTE: This is a derivative work of https://mkyong.com/java/java-symmetric-key-cryptography-example/
 * </p>
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
public class AESEncryptor implements Encryptor {
    private final String encryptAlgo;
    private final int tagLengthBit;
    private final int ivLengthByte;
    private final int saltLengthByte;
    private final Charset UTF_8 = StandardCharsets.UTF_8;

    private final SecureString password;

    /**
     * Instantiates a new {@link AESEncryptor} with these default values
     *     <ul>
     *         <li>Encryption Algo - AES/GCM/NoPadding</li>
     *         <li>Tag Length (bit) - 128</li>
     *         <li>IV Length (byte) - 12</li>
     *         <li>Salt Length (byte) - 16</li>
     *     </ul>
     *
     * @param password the password
     */
    public AESEncryptor(String password) {
        this(password, "AES/GCM/NoPadding", 128, 12, 16);
    }

    /**
     * Instantiates a new {@link AESEncryptor}.
     *
     * @param password       the password
     * @param encryptionAlgo the encryption algo
     * @param tagLengthBit   the tag length bit
     * @param ivLengthByte   the iv length byte
     * @param saltLengthByte the salt length byte
     */
    public AESEncryptor(String password, String encryptionAlgo,
                        Integer tagLengthBit, Integer ivLengthByte,
                        Integer saltLengthByte) {
        this.password = new SecureString(password);
        this.encryptAlgo = encryptionAlgo;
        this.tagLengthBit = tagLengthBit;
        this.ivLengthByte = ivLengthByte;
        this.saltLengthByte = saltLengthByte;
    }

    /**
     * Returns a base64 encoded AES encrypted string.
     *
     * @param plainText the text as byte array
     * @return the encrypted string
     */
    @Override
    public String encrypt(byte[] plainText) {
        try {
            // 16 bytes salt
            byte[] salt = CryptoUtils.getRandomNonce(saltLengthByte);

            // GCM recommended 12 bytes iv?
            byte[] iv = CryptoUtils.getRandomNonce(ivLengthByte);

            // secret key from password
            SecretKey aesKeyFromPassword = CryptoUtils.getAESKeyFromPassword(password.asString().toCharArray(), salt);

            Cipher cipher = Cipher.getInstance(encryptAlgo);

            // ASE-GCM needs GCMParameterSpec
            cipher.init(Cipher.ENCRYPT_MODE, aesKeyFromPassword, new GCMParameterSpec(tagLengthBit, iv));

            byte[] cipherText = cipher.doFinal(plainText);

            // prefix IV and Salt to cipher text
            byte[] cipherTextWithIvSalt = ByteBuffer.allocate(iv.length + salt.length + cipherText.length)
                .put(iv)
                .put(salt)
                .put(cipherText)
                .array();

            // string representation, base64, send this string to other for decryption.
            return Base64.encodeToString(cipherTextWithIvSalt, Base64.URL_SAFE);
        } catch (Exception e) {
            throw new NitriteSecurityException("Failed to encrypt data", e);
        }
    }

    /**
     * Returns the decrypted string encoded by AES.
     *
     * <p>
     *     NOTE: The same password, salt and iv are needed to decrypt it.
     * </p>
     * @param encryptedText the encrypted text
     * @return the plain text decrypted string
     */
    @Override
    public String decrypt(String encryptedText) {
        try {
            byte[] decode = Base64.decode(encryptedText.getBytes(UTF_8), Base64.URL_SAFE);

            // get back the iv and salt from the cipher text
            ByteBuffer bb = ByteBuffer.wrap(decode);
            byte[] iv = new byte[ivLengthByte];
            bb.get(iv);

            byte[] salt = new byte[saltLengthByte];
            bb.get(salt);

            byte[] cipherText = new byte[bb.remaining()];
            bb.get(cipherText);

            // get back the aes key from the same password and salt
            SecretKey aesKeyFromPassword = CryptoUtils.getAESKeyFromPassword(password.asString().toCharArray(), salt);
            Cipher cipher = Cipher.getInstance(encryptAlgo);
            cipher.init(Cipher.DECRYPT_MODE, aesKeyFromPassword, new GCMParameterSpec(tagLengthBit, iv));
            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, UTF_8);
        } catch (Exception e) {
            throw new NitriteSecurityException("Failed to decrypt data", e);
        }
    }
}
