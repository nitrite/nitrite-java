/*
 * Copyright 2004-2019 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */

package org.dizitart.no2.mvstore.compat.v1.mvstore.util;


import org.dizitart.no2.mvstore.compat.v1.mvstore.fs.FileUtils;
import org.h2.util.Utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SortedProperties extends Properties {
    private static final long serialVersionUID = 1L;

    public SortedProperties() {
    }

    public synchronized Enumeration<Object> keys() {
        Vector var1 = new Vector<>();
        Iterator var2 = this.keySet().iterator();

        while(var2.hasNext()) {
            Object var3 = var2.next();
            var1.add(var3.toString());
        }

        Collections.sort(var1);
        return (new Vector(var1)).elements();
    }

    public static boolean getBooleanProperty(Properties var0, String var1, boolean var2) {
        try {
            return Utils.parseBoolean(var0.getProperty(var1, (String)null), var2, true);
        } catch (IllegalArgumentException var4) {
            var4.printStackTrace();
            return var2;
        }
    }

    public static int getIntProperty(Properties var0, String var1, int var2) {
        String var3 = var0.getProperty(var1, Integer.toString(var2));

        try {
            return Integer.decode(var3);
        } catch (Exception var5) {
            var5.printStackTrace();
            return var2;
        }
    }

    public static String getStringProperty(Properties var0, String var1, String var2) {
        return var0.getProperty(var1, var2);
    }

    public static synchronized SortedProperties loadProperties(String var0) throws IOException {
        SortedProperties var1 = new SortedProperties();
        if (FileUtils.exists(var0)) {
            InputStream var2 = FileUtils.newInputStream(var0);
            Throwable var3 = null;

            try {
                var1.load(var2);
            } catch (Throwable var12) {
                var3 = var12;
                throw var12;
            } finally {
                if (var2 != null) {
                    if (var3 != null) {
                        try {
                            var2.close();
                        } catch (Throwable var11) {
                            var3.addSuppressed(var11);
                        }
                    } else {
                        var2.close();
                    }
                }

            }
        }

        return var1;
    }

    public synchronized void store(String var1) throws IOException {
        ByteArrayOutputStream var2 = new ByteArrayOutputStream();
        this.store(var2, (String)null);
        ByteArrayInputStream var3 = new ByteArrayInputStream(var2.toByteArray());
        InputStreamReader var4 = new InputStreamReader(var3, StandardCharsets.ISO_8859_1);
        LineNumberReader var5 = new LineNumberReader(var4);

        OutputStreamWriter var6;
        try {
            var6 = new OutputStreamWriter(FileUtils.newOutputStream(var1, false));
        } catch (Exception var18) {
            throw new IOException(var18.toString(), var18);
        }

        PrintWriter var7 = new PrintWriter(new BufferedWriter(var6));
        Throwable var8 = null;

        try {
            while(true) {
                String var9 = var5.readLine();
                if (var9 == null) {
                    return;
                }

                if (!var9.startsWith("#")) {
                    var7.print(var9 + "\n");
                }
            }
        } catch (Throwable var19) {
            var8 = var19;
            throw var19;
        } finally {
            if (var7 != null) {
                if (var8 != null) {
                    try {
                        var7.close();
                    } catch (Throwable var17) {
                        var8.addSuppressed(var17);
                    }
                } else {
                    var7.close();
                }
            }

        }
    }

    public synchronized String toLines() {
        StringBuilder var1 = new StringBuilder();
        Iterator var2 = (new TreeMap(this)).entrySet().iterator();

        while(var2.hasNext()) {
            Map.Entry var3 = (Map.Entry)var2.next();
            var1.append(var3.getKey()).append('=').append(var3.getValue()).append('\n');
        }

        return var1.toString();
    }

    public static SortedProperties fromLines(String var0) {
        SortedProperties var1 = new SortedProperties();
        String[] var2 = StringUtils.arraySplit(var0, '\n', true);
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String var5 = var2[var4];
            int var6 = var5.indexOf(61);
            if (var6 > 0) {
                var1.put(var5.substring(0, var6), var5.substring(var6 + 1));
            }
        }

        return var1;
    }
}
