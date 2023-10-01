/*
 * Copyright 2004-2019 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */

package org.dizitart.no2.mvstore.compat.v1.mvstore.compress;

import java.nio.ByteBuffer;

public final class CompressLZF implements Compressor {
    private int[] cachedHashTable;

    public CompressLZF() {
    }

    public void setOptions(String var1) {
    }

    private static int first(byte[] var0, int var1) {
        return var0[var1] << 8 | var0[var1 + 1] & 255;
    }

    private static int first(ByteBuffer var0, int var1) {
        return var0.get(var1) << 8 | var0.get(var1 + 1) & 255;
    }

    private static int next(int var0, byte[] var1, int var2) {
        return var0 << 8 | var1[var2 + 2] & 255;
    }

    private static int next(int var0, ByteBuffer var1, int var2) {
        return var0 << 8 | var1.get(var2 + 2) & 255;
    }

    private static int hash(int var0) {
        return var0 * 2777 >> 9 & 16383;
    }

    public int compress(byte[] var1, int var2, byte[] var3, int var4) {
        int var5 = 0;
        if (this.cachedHashTable == null) {
            this.cachedHashTable = new int[16384];
        }

        int[] var6 = this.cachedHashTable;
        int var7 = 0;
        ++var4;
        int var8 = first((byte[])var1, 0);

        while(true) {
            while(var5 < var2 - 4) {
                byte var9 = var1[var5 + 2];
                var8 = (var8 << 8) + (var9 & 255);
                int var10 = hash(var8);
                int var11 = var6[var10];
                var6[var10] = var5;
                if (var11 < var5 && var11 > 0 && (var10 = var5 - var11 - 1) < 8192 && var1[var11 + 2] == var9 && var1[var11 + 1] == (byte)(var8 >> 8) && var1[var11] == (byte)(var8 >> 16)) {
                    int var12 = var2 - var5 - 2;
                    if (var12 > 264) {
                        var12 = 264;
                    }

                    if (var7 == 0) {
                        --var4;
                    } else {
                        var3[var4 - var7 - 1] = (byte)(var7 - 1);
                        var7 = 0;
                    }

                    int var13;
                    for(var13 = 3; var13 < var12 && var1[var11 + var13] == var1[var5 + var13]; ++var13) {
                    }

                    var13 -= 2;
                    if (var13 < 7) {
                        var3[var4++] = (byte)((var10 >> 8) + (var13 << 5));
                    } else {
                        var3[var4++] = (byte)((var10 >> 8) + 224);
                        var3[var4++] = (byte)(var13 - 7);
                    }

                    var3[var4++] = (byte)var10;
                    ++var4;
                    var5 += var13;
                    var8 = first(var1, var5);
                    var8 = next(var8, var1, var5);
                    var6[hash(var8)] = var5++;
                    var8 = next(var8, var1, var5);
                    var6[hash(var8)] = var5++;
                } else {
                    var3[var4++] = var1[var5++];
                    ++var7;
                    if (var7 == 32) {
                        var3[var4 - var7 - 1] = (byte)(var7 - 1);
                        var7 = 0;
                        ++var4;
                    }
                }
            }

            while(var5 < var2) {
                var3[var4++] = var1[var5++];
                ++var7;
                if (var7 == 32) {
                    var3[var4 - var7 - 1] = (byte)(var7 - 1);
                    var7 = 0;
                    ++var4;
                }
            }

            var3[var4 - var7 - 1] = (byte)(var7 - 1);
            if (var7 == 0) {
                --var4;
            }

            return var4;
        }
    }

    public int compress(ByteBuffer var1, int var2, byte[] var3, int var4) {
        int var5 = var1.capacity() - var2;
        if (this.cachedHashTable == null) {
            this.cachedHashTable = new int[16384];
        }

        int[] var6 = this.cachedHashTable;
        int var7 = 0;
        ++var4;
        int var8 = first((ByteBuffer)var1, 0);

        while(true) {
            while(var2 < var5 - 4) {
                byte var9 = var1.get(var2 + 2);
                var8 = (var8 << 8) + (var9 & 255);
                int var10 = hash(var8);
                int var11 = var6[var10];
                var6[var10] = var2;
                if (var11 < var2 && var11 > 0 && (var10 = var2 - var11 - 1) < 8192 && var1.get(var11 + 2) == var9 && var1.get(var11 + 1) == (byte)(var8 >> 8) && var1.get(var11) == (byte)(var8 >> 16)) {
                    int var12 = var5 - var2 - 2;
                    if (var12 > 264) {
                        var12 = 264;
                    }

                    if (var7 == 0) {
                        --var4;
                    } else {
                        var3[var4 - var7 - 1] = (byte)(var7 - 1);
                        var7 = 0;
                    }

                    int var13;
                    for(var13 = 3; var13 < var12 && var1.get(var11 + var13) == var1.get(var2 + var13); ++var13) {
                    }

                    var13 -= 2;
                    if (var13 < 7) {
                        var3[var4++] = (byte)((var10 >> 8) + (var13 << 5));
                    } else {
                        var3[var4++] = (byte)((var10 >> 8) + 224);
                        var3[var4++] = (byte)(var13 - 7);
                    }

                    var3[var4++] = (byte)var10;
                    ++var4;
                    var2 += var13;
                    var8 = first(var1, var2);
                    var8 = next(var8, var1, var2);
                    var6[hash(var8)] = var2++;
                    var8 = next(var8, var1, var2);
                    var6[hash(var8)] = var2++;
                } else {
                    var3[var4++] = var1.get(var2++);
                    ++var7;
                    if (var7 == 32) {
                        var3[var4 - var7 - 1] = (byte)(var7 - 1);
                        var7 = 0;
                        ++var4;
                    }
                }
            }

            while(var2 < var5) {
                var3[var4++] = var1.get(var2++);
                ++var7;
                if (var7 == 32) {
                    var3[var4 - var7 - 1] = (byte)(var7 - 1);
                    var7 = 0;
                    ++var4;
                }
            }

            var3[var4 - var7 - 1] = (byte)(var7 - 1);
            if (var7 == 0) {
                --var4;
            }

            return var4;
        }
    }

    public void expand(byte[] var1, int var2, int var3, byte[] var4, int var5, int var6) {
        if (var2 >= 0 && var5 >= 0 && var6 >= 0) {
            do {
                int var7 = var1[var2++] & 255;
                if (var7 < 32) {
                    ++var7;
                    System.arraycopy(var1, var2, var4, var5, var7);
                    var5 += var7;
                    var2 += var7;
                } else {
                    int var8 = var7 >> 5;
                    if (var8 == 7) {
                        var8 += var1[var2++] & 255;
                    }

                    var8 += 2;
                    var7 = -((var7 & 31) << 8) - 1;
                    var7 -= var1[var2++] & 255;
                    var7 += var5;
                    if (var5 + var8 >= var4.length) {
                        throw new ArrayIndexOutOfBoundsException();
                    }

                    for(int var9 = 0; var9 < var8; ++var9) {
                        var4[var5++] = var4[var7++];
                    }
                }
            } while(var5 < var6);

        } else {
            throw new IllegalArgumentException();
        }
    }

    public static void expand(ByteBuffer var0, ByteBuffer var1) {
        do {
            int var2 = var0.get() & 255;
            int var3;
            if (var2 < 32) {
                ++var2;

                for(var3 = 0; var3 < var2; ++var3) {
                    var1.put(var0.get());
                }
            } else {
                var3 = var2 >> 5;
                if (var3 == 7) {
                    var3 += var0.get() & 255;
                }

                var3 += 2;
                var2 = -((var2 & 31) << 8) - 1;
                var2 -= var0.get() & 255;
                var2 += var1.position();

                for(int var4 = 0; var4 < var3; ++var4) {
                    var1.put(var1.get(var2++));
                }
            }
        } while(var1.position() < var1.capacity());

    }

    public int getAlgorithm() {
        return 1;
    }
}
