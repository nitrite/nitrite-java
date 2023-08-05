/*
 * Copyright 2004-2019 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */

package org.dizitart.no2.mvstore.compat.v1.mvstore.fs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public abstract class FileBase extends FileChannel {
    public FileBase() {
    }

    public abstract long size() throws IOException;

    public abstract long position() throws IOException;

    public abstract FileChannel position(long var1) throws IOException;

    public abstract int read(ByteBuffer var1) throws IOException;

    public abstract int write(ByteBuffer var1) throws IOException;

    public synchronized int read(ByteBuffer var1, long var2) throws IOException {
        long var4 = this.position();
        this.position(var2);
        int var6 = this.read(var1);
        this.position(var4);
        return var6;
    }

    public synchronized int write(ByteBuffer var1, long var2) throws IOException {
        long var4 = this.position();
        this.position(var2);
        int var6 = this.write(var1);
        this.position(var4);
        return var6;
    }

    public abstract FileChannel truncate(long var1) throws IOException;

    public void force(boolean var1) throws IOException {
    }

    protected void implCloseChannel() throws IOException {
    }

    public FileLock lock(long var1, long var3, boolean var5) throws IOException {
        throw new UnsupportedOperationException();
    }

    public MappedByteBuffer map(MapMode var1, long var2, long var4) throws IOException {
        throw new UnsupportedOperationException();
    }

    public long read(ByteBuffer[] var1, int var2, int var3) throws IOException {
        throw new UnsupportedOperationException();
    }

    public long transferFrom(ReadableByteChannel var1, long var2, long var4) throws IOException {
        throw new UnsupportedOperationException();
    }

    public long transferTo(long var1, long var3, WritableByteChannel var5) throws IOException {
        throw new UnsupportedOperationException();
    }

    public FileLock tryLock(long var1, long var3, boolean var5) throws IOException {
        throw new UnsupportedOperationException();
    }

    public long write(ByteBuffer[] var1, int var2, int var3) throws IOException {
        throw new UnsupportedOperationException();
    }
}
