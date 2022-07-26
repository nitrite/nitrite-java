/*
 * Copyright 2004-2019 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */

package org.dizitart.no2.mvstore.compat.v1.mvstore.util;

import org.h2.engine.SysProperties;
import org.h2.message.DbException;
import org.h2.util.Utils;

import java.io.ByteArrayOutputStream;
import java.lang.ref.SoftReference;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class StringUtils {
    private static SoftReference<String[]> softCache;
    private static long softCacheCreatedNs;
    private static final char[] HEX = "0123456789abcdef".toCharArray();
    private static final int[] HEX_DECODE = new int[103];
    private static final int TO_UPPER_CACHE_LENGTH = 2048;
    private static final int TO_UPPER_CACHE_MAX_ENTRY_LENGTH = 64;
    private static final String[][] TO_UPPER_CACHE = new String[2048][];

    private StringUtils() {
    }

    private static String[] getCache() {
        String[] var0;
        if (softCache != null) {
            var0 = (String[])softCache.get();
            if (var0 != null) {
                return var0;
            }
        }

        long var1 = System.nanoTime();
        if (softCacheCreatedNs != 0L && var1 - softCacheCreatedNs < TimeUnit.SECONDS.toNanos(5L)) {
            return null;
        } else {
            String[] var3;
            try {
                var0 = new String[SysProperties.OBJECT_CACHE_SIZE];
                softCache = new SoftReference(var0);
                var3 = var0;
            } finally {
                softCacheCreatedNs = System.nanoTime();
            }

            return var3;
        }
    }

    public static String toUpperEnglish(String var0) {
        if (var0.length() > 64) {
            return var0.toUpperCase(Locale.ENGLISH);
        } else {
            int var1 = var0.hashCode() & 2047;
            String[] var2 = TO_UPPER_CACHE[var1];
            if (var2 != null && var2[0].equals(var0)) {
                return var2[1];
            } else {
                String var3 = var0.toUpperCase(Locale.ENGLISH);
                var2 = new String[]{var0, var3};
                TO_UPPER_CACHE[var1] = var2;
                return var3;
            }
        }
    }

    public static String toLowerEnglish(String var0) {
        return var0.toLowerCase(Locale.ENGLISH);
    }

    public static String quoteStringSQL(String var0) {
        return var0 == null ? "NULL" : quoteStringSQL(new StringBuilder(var0.length() + 2), var0).toString();
    }

    public static StringBuilder quoteStringSQL(StringBuilder var0, String var1) {
        if (var1 == null) {
            return var0.append("NULL");
        } else {
            int var2 = var0.length();
            int var3 = var1.length();
            var0.append('\'');

            for(int var4 = 0; var4 < var3; ++var4) {
                char var5 = var1.charAt(var4);
                if (var5 == '\'') {
                    var0.append(var5);
                } else if (var5 < ' ' || var5 > 127) {
                    var0.setLength(var2);
                    var0.append("STRINGDECODE('");
                    javaEncode(var1, var0, true);
                    return var0.append("')");
                }

                var0.append(var5);
            }

            return var0.append('\'');
        }
    }

    public static String javaEncode(String var0) {
        StringBuilder var1 = new StringBuilder(var0.length());
        javaEncode(var0, var1, false);
        return var1.toString();
    }

    public static void javaEncode(String var0, StringBuilder var1, boolean var2) {
        int var3 = var0.length();

        for(int var4 = 0; var4 < var3; ++var4) {
            char var5 = var0.charAt(var4);
            switch (var5) {
                case '\t':
                    var1.append("\\t");
                    break;
                case '\n':
                    var1.append("\\n");
                    break;
                case '\f':
                    var1.append("\\f");
                    break;
                case '\r':
                    var1.append("\\r");
                    break;
                case '"':
                    var1.append("\\\"");
                    break;
                case '\'':
                    if (var2) {
                        var1.append('\'');
                    }

                    var1.append('\'');
                    break;
                case '\\':
                    var1.append("\\\\");
                    break;
                default:
                    if (var5 >= ' ' && var5 < 128) {
                        var1.append(var5);
                    } else {
                        var1.append("\\u").append(HEX[var5 >>> 12]).append(HEX[var5 >>> 8 & 15]).append(HEX[var5 >>> 4 & 15]).append(HEX[var5 & 15]);
                    }
            }
        }

    }

    public static String addAsterisk(String var0, int var1) {
        if (var0 != null) {
            int var2 = var0.length();
            var1 = Math.min(var1, var2);
            var0 = (new StringBuilder(var2 + 3)).append(var0, 0, var1).append("[*]").append(var0, var1, var2).toString();
        }

        return var0;
    }

    private static DbException getFormatException(String var0, int var1) {
        return DbException.get(90095, addAsterisk(var0, var1));
    }

    public static String javaDecode(String var0) {
        int var1 = var0.length();
        StringBuilder var2 = new StringBuilder(var1);

        for(int var3 = 0; var3 < var1; ++var3) {
            char var4 = var0.charAt(var3);
            if (var4 == '\\') {
                if (var3 + 1 >= var0.length()) {
                    throw getFormatException(var0, var3);
                }

                ++var3;
                var4 = var0.charAt(var3);
                switch (var4) {
                    case '"':
                        var2.append('"');
                        break;
                    case '#':
                        var2.append('#');
                        break;
                    case ':':
                        var2.append(':');
                        break;
                    case '=':
                        var2.append('=');
                        break;
                    case '\\':
                        var2.append('\\');
                        break;
                    case 'b':
                        var2.append('\b');
                        break;
                    case 'f':
                        var2.append('\f');
                        break;
                    case 'n':
                        var2.append('\n');
                        break;
                    case 'r':
                        var2.append('\r');
                        break;
                    case 't':
                        var2.append('\t');
                        break;
                    case 'u':
                        try {
                            var4 = (char)Integer.parseInt(var0.substring(var3 + 1, var3 + 5), 16);
                        } catch (NumberFormatException var7) {
                            throw getFormatException(var0, var3);
                        }

                        var3 += 4;
                        var2.append(var4);
                        break;
                    default:
                        if (var4 < '0' || var4 > '9') {
                            throw getFormatException(var0, var3);
                        }

                        try {
                            var4 = (char)Integer.parseInt(var0.substring(var3, var3 + 3), 8);
                        } catch (NumberFormatException var6) {
                            throw getFormatException(var0, var3);
                        }

                        var3 += 2;
                        var2.append(var4);
                }
            } else {
                var2.append(var4);
            }
        }

        return var2.toString();
    }

    public static String quoteJavaString(String var0) {
        if (var0 == null) {
            return "null";
        } else {
            StringBuilder var1 = (new StringBuilder(var0.length() + 2)).append('"');
            javaEncode(var0, var1, false);
            return var1.append('"').toString();
        }
    }

    public static String quoteJavaStringArray(String[] var0) {
        if (var0 == null) {
            return "null";
        } else {
            StringBuilder var1 = new StringBuilder("new String[]{");

            for(int var2 = 0; var2 < var0.length; ++var2) {
                if (var2 > 0) {
                    var1.append(", ");
                }

                var1.append(quoteJavaString(var0[var2]));
            }

            return var1.append('}').toString();
        }
    }

    public static String quoteJavaIntArray(int[] var0) {
        if (var0 == null) {
            return "null";
        } else {
            StringBuilder var1 = new StringBuilder("new int[]{");

            for(int var2 = 0; var2 < var0.length; ++var2) {
                if (var2 > 0) {
                    var1.append(", ");
                }

                var1.append(var0[var2]);
            }

            return var1.append('}').toString();
        }
    }

    public static String unEnclose(String var0) {
        return var0.startsWith("(") && var0.endsWith(")") ? var0.substring(1, var0.length() - 1) : var0;
    }

    public static String urlEncode(String var0) {
        try {
            return URLEncoder.encode(var0, "UTF-8");
        } catch (Exception var2) {
            throw DbException.convert(var2);
        }
    }

    public static String urlDecode(String var0) {
        int var1 = var0.length();
        byte[] var2 = new byte[var1];
        int var3 = 0;

        for(int var4 = 0; var4 < var1; ++var4) {
            char var5 = var0.charAt(var4);
            if (var5 == '+') {
                var2[var3++] = 32;
            } else if (var5 == '%') {
                var2[var3++] = (byte)Integer.parseInt(var0.substring(var4 + 1, var4 + 3), 16);
                var4 += 2;
            } else {
                if (var5 > 127 || var5 < ' ') {
                    throw new IllegalArgumentException("Unexpected char " + var5 + " decoding " + var0);
                }

                var2[var3++] = (byte)var5;
            }
        }

        return new String(var2, 0, var3, StandardCharsets.UTF_8);
    }

    public static String[] arraySplit(String var0, char var1, boolean var2) {
        if (var0 == null) {
            return null;
        } else {
            int var3 = var0.length();
            if (var3 == 0) {
                return new String[0];
            } else {
                ArrayList var4 = Utils.newSmallArrayList();
                StringBuilder var5 = new StringBuilder(var3);

                for(int var6 = 0; var6 < var3; ++var6) {
                    char var7 = var0.charAt(var6);
                    if (var7 == var1) {
                        String var8 = var5.toString();
                        var4.add(var2 ? var8.trim() : var8);
                        var5.setLength(0);
                    } else if (var7 == '\\' && var6 < var3 - 1) {
                        ++var6;
                        var5.append(var0.charAt(var6));
                    } else {
                        var5.append(var7);
                    }
                }

                String var9 = var5.toString();
                var4.add(var2 ? var9.trim() : var9);
                return (String[])var4.toArray(new String[0]);
            }
        }
    }

    public static String arrayCombine(String[] var0, char var1) {
        StringBuilder var2 = new StringBuilder();

        for(int var3 = 0; var3 < var0.length; ++var3) {
            if (var3 > 0) {
                var2.append(var1);
            }

            String var4 = var0[var3];
            if (var4 != null) {
                int var5 = 0;

                for(int var6 = var4.length(); var5 < var6; ++var5) {
                    char var7 = var4.charAt(var5);
                    if (var7 == '\\' || var7 == var1) {
                        var2.append('\\');
                    }

                    var2.append(var7);
                }
            }
        }

        return var2.toString();
    }

    public static StringBuilder join(StringBuilder var0, ArrayList<String> var1, String var2) {
        int var3 = 0;

        for(int var4 = var1.size(); var3 < var4; ++var3) {
            if (var3 > 0) {
                var0.append(var2);
            }

            var0.append((String)var1.get(var3));
        }

        return var0;
    }

    public static String xmlAttr(String var0, String var1) {
        return " " + var0 + "=\"" + xmlText(var1) + "\"";
    }

    public static String xmlNode(String var0, String var1, String var2) {
        return xmlNode(var0, var1, var2, true);
    }

    public static String xmlNode(String var0, String var1, String var2, boolean var3) {
        StringBuilder var4 = new StringBuilder();
        var4.append('<').append(var0);
        if (var1 != null) {
            var4.append(var1);
        }

        if (var2 == null) {
            var4.append("/>\n");
            return var4.toString();
        } else {
            var4.append('>');
            if (var3 && var2.indexOf(10) >= 0) {
                var4.append('\n');
                indent(var4, var2, 4, true);
            } else {
                var4.append(var2);
            }

            var4.append("</").append(var0).append(">\n");
            return var4.toString();
        }
    }

    public static StringBuilder indent(StringBuilder var0, String var1, int var2, boolean var3) {
        int var4 = 0;

        int var6;
        for(int var5 = var1.length(); var4 < var5; var4 = var6) {
            for(var6 = 0; var6 < var2; ++var6) {
                var0.append(' ');
            }

            var6 = var1.indexOf(10, var4);
            var6 = var6 < 0 ? var5 : var6 + 1;
            var0.append(var1, var4, var6);
        }

        if (var3 && !var1.endsWith("\n")) {
            var0.append('\n');
        }

        return var0;
    }

    public static String xmlComment(String var0) {
        int var1 = 0;

        while(true) {
            var1 = var0.indexOf("--", var1);
            if (var1 < 0) {
                if (var0.indexOf(10) >= 0) {
                    StringBuilder var2 = (new StringBuilder(var0.length() + 18)).append("<!--\n");
                    return indent(var2, var0, 4, true).append("-->\n").toString();
                } else {
                    return "<!-- " + var0 + " -->\n";
                }
            }

            var0 = var0.substring(0, var1 + 1) + " " + var0.substring(var1 + 1);
        }
    }

    public static String xmlCData(String var0) {
        if (var0.contains("]]>")) {
            return xmlText(var0);
        } else {
            boolean var1 = var0.endsWith("\n");
            var0 = "<![CDATA[" + var0 + "]]>";
            return var1 ? var0 + "\n" : var0;
        }
    }

    public static String xmlStartDoc() {
        return "<?xml version=\"1.0\"?>\n";
    }

    public static String xmlText(String var0) {
        return xmlText(var0, false);
    }

    public static String xmlText(String var0, boolean var1) {
        int var2 = var0.length();
        StringBuilder var3 = new StringBuilder(var2);

        for(int var4 = 0; var4 < var2; ++var4) {
            char var5 = var0.charAt(var4);
            switch (var5) {
                case '\t':
                    var3.append(var5);
                    break;
                case '\n':
                case '\r':
                    if (var1) {
                        var3.append("&#x").append(Integer.toHexString(var5)).append(';');
                    } else {
                        var3.append(var5);
                    }
                    break;
                case '"':
                    var3.append("&quot;");
                    break;
                case '&':
                    var3.append("&amp;");
                    break;
                case '\'':
                    var3.append("&#39;");
                    break;
                case '<':
                    var3.append("&lt;");
                    break;
                case '>':
                    var3.append("&gt;");
                    break;
                default:
                    if (var5 >= ' ' && var5 <= 127) {
                        var3.append(var5);
                    } else {
                        var3.append("&#x").append(Integer.toHexString(var5)).append(';');
                    }
            }
        }

        return var3.toString();
    }

    public static String replaceAll(String var0, String var1, String var2) {
        int var3 = var0.indexOf(var1);
        if (var3 >= 0 && !var1.isEmpty()) {
            StringBuilder var4 = new StringBuilder(var0.length() - var1.length() + var2.length());
            int var5 = 0;

            do {
                var4.append(var0, var5, var3).append(var2);
                var5 = var3 + var1.length();
                var3 = var0.indexOf(var1, var5);
            } while(var3 >= 0);

            var4.append(var0, var5, var0.length());
            return var4.toString();
        } else {
            return var0;
        }
    }

    public static String quoteIdentifier(String var0) {
        return quoteIdentifier(new StringBuilder(var0.length() + 2), var0).toString();
    }

    public static StringBuilder quoteIdentifier(StringBuilder var0, String var1) {
        var0.append('"');
        int var2 = 0;

        for(int var3 = var1.length(); var2 < var3; ++var2) {
            char var4 = var1.charAt(var2);
            if (var4 == '"') {
                var0.append(var4);
            }

            var0.append(var4);
        }

        return var0.append('"');
    }

    public static boolean isNullOrEmpty(String var0) {
        return var0 == null || var0.isEmpty();
    }

    public static String quoteRemarkSQL(String var0) {
        var0 = replaceAll(var0, "*/", "++/");
        return replaceAll(var0, "/*", "/++");
    }

    public static String pad(String var0, int var1, String var2, boolean var3) {
        if (var1 < 0) {
            var1 = 0;
        }

        if (var1 < var0.length()) {
            return var0.substring(0, var1);
        } else if (var1 == var0.length()) {
            return var0;
        } else {
            char var4;
            if (var2 != null && !var2.isEmpty()) {
                var4 = var2.charAt(0);
            } else {
                var4 = ' ';
            }

            StringBuilder var5 = new StringBuilder(var1);
            var1 -= var0.length();
            if (var3) {
                var5.append(var0);
            }

            for(int var6 = 0; var6 < var1; ++var6) {
                var5.append(var4);
            }

            if (!var3) {
                var5.append(var0);
            }

            return var5.toString();
        }
    }

    public static char[] cloneCharArray(char[] var0) {
        if (var0 == null) {
            return null;
        } else {
            int var1 = var0.length;
            return var1 == 0 ? var0 : Arrays.copyOf(var0, var1);
        }
    }

    public static String trim(String var0, boolean var1, boolean var2, String var3) {
        char var4 = var3 != null && !var3.isEmpty() ? var3.charAt(0) : 32;
        int var5 = 0;
        int var6 = var0.length();
        if (var1) {
            while(var5 < var6 && var0.charAt(var5) == var4) {
                ++var5;
            }
        }

        if (var2) {
            while(var6 > var5 && var0.charAt(var6 - 1) == var4) {
                --var6;
            }
        }

        return var0.substring(var5, var6);
    }

    public static String trimSubstring(String var0, int var1) {
        return trimSubstring(var0, var1, var0.length());
    }

    public static String trimSubstring(String var0, int var1, int var2) {
        while(var1 < var2 && var0.charAt(var1) <= ' ') {
            ++var1;
        }

        while(var1 < var2 && var0.charAt(var2 - 1) <= ' ') {
            --var2;
        }

        return var0.substring(var1, var2);
    }

    public static StringBuilder trimSubstring(StringBuilder var0, String var1, int var2, int var3) {
        while(var2 < var3 && var1.charAt(var2) <= ' ') {
            ++var2;
        }

        while(var2 < var3 && var1.charAt(var3 - 1) <= ' ') {
            --var3;
        }

        return var0.append(var1, var2, var3);
    }

    public static String cache(String var0) {
        if (!SysProperties.OBJECT_CACHE) {
            return var0;
        } else if (var0 == null) {
            return var0;
        } else if (var0.isEmpty()) {
            return "";
        } else {
            String[] var1 = getCache();
            if (var1 != null) {
                int var2 = var0.hashCode();
                int var3 = var2 & SysProperties.OBJECT_CACHE_SIZE - 1;
                String var4 = var1[var3];
                if (var0.equals(var4)) {
                    return var4;
                }

                var1[var3] = var0;
            }

            return var0;
        }
    }

    public static void clearCache() {
        softCache = null;
    }

    public static int parseUInt31(String var0, int var1, int var2) {
        if (var2 <= var0.length() && var1 >= 0 && var1 <= var2) {
            if (var1 == var2) {
                throw new NumberFormatException("");
            } else {
                int var3 = 0;

                for(int var4 = var1; var4 < var2; ++var4) {
                    char var5 = var0.charAt(var4);
                    if (var5 < '0' || var5 > '9' || var3 > 214748364) {
                        throw new NumberFormatException(var0.substring(var1, var2));
                    }

                    var3 = var3 * 10 + var5 - 48;
                    if (var3 < 0) {
                        throw new NumberFormatException(var0.substring(var1, var2));
                    }
                }

                return var3;
            }
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public static byte[] convertHexToBytes(String var0) {
        int var1 = var0.length();
        if (var1 % 2 != 0) {
            throw DbException.get(90003, var0);
        } else {
            var1 /= 2;
            byte[] var2 = new byte[var1];
            int var3 = 0;
            int[] var4 = HEX_DECODE;

            try {
                for(int var5 = 0; var5 < var1; ++var5) {
                    int var6 = var4[var0.charAt(var5 + var5)] << 4 | var4[var0.charAt(var5 + var5 + 1)];
                    var3 |= var6;
                    var2[var5] = (byte)var6;
                }
            } catch (ArrayIndexOutOfBoundsException var7) {
                throw DbException.get(90004, var0);
            }

            if ((var3 & -256) != 0) {
                throw DbException.get(90004, var0);
            } else {
                return var2;
            }
        }
    }

    public static ByteArrayOutputStream convertHexWithSpacesToBytes(ByteArrayOutputStream var0, String var1) {
        int var2 = var1.length();
        if (var0 == null) {
            var0 = new ByteArrayOutputStream(var2 / 2);
        }

        int var3 = 0;
        int[] var4 = HEX_DECODE;

        try {
            int var5 = 0;

            while(var5 < var2) {
                char var6 = var1.charAt(var5++);
                if (var6 != ' ') {
                    char var7;
                    do {
                        if (var5 >= var2) {
                            if (((var3 | var4[var6]) & -256) != 0) {
                                throw DbException.get(90004, var1);
                            }

                            throw DbException.get(90003, var1);
                        }

                        var7 = var1.charAt(var5++);
                    } while(var7 == ' ');

                    int var8 = var4[var6] << 4 | var4[var7];
                    var3 |= var8;
                    var0.write(var8);
                }
            }
        } catch (ArrayIndexOutOfBoundsException var9) {
            throw DbException.get(90004, var1);
        }

        if ((var3 & -256) != 0) {
            throw DbException.get(90004, var1);
        } else {
            return var0;
        }
    }

    public static String convertBytesToHex(byte[] var0) {
        return convertBytesToHex(var0, var0.length);
    }

    public static String convertBytesToHex(byte[] var0, int var1) {
        char[] var2 = new char[var1 + var1];
        char[] var3 = HEX;

        for(int var4 = 0; var4 < var1; ++var4) {
            int var5 = var0[var4] & 255;
            var2[var4 + var4] = var3[var5 >> 4];
            var2[var4 + var4 + 1] = var3[var5 & 15];
        }

        return new String(var2);
    }

    public static StringBuilder convertBytesToHex(StringBuilder var0, byte[] var1) {
        return convertBytesToHex(var0, var1, var1.length);
    }

    public static StringBuilder convertBytesToHex(StringBuilder var0, byte[] var1, int var2) {
        char[] var3 = HEX;

        for(int var4 = 0; var4 < var2; ++var4) {
            int var5 = var1[var4] & 255;
            var0.append(var3[var5 >>> 4]).append(var3[var5 & 15]);
        }

        return var0;
    }

    public static StringBuilder appendHex(StringBuilder var0, long var1, int var3) {
        char[] var4 = HEX;
        int var5 = var3 * 8;

        while(var5 > 0) {
            var5 -= 4;
            StringBuilder var10000 = var0.append(var4[(int)(var1 >> var5) & 15]);
            var5 -= 4;
            var10000.append(var4[(int)(var1 >> var5) & 15]);
        }

        return var0;
    }

    public static boolean isNumber(String var0) {
        int var1 = var0.length();
        if (var1 == 0) {
            return false;
        } else {
            for(int var2 = 0; var2 < var1; ++var2) {
                if (!Character.isDigit(var0.charAt(var2))) {
                    return false;
                }
            }

            return true;
        }
    }

    public static boolean isWhitespaceOrEmpty(String var0) {
        int var1 = 0;

        for(int var2 = var0.length(); var1 < var2; ++var1) {
            if (var0.charAt(var1) > ' ') {
                return false;
            }
        }

        return true;
    }

    public static void appendZeroPadded(StringBuilder var0, int var1, long var2) {
        if (var1 == 2) {
            if (var2 < 10L) {
                var0.append('0');
            }

            var0.append(var2);
        } else {
            String var4 = Long.toString(var2);

            for(var1 -= var4.length(); var1 > 0; --var1) {
                var0.append('0');
            }

            var0.append(var4);
        }

    }

    public static String escapeMetaDataPattern(String var0) {
        return var0 != null && !var0.isEmpty() ? replaceAll(var0, "\\", "\\\\") : var0;
    }

    static {
        int var0;
        for(var0 = 0; var0 < HEX_DECODE.length; ++var0) {
            HEX_DECODE[var0] = -1;
        }

        for(var0 = 0; var0 <= 9; HEX_DECODE[var0 + 48] = var0++) {
        }

        for(var0 = 0; var0 <= 5; ++var0) {
            HEX_DECODE[var0 + 97] = HEX_DECODE[var0 + 65] = var0 + 10;
        }

    }
}
