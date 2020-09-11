package org.dizitart.no2.mapdb;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.mapdb.serializers.Serializers;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
class StoreFactory {
    private StoreFactory() {}

    public static DB open(MapDBConfig dbConfig) {
        DBMaker.StoreType storeType;
        boolean defaultConfig = true;

        if (dbConfig.storeType() != null) {
            defaultConfig = false;
            switch (dbConfig.storeType()) {
                case DirectBuffer:
                    storeType = DBMaker.StoreType.directbuffer;
                    break;
                case MemoryMappedFile:
                    storeType = DBMaker.StoreType.fileMMap;
                    break;
                case RandomAccessFile:
                    storeType = DBMaker.StoreType.fileRaf;
                    break;
                case FileChannel:
                    storeType = DBMaker.StoreType.fileChannel;
                    break;
                default:
                    storeType = DBMaker.StoreType.bytearray;
                    break;
            }
        } else {
            if (StringUtils.isNullOrEmpty(dbConfig.filePath())) {
                // if no file specified, use on bytearray memory db
                storeType = DBMaker.StoreType.bytearray;
            } else {
                storeType = DBMaker.StoreType.fileRaf;
            }
        }

        DBMaker.Maker maker = new DBMaker.Maker(storeType, dbConfig.volume(),
            dbConfig.volumeExists(), dbConfig.filePath());

        if (dbConfig.allocateStartSize() != null) {
            defaultConfig = false;
            maker.allocateStartSize(dbConfig.allocateStartSize());
        }

        if (dbConfig.allocateIncrement() != null) {
            defaultConfig = false;
            maker.allocateIncrement(dbConfig.allocateIncrement());
        }

        if (dbConfig.fileDeleteAfterClose() != null && dbConfig.fileDeleteAfterClose()) {
            maker.fileDeleteAfterClose();
        }

        if (dbConfig.fileDeleteAfterOpen() != null && dbConfig.fileDeleteAfterOpen()) {
            maker.fileDeleteAfterOpen();
        }

        if (dbConfig.isThreadSafe() != null && !dbConfig.isThreadSafe()) {
            defaultConfig = false;
            maker.concurrencyDisable();
        }

        if (dbConfig.concurrencyScale() != null) {
            defaultConfig = false;
            maker.concurrencyScale(dbConfig.concurrencyScale());
        }

        if (dbConfig.cleanerHack() != null && dbConfig.cleanerHack()) {
            defaultConfig = false;
            maker.cleanerHackEnable();
        }

        if (dbConfig.fileMmapPreclearDisable() != null && dbConfig.fileMmapPreclearDisable()) {
            defaultConfig = false;
            maker.fileMmapPreclearDisable();
        }

        if (dbConfig.fileLockWait() != null) {
            defaultConfig = false;
            maker.fileLockWait(dbConfig.fileLockWait());
        }

        if (dbConfig.fileMmapfIfSupported() != null && dbConfig.fileMmapfIfSupported()) {
            defaultConfig = false;
            maker.fileMmapEnableIfSupported();
        }

        if (dbConfig.closeOnJvmShutdown() != null && dbConfig.closeOnJvmShutdown()) {
            maker.closeOnJvmShutdown();
        }

        if (dbConfig.closeOnJvmShutdownWeakReference() != null && dbConfig.closeOnJvmShutdownWeakReference()) {
            maker.closeOnJvmShutdownWeakReference();
        }

        if (dbConfig.isReadOnly() != null && dbConfig.isReadOnly()) {
            defaultConfig = false;
            if (isNullOrEmpty(dbConfig.filePath())) {
                throw new InvalidOperationException("unable create readonly in-memory database");
            }
            maker.readOnly();
        }

        if (dbConfig.checksumStoreEnable() != null && dbConfig.checksumStoreEnable()) {
            maker.checksumStoreEnable();
        }

        if (dbConfig.checksumHeaderBypass() != null && dbConfig.checksumHeaderBypass()) {
            maker.checksumHeaderBypass();
        }

        if (!StringUtils.isNullOrEmpty(dbConfig.filePath()) && defaultConfig) {
            maker.fileMmapEnableIfSupported()
                .fileMmapPreclearDisable()
                .cleanerHackEnable();
        }

        if (dbConfig.serializerRegistry() != null) {
            dbConfig.serializerRegistry().forEach(Serializers::registerSerializer);
        }

        DB db = maker.make();
        db.getStore().fileLoad();

        return db;
    }
}
