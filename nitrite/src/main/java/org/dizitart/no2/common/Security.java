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

package org.dizitart.no2.common;

import lombok.Data;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.exceptions.SecurityException;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Random;

import static org.dizitart.no2.common.Constants.*;
import static org.dizitart.no2.exceptions.ErrorCodes.SE_HASHING_FAILED;
import static org.dizitart.no2.exceptions.ErrorMessage.*;
import static org.dizitart.no2.util.StringUtils.isNullOrEmpty;

/**
 * Provides functionality to secure nitrite database using username and password.
 *
 * It does not store password in plain text format instead it stores password hash
 * and salt.
 *
 * @author Anindya Chatterjee.
 */
@Slf4j
@UtilityClass
public class Security {
    private static final Random random = new SecureRandom();

    public static MVStore createSecurely(MVStore.Builder builder, String userId, String password) {
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
            if (store != null) {
                store.commit();
            }
        }

        return store;
    }

    public static MVStore openSecurely(MVStore.Builder builder, String userId, String password) {
        MVStore store = builder.open();

        if (!isNullOrEmpty(password) && !isNullOrEmpty(userId)) {
            if (!store.hasMap(USER_MAP)) {
                throw new SecurityException(NO_USER_MAP_FOUND);
            }
            MVMap<String, UserCredential> userMap = store.openMap(USER_MAP);
            UserCredential userCredential = userMap.get(userId);

            if (userCredential != null) {
                byte[] salt = userCredential.getPasswordSalt();
                byte[] expectedHash = userCredential.getPasswordHash();

                if (!isExpectedPassword(password.toCharArray(), salt, expectedHash)) {
                    throw new SecurityException(INVALID_USER_PASSWORD);
                }
            } else {
                throw new SecurityException(NULL_USER_CREDENTIAL);
            }
        } else {
            if (store.hasMap(USER_MAP)) {
                throw new SecurityException(USER_MAP_SHOULD_NOT_EXISTS);
            }
        }

        return store;
    }

    public static boolean validateUserPassword(NitriteStore store, String userId, String password) {
        if (isNullOrEmpty(userId)
                && isNullOrEmpty(password)
                && !store.hasMap(USER_MAP)) {
            return true;
        }

        if (!isNullOrEmpty(userId)) {
            if (isNullOrEmpty(password)) {
                return false;
            }

            NitriteMap<String, UserCredential> userMap = store.openMap(USER_MAP);
            if (userMap.containsKey(userId)) {
                UserCredential credential = userMap.get(userId);

                byte[] salt = credential.getPasswordSalt();
                byte[] expectedHash = credential.getPasswordHash();

                return isExpectedPassword(password.toCharArray(), salt, expectedHash);
            }
        }

        return false;
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
            throw new SecurityException(errorMessage("Error while hashing a password: "
                            + e.getMessage(), SE_HASHING_FAILED));
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

    @Data
    private static class UserCredential implements Serializable {
        private byte[] passwordHash;
        private byte[] passwordSalt;
    }
}
