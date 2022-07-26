/*
 * Copyright 2004-2019 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.dizitart.no2.mvstore.compat.v1.mvstore.util;

import java.util.UUID;

public final class Bits {
    public static int compareNotNull(char[] var0, char[] var1) {
        if (var0 == var1) {
            return 0;
        } else {
            int var2 = Math.min(var0.length, var1.length);

            for(int var3 = 0; var3 < var2; ++var3) {
                char var4 = var0[var3];
                char var5 = var1[var3];
                if (var4 != var5) {
                    return var4 > var5 ? 1 : -1;
                }
            }

            return Integer.signum(var0.length - var1.length);
        }
    }

    public static int compareNotNullSigned(byte[] var0, byte[] var1) {
        if (var0 == var1) {
            return 0;
        } else {
            int var2 = Math.min(var0.length, var1.length);

            for(int var3 = 0; var3 < var2; ++var3) {
                byte var4 = var0[var3];
                byte var5 = var1[var3];
                if (var4 != var5) {
                    return var4 > var5 ? 1 : -1;
                }
            }

            return Integer.signum(var0.length - var1.length);
        }
    }

    public static int compareNotNullUnsigned(byte[] var0, byte[] var1) {
        if (var0 == var1) {
            return 0;
        } else {
            int var2 = Math.min(var0.length, var1.length);

            for(int var3 = 0; var3 < var2; ++var3) {
                int var4 = var0[var3] & 255;
                int var5 = var1[var3] & 255;
                if (var4 != var5) {
                    return var4 > var5 ? 1 : -1;
                }
            }

            return Integer.signum(var0.length - var1.length);
        }
    }

    public static int readInt(byte[] var0, int var1) {
        return (var0[var1++] << 24) + ((var0[var1++] & 255) << 16) + ((var0[var1++] & 255) << 8) + (var0[var1] & 255);
    }

    public static int readIntLE(byte[] var0, int var1) {
        return (var0[var1++] & 255) + ((var0[var1++] & 255) << 8) + ((var0[var1++] & 255) << 16) + (var0[var1] << 24);
    }

    public static long readLong(byte[] var0, int var1) {
        return ((long)readInt(var0, var1) << 32) + ((long)readInt(var0, var1 + 4) & 4294967295L);
    }

    public static long readLongLE(byte[] var0, int var1) {
        return ((long)readIntLE(var0, var1) & 4294967295L) + ((long)readIntLE(var0, var1 + 4) << 32);
    }

    public static double readDouble(byte[] var0, int var1) {
        return Double.longBitsToDouble(readLong(var0, var1));
    }

    public static double readDoubleLE(byte[] var0, int var1) {
        return Double.longBitsToDouble(readLongLE(var0, var1));
    }

    public static byte[] uuidToBytes(long var0, long var2) {
        byte[] var4 = new byte[16];

        for(int var5 = 0; var5 < 8; ++var5) {
            var4[var5] = (byte)((int)(var0 >> 8 * (7 - var5) & 255L));
            var4[8 + var5] = (byte)((int)(var2 >> 8 * (7 - var5) & 255L));
        }

        return var4;
    }

    public static byte[] uuidToBytes(UUID var0) {
        return uuidToBytes(var0.getMostSignificantBits(), var0.getLeastSignificantBits());
    }

    public static void writeInt(byte[] var0, int var1, int var2) {
        var0[var1++] = (byte)(var2 >> 24);
        var0[var1++] = (byte)(var2 >> 16);
        var0[var1++] = (byte)(var2 >> 8);
        var0[var1] = (byte)var2;
    }

    public static void writeIntLE(byte[] var0, int var1, int var2) {
        var0[var1++] = (byte)var2;
        var0[var1++] = (byte)(var2 >> 8);
        var0[var1++] = (byte)(var2 >> 16);
        var0[var1] = (byte)(var2 >> 24);
    }

    public static void writeLong(byte[] var0, int var1, long var2) {
        writeInt(var0, var1, (int)(var2 >> 32));
        writeInt(var0, var1 + 4, (int)var2);
    }

    public static void writeLongLE(byte[] var0, int var1, long var2) {
        writeIntLE(var0, var1, (int)var2);
        writeIntLE(var0, var1 + 4, (int)(var2 >> 32));
    }

    public static void writeDouble(byte[] var0, int var1, double var2) {
        writeLong(var0, var1, Double.doubleToRawLongBits(var2));
    }

    public static void writeDoubleLE(byte[] var0, int var1, double var2) {
        writeLongLE(var0, var1, Double.doubleToRawLongBits(var2));
    }

    private Bits() {
    }
}
