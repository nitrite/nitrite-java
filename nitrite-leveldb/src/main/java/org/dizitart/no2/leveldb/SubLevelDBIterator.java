package org.dizitart.no2.leveldb;

import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.impl.SeekingIteratorAdapter;
import org.iq80.leveldb.util.Slice;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.google.common.primitives.Bytes.concat;
import static org.dizitart.no2.leveldb.BufferUtils.slice;

public class SubLevelDBIterator implements DBIterator {
    private final byte[] prefix;
    private final DBIterator delegate;
    private final int prefixLength;

    private boolean nextElementSet;
    private Map.Entry<byte[], byte[]> nextElement;

    public SubLevelDBIterator(byte[] prefix, DBIterator iterator) {
        this.prefix = prefix;
        this.prefixLength = prefix.length;
        this.delegate = iterator;
        seekToFirst();
    }

    @Override
    public void seek(byte[] key) {
        byte[] newKey = concat(prefix, key);
        delegate.seek(newKey);
    }

    @Override
    public void seekToFirst() {
        delegate.seek(prefix);
    }

    @Override
    public Map.Entry<byte[], byte[]> peekNext() {
        Map.Entry<byte[], byte[]> entry = delegate.peekNext();
        byte[] key = slice(entry.getKey(), prefixLength);
        return new SeekingIteratorAdapter.DbEntry(new Slice(key), new Slice(entry.getValue()));
    }

    @Override
    public boolean hasPrev() {
        return delegate.hasPrev();
    }

    @Override
    public Map.Entry<byte[], byte[]> prev() {
        return delegate.prev();
    }

    @Override
    public Map.Entry<byte[], byte[]> peekPrev() {
        return delegate.peekPrev();
    }

    @Override
    public void seekToLast() {
        delegate.seekToLast();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public boolean hasNext() {
        return nextElementSet || setNextElement();
    }

    @Override
    public Map.Entry<byte[], byte[]> next() {
        if (!nextElementSet && !setNextElement()) {
            throw new NoSuchElementException();
        }
        nextElementSet = false;
        return nextElement;
    }

    private boolean setNextElement() {
        if (delegate.hasNext()) {
            Map.Entry<byte[], byte[]> entry = delegate.peekNext();
            if (entry == null) {
                return false;
            }

            byte[] key = entry.getKey();
            byte[] keyPrefix = slice(key, 0, prefixLength);

            if(Arrays.equals(keyPrefix, prefix)) {
                Map.Entry<byte[], byte[]> next = delegate.next();
                byte[] newKey = slice(key, prefixLength);
                nextElementSet = true;
                nextElement = new SeekingIteratorAdapter.DbEntry(new Slice(newKey), new Slice(next.getValue()));
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
}
