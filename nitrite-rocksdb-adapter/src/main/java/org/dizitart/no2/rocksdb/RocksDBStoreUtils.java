package org.dizitart.no2.rocksdb;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.exceptions.InvalidOperationException;

import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;

@Slf4j
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
