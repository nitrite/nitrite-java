/*
 * Copyright 2004-2019 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.dizitart.no2.mvstore.compat.v1.mvstore.fs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.List;

public abstract class FilePathWrapper extends FilePath {
    private FilePath base;

    public FilePathWrapper() {
    }

    public FilePathWrapper getPath(String var1) {
        return this.create(var1, this.unwrap(var1));
    }

    public FilePathWrapper wrap(FilePath var1) {
        return var1 == null ? null : this.create(this.getPrefix() + var1.name, var1);
    }

    public FilePath unwrap() {
        return this.unwrap(this.name);
    }

    private FilePathWrapper create(String var1, FilePath var2) {
        try {
            FilePathWrapper var3 = (FilePathWrapper)this.getClass().getDeclaredConstructor().newInstance();
            var3.name = var1;
            var3.base = var2;
            return var3;
        } catch (Exception var4) {
            throw new IllegalArgumentException("Path: " + var1, var4);
        }
    }

    protected String getPrefix() {
        return this.getScheme() + ":";
    }

    protected FilePath unwrap(String var1) {
        return FilePath.get(var1.substring(this.getScheme().length() + 1));
    }

    protected FilePath getBase() {
        return this.base;
    }

    public boolean canWrite() {
        return this.base.canWrite();
    }

    public void createDirectory() {
        this.base.createDirectory();
    }

    public boolean createFile() {
        return this.base.createFile();
    }

    public void delete() {
        this.base.delete();
    }

    public boolean exists() {
        return this.base.exists();
    }

    public FilePath getParent() {
        return this.wrap(this.base.getParent());
    }

    public boolean isAbsolute() {
        return this.base.isAbsolute();
    }

    public boolean isDirectory() {
        return this.base.isDirectory();
    }

    public long lastModified() {
        return this.base.lastModified();
    }

    public FilePath toRealPath() {
        return this.wrap(this.base.toRealPath());
    }

    public List<FilePath> newDirectoryStream() {
        List var1 = this.base.newDirectoryStream();
        int var2 = 0;

        for(int var3 = var1.size(); var2 < var3; ++var2) {
            var1.set(var2, this.wrap((FilePath)var1.get(var2)));
        }

        return var1;
    }

    public void moveTo(FilePath var1, boolean var2) {
        this.base.moveTo(((FilePathWrapper)var1).base, var2);
    }

    public InputStream newInputStream() throws IOException {
        return this.base.newInputStream();
    }

    public OutputStream newOutputStream(boolean var1) throws IOException {
        return this.base.newOutputStream(var1);
    }

    public FileChannel open(String var1) throws IOException {
        return this.base.open(var1);
    }

    public boolean setReadOnly() {
        return this.base.setReadOnly();
    }

    public long size() {
        return this.base.size();
    }

    public FilePath createTempFile(String var1, boolean var2) throws IOException {
        return this.wrap(this.base.createTempFile(var1, var2));
    }
}
