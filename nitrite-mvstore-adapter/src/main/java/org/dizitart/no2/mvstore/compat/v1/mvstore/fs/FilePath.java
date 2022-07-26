/*
 * Copyright 2004-2019 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.dizitart.no2.mvstore.compat.v1.mvstore.fs;

import org.h2.util.MathUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public abstract class FilePath {
    private static FilePath defaultProvider;
    private static ConcurrentHashMap<String, FilePath> providers;
    private static String tempRandom;
    private static long tempSequence;
    protected String name;

    public FilePath() {
    }

    public static FilePath get(String var0) {
        var0 = var0.replace('\\', '/');
        int var1 = var0.indexOf(58);
        registerDefaultProviders();
        if (var1 < 2) {
            return defaultProvider.getPath(var0);
        } else {
            String var2 = var0.substring(0, var1);
            FilePath var3 = providers.get(var2);
            if (var3 == null) {
                var3 = defaultProvider;
            }

            return var3.getPath(var0);
        }
    }

    private static void registerDefaultProviders() {
        if (providers == null || defaultProvider == null) {
            ConcurrentHashMap var0 = new ConcurrentHashMap();
            String[] var1 = new String[] {
                "org.dizitart.no2.mvstore.compat.v1.mvstore.fs.FilePathDisk",
                "org.dizitart.no2.mvstore.compat.v1.mvstore.fs.FilePathNio",
                "org.dizitart.no2.mvstore.compat.v1.mvstore.fs.FilePathEncrypt",
                "org.h2.store.fs.FilePathMem",
                "org.h2.store.fs.FilePathMemLZF",
                "org.h2.store.fs.FilePathNioMem",
                "org.h2.store.fs.FilePathNioMemLZF",
                "org.h2.store.fs.FilePathSplit",
                "org.h2.store.fs.FilePathNioMapped",
                "org.h2.store.fs.FilePathAsync",
                "org.h2.store.fs.FilePathZip",
                "org.h2.store.fs.FilePathRetryOnInterrupt"
            };
            int var2 = var1.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                String var4 = var1[var3];

                try {
                    FilePath var5 = (FilePath)Class.forName(var4).getDeclaredConstructor().newInstance();
                    var0.put(var5.getScheme(), var5);
                    if (defaultProvider == null) {
                        defaultProvider = var5;
                    }
                } catch (Exception var6) {
                }
            }

            providers = var0;
        }

    }

    public static void register(FilePath var0) {
        registerDefaultProviders();
        providers.put(var0.getScheme(), var0);
    }

    public static void unregister(FilePath var0) {
        registerDefaultProviders();
        providers.remove(var0.getScheme());
    }

    public abstract long size();

    public abstract void moveTo(FilePath var1, boolean var2);

    public abstract boolean createFile();

    public abstract boolean exists();

    public abstract void delete();

    public abstract List<FilePath> newDirectoryStream();

    public abstract FilePath toRealPath();

    public abstract FilePath getParent();

    public abstract boolean isDirectory();

    public abstract boolean isAbsolute();

    public abstract long lastModified();

    public abstract boolean canWrite();

    public abstract void createDirectory();

    public String getName() {
        int var1 = Math.max(this.name.indexOf(58), this.name.lastIndexOf(47));
        return var1 < 0 ? this.name : this.name.substring(var1 + 1);
    }

    public abstract OutputStream newOutputStream(boolean var1) throws IOException;

    public abstract FileChannel open(String var1) throws IOException;

    public abstract InputStream newInputStream() throws IOException;

    public abstract boolean setReadOnly();

    public FilePath createTempFile(String var1, boolean var2) throws IOException {
        while(true) {
            FilePath var3 = this.getPath(this.name + getNextTempFileNamePart(false) + var1);
            if (!var3.exists() && var3.createFile()) {
                var3.open("rw").close();
                return var3;
            }

            getNextTempFileNamePart(true);
        }
    }

    protected static synchronized String getNextTempFileNamePart(boolean var0) {
        if (var0 || tempRandom == null) {
            tempRandom = MathUtils.randomInt(Integer.MAX_VALUE) + ".";
        }

        return tempRandom + tempSequence++;
    }

    public String toString() {
        return this.name;
    }

    public abstract String getScheme();

    public abstract FilePath getPath(String var1);

    public FilePath unwrap() {
        return this;
    }
}
