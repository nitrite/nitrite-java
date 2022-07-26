/*
 * Copyright 2004-2019 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.dizitart.no2.mvstore.compat.v1.mvstore.fs;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.NonWritableChannelException;

class FileNio extends FileBase {
    private final String name;
    private final FileChannel channel;

    FileNio(String var1, String var2) throws IOException {
        this.name = var1;
        this.channel = (new RandomAccessFile(var1, var2)).getChannel();
    }

    public void implCloseChannel() throws IOException {
        this.channel.close();
    }

    public long position() throws IOException {
        return this.channel.position();
    }

    public long size() throws IOException {
        return this.channel.size();
    }

    public int read(ByteBuffer var1) throws IOException {
        return this.channel.read(var1);
    }

    public FileChannel position(long var1) throws IOException {
        this.channel.position(var1);
        return this;
    }

    public int read(ByteBuffer var1, long var2) throws IOException {
        return this.channel.read(var1, var2);
    }

    public int write(ByteBuffer var1, long var2) throws IOException {
        return this.channel.write(var1, var2);
    }

    public FileChannel truncate(long var1) throws IOException {
        long var3 = this.channel.size();
        if (var1 < var3) {
            long var5 = this.channel.position();
            this.channel.truncate(var1);
            long var7 = this.channel.position();
            if (var5 < var1) {
                if (var7 != var5) {
                    this.channel.position(var5);
                }
            } else if (var7 > var1) {
                this.channel.position(var1);
            }
        }

        return this;
    }

    public void force(boolean var1) throws IOException {
        this.channel.force(var1);
    }

    public int write(ByteBuffer var1) throws IOException {
        try {
            return this.channel.write(var1);
        } catch (NonWritableChannelException var3) {
            throw new IOException("read only");
        }
    }

    public synchronized FileLock tryLock(long var1, long var3, boolean var5) throws IOException {
        return this.channel.tryLock(var1, var3, var5);
    }

    public String toString() {
        return "nio:" + this.name;
    }
}
