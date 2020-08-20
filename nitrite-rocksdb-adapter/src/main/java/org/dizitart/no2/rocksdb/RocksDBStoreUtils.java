package org.dizitart.no2.rocksdb;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.rocksdb.formatter.ObjectFormatter;
import org.dizitart.no2.store.StoreInfo;
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

    static StoreInfo getStoreInfo(RocksDBReference reference, ObjectFormatter objectFormatter) {
        try {
            ColumnFamilyHandle storeInfo = reference.getOrCreateColumnFamily(STORE_INFO);
            byte[] key = objectFormatter.encode(Constants.STORE_INFO_KEY);
            byte[] value = reference.getRocksDB().get(storeInfo, key);

            Document document = objectFormatter.decode(value, Document.class);
            if (document != null) {
                return new StoreInfo(document);
            }
        } catch (RocksDBException e) {
            log.error("Error while retrieving store info", e);
            throw new NitriteIOException("failed to read store info", e);
        }

        return null;
    }

    private static void writeStoreInfo(RocksDBReference reference, ObjectFormatter objectFormatter) {
        try {
            ColumnFamilyHandle storeInfo = reference.getOrCreateColumnFamily(STORE_INFO);

            Document document = Document.createDocument();
            document.put(CREATE_TIME, System.currentTimeMillis());
            document.put(FILE_STORE, "RocksDB/6.11.4");
            document.put(STORE_VERSION, NITRITE_VERSION);

            byte[] key = objectFormatter.encode(Constants.STORE_INFO_KEY);
            byte[] value = objectFormatter.encode(document);
            reference.getRocksDB().put(storeInfo, key, value);
        } catch (RocksDBException e) {
            log.error("Error while writing store info", e);
            throw new NitriteIOException("failed to write nitrite info", e);
        }
    }
}
