package org.dizitart.no2.leveldb;

import java.util.Arrays;

public class BufferUtils {
    private BufferUtils() {}

    public static byte[] concat(byte[] first, byte[] second) {
        return com.google.common.primitives.Bytes.concat(first, second);
    }

    public static byte[] slice(byte[] array, int start, int end) {
        return Arrays.copyOfRange(array, start, end);
    }

    public static byte[] slice(byte[] array, int start) {
        return Arrays.copyOfRange(array, start, array.length);
    }
}
