/*
 * Copyright 2004-2019 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */

package org.dizitart.no2.mvstore.compat.v1.mvstore.fs;

import org.dizitart.no2.mvstore.compat.v1.mvstore.SysProperties;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.NonWritableChannelException;


class FileDisk extends FileBase {
    private final RandomAccessFile file;
    private final String name;
    private final boolean readOnly;

    FileDisk(String var1, String var2) throws FileNotFoundException {
        this.file = new RandomAccessFile(var1, var2);
        this.name = var1;
        this.readOnly = var2.equals("r");
    }

    public void force(boolean var1) throws IOException {
        String var2 = SysProperties.SYNC_METHOD;
        if (!"".equals(var2)) {
            if ("sync".equals(var2)) {
                this.file.getFD().sync();
            } else if ("force".equals(var2)) {
                this.file.getChannel().force(true);
            } else if ("forceFalse".equals(var2)) {
                this.file.getChannel().force(false);
            } else {
                this.file.getFD().sync();
            }
        }

    }

    public FileChannel truncate(long var1) throws IOException {
        if (this.readOnly) {
            throw new NonWritableChannelException();
        } else {
            this.file.getChannel().truncate(var1);
            return this;
        }
    }

    public synchronized FileLock tryLock(long var1, long var3, boolean var5) throws IOException {
        return this.file.getChannel().tryLock(var1, var3, var5);
    }

    public void implCloseChannel() throws IOException {
        this.file.close();
    }

    public long position() throws IOException {
        return this.file.getFilePointer();
    }

    public long size() throws IOException {
        return this.file.length();
    }

    public int read(ByteBuffer var1) throws IOException {
        int var2 = this.file.read(var1.array(), var1.arrayOffset() + var1.position(), var1.remaining());
        if (var2 > 0) {
            var1.position(var1.position() + var2);
        }

        return var2;
    }

    public FileChannel position(long var1) throws IOException {
        this.file.seek(var1);
        return this;
    }

    public int write(ByteBuffer var1) throws IOException {
        int var2 = var1.remaining();
        this.file.write(var1.array(), var1.arrayOffset() + var1.position(), var2);
        var1.position(var1.position() + var2);
        return var2;
    }

    public String toString() {
        return this.name;
    }
}
