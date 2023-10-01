package org.dizitart.no2.rocksdb;

import org.dizitart.no2.exceptions.InvalidOperationException;

import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;

/**
 * @since 4.0
 * @author Anindya Chatterjee
 */
class RocksDBStoreUtils {
    private RocksDBStoreUtils() {
    }

    public static RocksDBReference openOrCreate(RocksDBConfig storeConfig) {
        RocksDBReference db;
        if (!isNullOrEmpty(storeConfig.filePath())) {
            db = StoreFactory.createDBReference(storeConfig);
        } else {
            throw new InvalidOperationException("Nitrite rocksdb store does not support in-memory database");
        }
        return db;
    }
}
