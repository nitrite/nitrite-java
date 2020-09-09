package org.dizitart.no2.rocksdb;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.rocksdb.formatter.ObjectFormatter;
import org.dizitart.no2.store.StoreMetadata;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDBException;

import java.io.File;

import static org.dizitart.no2.common.Constants.*;
import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;

@Slf4j
class RocksDBStoreUtils {
    private RocksDBStoreUtils() {
    }

    public static RocksDBReference openOrCreate(String username, String password, RocksDBConfig storeConfig) {
        RocksDBReference db;
        File dbFile;
        if (!isNullOrEmpty(storeConfig.filePath())) {
            dbFile = new File(storeConfig.filePath());
            if (dbFile.exists()) {
                db = StoreFactory.openSecurely(storeConfig, username, password);
            } else {
                db = StoreFactory.createSecurely(storeConfig, username, password);
                writeStoreInfo(db, storeConfig.objectFormatter());
            }
        } else {
            throw new InvalidOperationException("nitrite rocksdb store does not support in-memory database");
        }
        return db;
    }

    static StoreMetadata getStoreInfo(RocksDBReference reference, ObjectFormatter objectFormatter) {
        try {
            ColumnFamilyHandle storeInfo = reference.getOrCreateColumnFamily(STORE_INFO);
            byte[] key = objectFormatter.encode(Constants.STORE_INFO_KEY);
            byte[] value = reference.getRocksDB().get(storeInfo, key);

            Document document = objectFormatter.decode(value, Document.class);
            if (document != null) {
                return new StoreMetadata(document);
            }
        } catch (RocksDBException e) {
            log.error("Error while retrieving store info", e);
            throw new NitriteIOException("failed to read store info", e);
        }

        return null;
    }

    static void updateStoreInfo(RocksDBReference reference,
                                ObjectFormatter objectFormatter,
                                StoreMetadata storeMetadata) {
        if (storeMetadata != null) {
            try {
                Document document = storeMetadata.getInfo();

                byte[] key = objectFormatter.encode(Constants.STORE_INFO_KEY);
                byte[] value = objectFormatter.encode(document);

                ColumnFamilyHandle storeInfo = reference.getOrCreateColumnFamily(STORE_INFO);
                reference.getRocksDB().put(storeInfo, key, value);
            } catch (RocksDBException e) {
                log.error("Error while updating store info", e);
                throw new NitriteIOException("failed to update nitrite info", e);
            }
        }
    }

    private static void writeStoreInfo(RocksDBReference reference, ObjectFormatter objectFormatter) {
        try {
            StoreMetadata metadata = new StoreMetadata();
            metadata.setCreateTime(System.currentTimeMillis());
            metadata.setStoreVersion("RocksDB/" + getRocksDbVersion());
            metadata.setNitriteVersion(NITRITE_VERSION);
            metadata.setDatabaseRevision(INITIAL_REVISION);

            Document document = metadata.getInfo();

            byte[] key = objectFormatter.encode(Constants.STORE_INFO_KEY);
            byte[] value = objectFormatter.encode(document);

            ColumnFamilyHandle storeInfo = reference.getOrCreateColumnFamily(STORE_INFO);
            reference.getRocksDB().put(storeInfo, key, value);
        } catch (RocksDBException e) {
            log.error("Error while writing store info", e);
            throw new NitriteIOException("failed to write nitrite info", e);
        }
    }

    private static String getRocksDbVersion() {
        return "6.11.4";
    }
}
