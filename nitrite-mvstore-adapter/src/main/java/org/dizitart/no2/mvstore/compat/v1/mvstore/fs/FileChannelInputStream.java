/*
 * Copyright 2004-2019 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */

package org.dizitart.no2.mvstore.compat.v1.mvstore.fs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileChannelInputStream extends InputStream {
    private final FileChannel channel;
    private final boolean closeChannel;
    private ByteBuffer buffer;
    private long pos;

    public FileChannelInputStream(FileChannel var1, boolean var2) {
        this.channel = var1;
        this.closeChannel = var2;
    }

    public int read() throws IOException {
        if (this.buffer == null) {
            this.buffer = ByteBuffer.allocate(1);
        }

        this.buffer.rewind();
        int var1 = this.channel.read(this.buffer, (long)(this.pos++));
        return var1 < 0 ? -1 : this.buffer.get(0) & 255;
    }

    public int read(byte[] var1) throws IOException {
        return this.read(var1, 0, var1.length);
    }

    public int read(byte[] var1, int var2, int var3) throws IOException {
        ByteBuffer var4 = ByteBuffer.wrap(var1, var2, var3);
        int var5 = this.channel.read(var4, this.pos);
        if (var5 == -1) {
            return -1;
        } else {
            this.pos += (long)var5;
            return var5;
        }
    }

    public void close() throws IOException {
        if (this.closeChannel) {
            this.channel.close();
        }

    }
}
