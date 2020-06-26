/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.store;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.exceptions.SecurityException;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Random;

import static org.dizitart.no2.common.Constants.*;
import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;

/**
 * Provides functionality to secure nitrite database using username and password.
 * <p>
 * It does not store password in plain text format instead it stores password hash
 * and salt.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
@Slf4j
class Security {
    private static final Random random = new SecureRandom();

    private Security() { }

    static MVStore createSecurely(MVStore.Builder builder, String userId, String password) {
        MVStore store = builder.open();

        try {
            if (!isNullOrEmpty(password) && !isNullOrEmpty(userId)) {
                byte[] salt = getNextSalt();
                byte[] hash = hash(password.toCharArray(), salt);
                UserCredential userCredential = new UserCredential();
                userCredential.setPasswordHash(hash);
                userCredential.setPasswordSalt(salt);

                MVMap<String, UserCredential> userMap = store.openMap(USER_MAP);
                userMap.put(userId, userCredential);
            }
        } finally {
            store.commit();
        }

        return store;
    }

    static MVStore openSecurely(MVStore.Builder builder, String userId, String password) {
        MVStore store = builder.open();
        boolean success = false;

        try {
            if (!isNullOrEmpty(password) && !isNullOrEmpty(userId)) {
                if (!store.hasMap(USER_MAP)) {
                    throw new SecurityException("no user map found in the database");
                }
                MVMap<String, UserCredential> userMap = store.openMap(USER_MAP);
                UserCredential userCredential = userMap.get(userId);

                if (userCredential != null) {
                    byte[] salt = userCredential.getPasswordSalt();
                    byte[] expectedHash = userCredential.getPasswordHash();

                    if (!isExpectedPassword(password.toCharArray(), salt, expectedHash)) {
                        throw new SecurityException("username or password is invalid");
                    }
                } else {
                    throw new SecurityException("no username or password is stored in the database");
                }
            } else {
                if (store.hasMap(USER_MAP)) {
                    throw new SecurityException("user map found unexpectedly");
                }
            }

            success = true;
            return store;
        } finally {
            if (!success) {
                store.close();
            }
        }
    }

    private static byte[] getNextSalt() {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    private static byte[] hash(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, HASH_ITERATIONS, HASH_KEY_LENGTH);
        Arrays.fill(password, Character.MIN_VALUE);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Error while hashing password", e);
            throw new SecurityException("error while hashing a password: "
                + e.getMessage());
        } finally {
            spec.clearPassword();
        }
    }

    private static boolean isExpectedPassword(char[] password, byte[] salt, byte[] expectedHash) {
        byte[] pwdHash = hash(password, salt);
        Arrays.fill(password, Character.MIN_VALUE);
        if (pwdHash.length != expectedHash.length) return false;
        for (int i = 0; i < pwdHash.length; i++) {
            if (pwdHash[i] != expectedHash[i]) return false;
        }
        return true;
    }

}
