package org.dizitart.no2.leveldb;

import com.google.common.base.Charsets;
import lombok.Getter;
import org.iq80.leveldb.*;

import java.io.IOException;

import static org.dizitart.no2.leveldb.BufferUtils.concat;

@Getter
public class SubLevelDB implements DB {
    private final DB db;
    private final String prefix;
    private final byte[] prefixBuffer;

    public SubLevelDB(DB db, String prefix) {
        this.db = db;
        this.prefix = prefix;
        this.prefixBuffer = prefix.getBytes(Charsets.UTF_8);
    }

    @Override
    public byte[] get(byte[] key) throws DBException {
        byte[] newKey = concat(prefixBuffer, key);
        return this.db.get(newKey);
    }

    @Override
    public byte[] get(byte[] key, ReadOptions options) throws DBException {
        byte[] newKey = concat(prefixBuffer, key);
        return this.db.get(newKey, options);
    }

    @Override
    public DBIterator iterator() {
        return new SubLevelDBIterator(prefixBuffer, db.iterator());
    }

    @Override
    public DBIterator iterator(ReadOptions options) {
        return new SubLevelDBIterator(prefixBuffer, db.iterator(options));
    }

    @Override
    public void put(byte[] key, byte[] value) throws DBException {
        byte[] newKey = concat(prefixBuffer, key);
        this.db.put(newKey, value);
    }

    @Override
    public void delete(byte[] key) throws DBException {
        byte[] newKey = concat(prefixBuffer, key);
        this.db.delete(newKey);
    }

    @Override
    public void write(WriteBatch updates) throws DBException {
        this.db.write(updates);
    }

    @Override
    public WriteBatch createWriteBatch() {
        WriteBatch writeBatch = this.db.createWriteBatch();
        return new SubLevelDBWriteBatch(prefixBuffer, writeBatch);
    }

    @Override
    public Snapshot put(byte[] key, byte[] value, WriteOptions options) throws DBException {
        byte[] newKey = concat(prefixBuffer, key);
        return this.db.put(newKey, value, options);
    }

    @Override
    public Snapshot delete(byte[] key, WriteOptions options) throws DBException {
        byte[] newKey = concat(prefixBuffer, key);
        return this.db.delete(newKey, options);
    }

    @Override
    public Snapshot write(WriteBatch updates, WriteOptions options) throws DBException {
        return this.db.write(updates, options);
    }

    @Override
    public Snapshot getSnapshot() {
        return this.db.getSnapshot();
    }

    @Override
    public long[] getApproximateSizes(Range... ranges) {
        return db.getApproximateSizes(ranges);
    }

    @Override
    public String getProperty(String name) {
        return db.getProperty(name);
    }

    @Override
    public void suspendCompactions() throws InterruptedException {
        db.suspendCompactions();
    }

    @Override
    public void resumeCompactions() {
        db.resumeCompactions();
    }

    @Override
    public void compactRange(byte[] begin, byte[] end) throws DBException {
        db.compactRange(begin, end);
    }

    @Override
    public void close() throws IOException {

    }
}
