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
    private static final String ENCRYPT_ALGO = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;
    private static final int SALT_LENGTH_BYTE = 16;
    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    private final SecureString password;

    /**
     * Instantiates a new Encryptor.
     *
     * @param password the password
     */
    public AESEncryptor(String password) {
        this.password = new SecureString(password);
    }

    /**
     * Returns a base64 encoded AES encrypted string.
     *
     * @param plainText the text as byte array
     * @return the encrypted string
     * @throws Exception the exception
     */
    @Override
    public String encrypt(byte[] plainText) throws Exception {
        // 16 bytes salt
        byte[] salt = CryptoUtils.getRandomNonce(SALT_LENGTH_BYTE);

        // GCM recommended 12 bytes iv?
        byte[] iv = CryptoUtils.getRandomNonce(IV_LENGTH_BYTE);

        // secret key from password
        SecretKey aesKeyFromPassword = CryptoUtils.getAESKeyFromPassword(password.asString().toCharArray(), salt);

        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);

        // ASE-GCM needs GCMParameterSpec
        cipher.init(Cipher.ENCRYPT_MODE, aesKeyFromPassword, new GCMParameterSpec(TAG_LENGTH_BIT, iv));

        byte[] cipherText = cipher.doFinal(plainText);

        // prefix IV and Salt to cipher text
        byte[] cipherTextWithIvSalt = ByteBuffer.allocate(iv.length + salt.length + cipherText.length)
            .put(iv)
            .put(salt)
            .put(cipherText)
            .array();

        // string representation, base64, send this string to other for decryption.
        return Base64.encodeToString(cipherTextWithIvSalt, Base64.URL_SAFE);
    }

    /**
     * Returns the decrypted string encoded by AES.
     *
     * <p>
     *     NOTE: The same password, salt and iv are needed to decrypt it.
     * </p>
     * @param encryptedText the encrypted text
     * @return the plain text decrypted string
     * @throws Exception the exception
     */
    @Override
    public String decrypt(String encryptedText) throws Exception {
        byte[] decode = Base64.decode(encryptedText.getBytes(UTF_8), Base64.URL_SAFE);

        // get back the iv and salt from the cipher text
        ByteBuffer bb = ByteBuffer.wrap(decode);
        byte[] iv = new byte[IV_LENGTH_BYTE];
        bb.get(iv);

        byte[] salt = new byte[SALT_LENGTH_BYTE];
        bb.get(salt);

        byte[] cipherText = new byte[bb.remaining()];
        bb.get(cipherText);

        // get back the aes key from the same password and salt
        SecretKey aesKeyFromPassword = CryptoUtils.getAESKeyFromPassword(password.asString().toCharArray(), salt);
        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
        cipher.init(Cipher.DECRYPT_MODE, aesKeyFromPassword, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
        byte[] plainText = cipher.doFinal(cipherText);
        return new String(plainText, UTF_8);
    }
}
