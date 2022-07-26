/*
 * Copyright 2004-2019 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */

package org.dizitart.no2.mvstore.compat.v1.mvstore.compress;

public interface Compressor {
    int NO = 0;
    int LZF = 1;
    int DEFLATE = 2;

    int getAlgorithm();

    int compress(byte[] var1, int var2, byte[] var3, int var4);

    void expand(byte[] var1, int var2, int var3, byte[] var4, int var5, int var6);

    void setOptions(String var1);
}
