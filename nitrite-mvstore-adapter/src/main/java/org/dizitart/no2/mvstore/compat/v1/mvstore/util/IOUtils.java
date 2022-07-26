/*
 * Copyright 2004-2019 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */

package org.dizitart.no2.mvstore.compat.v1.mvstore.util;

import org.dizitart.no2.mvstore.compat.v1.mvstore.SysProperties;
import org.dizitart.no2.mvstore.compat.v1.mvstore.fs.FileUtils;
import org.h2.jdbc.JdbcException;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class IOUtils {
    private IOUtils() {
    }

    public static void closeSilently(AutoCloseable var0) {
        if (var0 != null) {
            try {
                trace("closeSilently", (String)null, var0);
                var0.close();
            } catch (Exception var2) {
            }
        }

    }

    public static void skipFully(InputStream var0, long var1) throws IOException {
        try {
            while(var1 > 0L) {
                long var3 = var0.skip(var1);
                if (var3 <= 0L) {
                    throw new EOFException();
                }

                var1 -= var3;
            }

        } catch (Exception var5) {
            throw convertToIOException(var5);
        }
    }

    public static void skipFully(Reader var0, long var1) throws IOException {
        try {
            while(var1 > 0L) {
                long var3 = var0.skip(var1);
                if (var3 <= 0L) {
                    throw new EOFException();
                }

                var1 -= var3;
            }

        } catch (Exception var5) {
            throw convertToIOException(var5);
        }
    }

    public static long copyAndClose(InputStream var0, OutputStream var1) throws IOException {
        long var4;
        try {
            long var2 = copyAndCloseInput(var0, var1);
            var1.close();
            var4 = var2;
        } catch (Exception var9) {
            throw convertToIOException(var9);
        } finally {
            closeSilently(var1);
        }

        return var4;
    }

    public static long copyAndCloseInput(InputStream var0, OutputStream var1) throws IOException {
        long var2;
        try {
            var2 = copy(var0, var1);
        } catch (Exception var7) {
            throw convertToIOException(var7);
        } finally {
            closeSilently(var0);
        }

        return var2;
    }

    public static long copy(InputStream var0, OutputStream var1) throws IOException {
        return copy(var0, var1, Long.MAX_VALUE);
    }

    public static long copy(InputStream var0, OutputStream var1, long var2) throws IOException {
        try {
            long var4 = 0L;
            int var6 = (int)Math.min(var2, 4096L);

            for(byte[] var7 = new byte[var6]; var2 > 0L; var6 = (int)Math.min(var2, 4096L)) {
                var6 = var0.read(var7, 0, var6);
                if (var6 < 0) {
                    break;
                }

                if (var1 != null) {
                    var1.write(var7, 0, var6);
                }

                var4 += (long)var6;
                var2 -= (long)var6;
            }

            return var4;
        } catch (Exception var8) {
            throw convertToIOException(var8);
        }
    }

    public static long copyAndCloseInput(Reader var0, Writer var1, long var2) throws IOException {
        try {
            long var4 = 0L;
            int var6 = (int)Math.min(var2, 4096L);
            char[] var7 = new char[var6];

            while(true) {
                if (var2 > 0L) {
                    var6 = var0.read(var7, 0, var6);
                    if (var6 >= 0) {
                        if (var1 != null) {
                            var1.write(var7, 0, var6);
                        }

                        var2 -= (long)var6;
                        var6 = (int)Math.min(var2, 4096L);
                        var4 += (long)var6;
                        continue;
                    }
                }

                long var8 = var4;
                return var8;
            }
        } catch (Exception var13) {
            throw convertToIOException(var13);
        } finally {
            var0.close();
        }
    }

    public static byte[] readBytesAndClose(InputStream var0, int var1) throws IOException {
        byte[] var4;
        try {
            if (var1 <= 0) {
                var1 = Integer.MAX_VALUE;
            }

            int var2 = Math.min(4096, var1);
            ByteArrayOutputStream var3 = new ByteArrayOutputStream(var2);
            copy(var0, var3, (long)var1);
            var4 = var3.toByteArray();
        } catch (Exception var8) {
            throw convertToIOException(var8);
        } finally {
            var0.close();
        }

        return var4;
    }

    public static String readStringAndClose(Reader var0, int var1) throws IOException {
        String var4;
        try {
            if (var1 <= 0) {
                var1 = Integer.MAX_VALUE;
            }

            int var2 = Math.min(4096, var1);
            StringWriter var3 = new StringWriter(var2);
            copyAndCloseInput(var0, var3, (long)var1);
            var4 = var3.toString();
        } finally {
            var0.close();
        }

        return var4;
    }

    public static int readFully(InputStream var0, byte[] var1, int var2) throws IOException {
        try {
            int var3 = 0;

            int var5;
            for(int var4 = Math.min(var2, var1.length); var4 > 0; var4 -= var5) {
                var5 = var0.read(var1, var3, var4);
                if (var5 < 0) {
                    break;
                }

                var3 += var5;
            }

            return var3;
        } catch (Exception var6) {
            throw convertToIOException(var6);
        }
    }

    public static int readFully(Reader var0, char[] var1, int var2) throws IOException {
        try {
            int var3 = 0;

            int var5;
            for(int var4 = Math.min(var2, var1.length); var4 > 0; var4 -= var5) {
                var5 = var0.read(var1, var3, var4);
                if (var5 < 0) {
                    break;
                }

                var3 += var5;
            }

            return var3;
        } catch (Exception var6) {
            throw convertToIOException(var6);
        }
    }

    public static Reader getBufferedReader(InputStream var0) {
        return var0 == null ? null : new BufferedReader(new InputStreamReader(var0, StandardCharsets.UTF_8));
    }

    public static Reader getReader(InputStream var0) {
        return var0 == null ? null : new BufferedReader(new InputStreamReader(var0, StandardCharsets.UTF_8));
    }

    public static Writer getBufferedWriter(OutputStream var0) {
        return var0 == null ? null : new BufferedWriter(new OutputStreamWriter(var0, StandardCharsets.UTF_8));
    }

    public static Reader getAsciiReader(InputStream var0) {
        return var0 == null ? null : new InputStreamReader(var0, StandardCharsets.US_ASCII);
    }

    public static void trace(String var0, String var1, Object var2) {
        if (SysProperties.TRACE_IO) {
            System.out.println("IOUtils." + var0 + ' ' + var1 + ' ' + var2);
        }

    }

    public static InputStream getInputStreamFromString(String var0) {
        return var0 == null ? null : new ByteArrayInputStream(var0.getBytes(StandardCharsets.UTF_8));
    }

    public static void copyFiles(String var0, String var1) throws IOException {
        InputStream var2 = FileUtils.newInputStream(var0);
        OutputStream var3 = FileUtils.newOutputStream(var1, false);
        copyAndClose(var2, var3);
    }

    public static IOException convertToIOException(Throwable var0) {
        if (var0 instanceof IOException) {
            return (IOException)var0;
        } else {
            if (var0 instanceof JdbcException && var0.getCause() != null) {
                var0 = var0.getCause();
            }

            return new IOException(var0.toString(), var0);
        }
    }
}
