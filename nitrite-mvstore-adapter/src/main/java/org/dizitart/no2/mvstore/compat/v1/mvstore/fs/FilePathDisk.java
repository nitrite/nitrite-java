/*
 * Copyright 2004-2019 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */

package org.dizitart.no2.mvstore.compat.v1.mvstore.fs;

import org.dizitart.no2.mvstore.compat.v1.mvstore.SysProperties;
import org.dizitart.no2.mvstore.compat.v1.mvstore.util.IOUtils;
import org.h2.message.DbException;

import java.io.*;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class FilePathDisk extends FilePath {
    private static final String CLASSPATH_PREFIX = "classpath:";

    public FilePathDisk() {
    }

    public FilePathDisk getPath(String var1) {
        FilePathDisk var2 = new FilePathDisk();
        var2.name = translateFileName(var1);
        return var2;
    }

    public long size() {
        if (this.name.startsWith("classpath:")) {
            try {
                String var1 = this.name.substring("classpath:".length());
                if (!var1.startsWith("/")) {
                    var1 = "/" + var1;
                }

                URL var2 = this.getClass().getResource(var1);
                return var2 != null ? new File(var2.getPath()).length() : 0L;
            } catch (Exception var3) {
                return 0L;
            }
        } else {
            return (new File(this.name)).length();
        }
    }

    protected static String translateFileName(String var0) {
        var0 = var0.replace('\\', '/');
        if (var0.startsWith("file:")) {
            var0 = var0.substring("file:".length());
        }

        return expandUserHomeDirectory(var0);
    }

    public static String expandUserHomeDirectory(String var0) {
        if (var0.startsWith("~") && (var0.length() == 1 || var0.startsWith("~/"))) {
            String var1 = SysProperties.USER_HOME;
            var0 = var1 + var0.substring(1);
        }

        return var0;
    }

    public void moveTo(FilePath var1, boolean var2) {
        File var3 = new File(this.name);
        File var4 = new File(var1.name);
        if (!var3.getAbsolutePath().equals(var4.getAbsolutePath())) {
            if (!var3.exists()) {
                throw DbException.get(90024, this.name + " (not found)", var1.name);
            } else if (var2) {
                boolean var7 = var3.renameTo(var4);
                if (!var7) {
                    throw DbException.get(90024, this.name, var1.name);
                }
            } else if (var4.exists()) {
                throw DbException.get(90024, this.name, var1 + " (exists)");
            } else {
                for(int var5 = 0; var5 < SysProperties.MAX_FILE_RETRY; ++var5) {
                    IOUtils.trace("rename", this.name + " >" + var1, (Object)null);
                    boolean var6 = var3.renameTo(var4);
                    if (var6) {
                        return;
                    }

                    wait(var5);
                }

                throw DbException.get(90024, this.name, var1.name);
            }
        }
    }

    private static void wait(int var0) {
        if (var0 == 8) {
            System.gc();
        }

        try {
            long var1 = (long)Math.min(256, var0 * var0);
            Thread.sleep(var1);
        } catch (InterruptedException var3) {
        }

    }

    public boolean createFile() {
        File var1 = new File(this.name);
        int var2 = 0;

        while(var2 < SysProperties.MAX_FILE_RETRY) {
            try {
                return var1.createNewFile();
            } catch (IOException var4) {
                wait(var2);
                ++var2;
            }
        }

        return false;
    }

    public boolean exists() {
        return (new File(this.name)).exists();
    }

    public void delete() {
        File var1 = new File(this.name);

        for(int var2 = 0; var2 < SysProperties.MAX_FILE_RETRY; ++var2) {
            IOUtils.trace("delete", this.name, (Object)null);
            boolean var3 = var1.delete();
            if (var3 || !var1.exists()) {
                return;
            }

            wait(var2);
        }

        throw DbException.get(90025, this.name);
    }

    public List<FilePath> newDirectoryStream() {
        ArrayList var1 = new ArrayList();
        File var2 = new File(this.name);

        try {
            String[] var3 = var2.list();
            if (var3 != null) {
                String var4 = var2.getCanonicalPath();
                if (!var4.endsWith(SysProperties.FILE_SEPARATOR)) {
                    var4 = var4 + SysProperties.FILE_SEPARATOR;
                }

                var1.ensureCapacity(var3.length);
                String[] var5 = var3;
                int var6 = var3.length;

                for(int var7 = 0; var7 < var6; ++var7) {
                    String var8 = var5[var7];
                    var1.add(this.getPath(var4 + var8));
                }
            }

            return var1;
        } catch (IOException var9) {
            throw DbException.convertIOException(var9, this.name);
        }
    }

    public boolean canWrite() {
        return canWriteInternal(new File(this.name));
    }

    public boolean setReadOnly() {
        File var1 = new File(this.name);
        return var1.setReadOnly();
    }

    public FilePathDisk toRealPath() {
        try {
            String var1 = (new File(this.name)).getCanonicalPath();
            return this.getPath(var1);
        } catch (IOException var2) {
            throw DbException.convertIOException(var2, this.name);
        }
    }

    public FilePath getParent() {
        String var1 = (new File(this.name)).getParent();
        return var1 == null ? null : this.getPath(var1);
    }

    public boolean isDirectory() {
        return (new File(this.name)).isDirectory();
    }

    public boolean isAbsolute() {
        return (new File(this.name)).isAbsolute();
    }

    public long lastModified() {
        return (new File(this.name)).lastModified();
    }

    private static boolean canWriteInternal(File var0) {
        try {
            if (!var0.canWrite()) {
                return false;
            }
        } catch (Exception var16) {
            return false;
        }

        RandomAccessFile var1 = null;

        boolean var3;
        try {
            var1 = new RandomAccessFile(var0, "rw");
            boolean var2 = true;
            return var2;
        } catch (FileNotFoundException var14) {
            var3 = false;
        } finally {
            if (var1 != null) {
                try {
                    var1.close();
                } catch (IOException var13) {
                }
            }

        }

        return var3;
    }

    public void createDirectory() {
        File var1 = new File(this.name);

        for(int var2 = 0; var2 < SysProperties.MAX_FILE_RETRY; ++var2) {
            if (var1.exists()) {
                if (var1.isDirectory()) {
                    return;
                }

                throw DbException.get(90062, this.name + " (a file with this name already exists)");
            }

            if (var1.mkdir()) {
                return;
            }

            wait(var2);
        }

        throw DbException.get(90062, this.name);
    }

    public OutputStream newOutputStream(boolean var1) throws IOException {
        try {
            File var2 = new File(this.name);
            File var3 = var2.getParentFile();
            if (var3 != null) {
                FileUtils.createDirectories(var3.getAbsolutePath());
            }

            FileOutputStream var4 = new FileOutputStream(this.name, var1);
            IOUtils.trace("openFileOutputStream", this.name, var4);
            return var4;
        } catch (IOException var5) {
            freeMemoryAndFinalize();
            return new FileOutputStream(this.name);
        }
    }

    public InputStream newInputStream() throws IOException {
        if (this.name.matches("[a-zA-Z]{2,19}:.*")) {
            if (this.name.startsWith("classpath:")) {
                String var4 = this.name.substring("classpath:".length());
                if (!var4.startsWith("/")) {
                    var4 = "/" + var4;
                }

                InputStream var2 = this.getClass().getResourceAsStream(var4);
                if (var2 == null) {
                    var2 = Thread.currentThread().getContextClassLoader().getResourceAsStream(var4.substring(1));
                }

                if (var2 == null) {
                    throw new FileNotFoundException("resource " + var4);
                } else {
                    return var2;
                }
            } else {
                URL var3 = new URL(this.name);
                return var3.openStream();
            }
        } else {
            FileInputStream var1 = new FileInputStream(this.name);
            IOUtils.trace("openFileInputStream", this.name, var1);
            return var1;
        }
    }

    static void freeMemoryAndFinalize() {
        IOUtils.trace("freeMemoryAndFinalize", null, null);
        Runtime var0 = Runtime.getRuntime();
        long var1 = var0.freeMemory();

        for(int var3 = 0; var3 < 16; ++var3) {
            var0.gc();
            long var4 = var0.freeMemory();
            if (var4 == var1) {
                break;
            }

            var1 = var4;
        }

    }

    public FileChannel open(String var1) throws IOException {
        FileDisk var2;
        try {
            var2 = new FileDisk(this.name, var1);
            IOUtils.trace("open", this.name, var2);
        } catch (IOException var6) {
            freeMemoryAndFinalize();

            try {
                var2 = new FileDisk(this.name, var1);
            } catch (IOException var5) {
                throw var6;
            }
        }

        return var2;
    }

    public String getScheme() {
        return "file";
    }

    public FilePath createTempFile(String var1, boolean var2) throws IOException {
        String var3 = this.name + ".";
        String var4 = (new File(var3)).getName();
        File var5;
        if (var2) {
            var5 = new File(System.getProperty("java.io.tmpdir", "."));
        } else {
            var5 = (new File(var3)).getAbsoluteFile().getParentFile();
        }

        FileUtils.createDirectories(var5.getAbsolutePath());

        while(true) {
            File var6 = new File(var5, var4 + getNextTempFileNamePart(false) + var1);
            if (!var6.exists() && var6.createNewFile()) {
                return get(var6.getCanonicalPath());
            }

            getNextTempFileNamePart(true);
        }
    }
}
