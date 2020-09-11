package org.dizitart.no2.store;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.exceptions.SecurityException;

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
 * @author Anindya Chatterjee
 */
@Slf4j
public class StoreSecurity {
    private static final Random random = new SecureRandom();

    private StoreSecurity() {}

    public static void authenticate(NitriteStore<?> store, String username, String password, boolean existing) {
        /*
        * 1. Proper user password create
        * 2. Proper user password open
        * 3. Blank user password for secured store
        * 4. Wrong user password for secured store
        * 5. User Password for un-secured store
        * */

        if (!isNullOrEmpty(password) && !isNullOrEmpty(username)) {
            /*
            * 1. create new
            * 2. open existing correct
            * 3. open existing incorrect
            * 4. open un-secured existing [how do I know if db already existing and user/pass not required?]
            * */

            if (!existing) {
                byte[] salt = getNextSalt();
                byte[] hash = hash(password.toCharArray(), salt);
                UserCredential userCredential = new UserCredential();
                userCredential.setPasswordHash(hash);
                userCredential.setPasswordSalt(salt);

                NitriteMap<String, UserCredential> userMap = store.openMap(USER_MAP, String.class, UserCredential.class);
                userMap.put(username, userCredential);
            } else {
                NitriteMap<String, UserCredential> userMap = store.openMap(USER_MAP, String.class, UserCredential.class);
                UserCredential userCredential = userMap.get(username);

                if (userCredential != null) {
                    byte[] salt = userCredential.getPasswordSalt();
                    byte[] expectedHash = userCredential.getPasswordHash();

                    if (!isExpectedPassword(password.toCharArray(), salt, expectedHash)) {
                        throw new SecurityException("username or password is invalid");
                    }
                } else {
                    throw new SecurityException("username or password is invalid");
                }
            }
        } else if (existing) {
            /*
            * 1. create new un-secured
            * 2. open un-secured
            * */
            if (store.hasMap(USER_MAP)) {
                throw new SecurityException("username or password is invalid");
            }
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
}
