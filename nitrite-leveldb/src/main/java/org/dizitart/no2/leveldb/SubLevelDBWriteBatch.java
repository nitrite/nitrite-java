package org.dizitart.no2.leveldb;

import org.iq80.leveldb.WriteBatch;

import java.io.IOException;

import static org.dizitart.no2.leveldb.BufferUtils.concat;

public class SubLevelDBWriteBatch implements WriteBatch {
    private final byte[] prefix;
    private final WriteBatch delegate;

    public SubLevelDBWriteBatch(byte[] prefix, WriteBatch delegate) {
        this.prefix = prefix;
        this.delegate = delegate;
    }

    @Override
    public WriteBatch put(byte[] key, byte[] value) {
        byte[] newKey = concat(prefix, key);
        return delegate.put(newKey, value);
    }

    @Override
    public WriteBatch delete(byte[] key) {
        byte[] newKey = concat(prefix, key);
        return delegate.delete(newKey);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
