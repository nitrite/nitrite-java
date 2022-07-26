/*
 * Copyright 2004-2019 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.dizitart.no2.mvstore.compat.v1.mvstore.fs;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileChannelOutputStream extends OutputStream {
    private final FileChannel channel;
    private final byte[] buffer = new byte[]{0};

    public FileChannelOutputStream(FileChannel var1, boolean var2) throws IOException {
        this.channel = var1;
        if (var2) {
            var1.position(var1.size());
        } else {
            var1.position(0L);
            var1.truncate(0L);
        }

    }

    public void write(int var1) throws IOException {
        this.buffer[0] = (byte)var1;
        FileUtils.writeFully(this.channel, ByteBuffer.wrap(this.buffer));
    }

    public void write(byte[] var1) throws IOException {
        FileUtils.writeFully(this.channel, ByteBuffer.wrap(var1));
    }

    public void write(byte[] var1, int var2, int var3) throws IOException {
        FileUtils.writeFully(this.channel, ByteBuffer.wrap(var1, var2, var3));
    }

    public void close() throws IOException {
        this.channel.close();
    }
}
