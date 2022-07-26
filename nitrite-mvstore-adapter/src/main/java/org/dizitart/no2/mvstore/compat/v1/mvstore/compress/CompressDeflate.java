/*
 * Copyright 2004-2019 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */

package org.dizitart.no2.mvstore.compat.v1.mvstore.compress;

import org.h2.message.DbException;

import java.util.StringTokenizer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CompressDeflate implements Compressor {
    private int level = -1;
    private int strategy = 0;

    public CompressDeflate() {
    }

    public void setOptions(String var1) {
        if (var1 != null) {
            try {
                StringTokenizer var2 = new StringTokenizer(var1);

                while(var2.hasMoreElements()) {
                    String var3 = var2.nextToken();
                    if (!"level".equals(var3) && !"l".equals(var3)) {
                        if ("strategy".equals(var3) || "s".equals(var3)) {
                            this.strategy = Integer.parseInt(var2.nextToken());
                        }
                    } else {
                        this.level = Integer.parseInt(var2.nextToken());
                    }

                    Deflater var4 = new Deflater(this.level);
                    var4.setStrategy(this.strategy);
                }

            } catch (Exception var5) {
                throw DbException.get(90102, var1);
            }
        }
    }

    public int compress(byte[] var1, int var2, byte[] var3, int var4) {
        Deflater var5 = new Deflater(this.level);
        var5.setStrategy(this.strategy);
        var5.setInput(var1, 0, var2);
        var5.finish();
        int var6 = var5.deflate(var3, var4, var3.length - var4);
        if (var6 == 0) {
            this.strategy = 0;
            this.level = -1;
            return this.compress(var1, var2, var3, var4);
        } else {
            var5.end();
            return var4 + var6;
        }
    }

    public int getAlgorithm() {
        return 2;
    }

    public void expand(byte[] var1, int var2, int var3, byte[] var4, int var5, int var6) {
        Inflater var7 = new Inflater();
        var7.setInput(var1, var2, var3);
        var7.finished();

        try {
            int var8 = var7.inflate(var4, var5, var6);
            if (var8 != var6) {
                throw new DataFormatException(var8 + " " + var6);
            }
        } catch (DataFormatException var9) {
            throw DbException.get(90104, var9, new String[0]);
        }

        var7.end();
    }
}
