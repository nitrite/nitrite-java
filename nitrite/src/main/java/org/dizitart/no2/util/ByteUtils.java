package org.dizitart.no2.util;

import lombok.experimental.UtilityClass;

import java.math.BigInteger;

/**
 * Byte utility class.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
@UtilityClass
public class ByteUtils {

    /**
     * Converts a byte array to a long value.
     *
     * @param b the byte array
     * @return the long value
     */
    public static long bytesToLong(byte[] b) {
        BigInteger integer = new BigInteger(b);
        return Math.abs(integer.longValue());
    }
}
