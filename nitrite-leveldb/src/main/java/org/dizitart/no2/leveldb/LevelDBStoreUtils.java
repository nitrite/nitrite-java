package org.dizitart.no2.leveldb;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.store.StoreInfo;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;

import static org.dizitart.no2.common.Constants.STORE_INFO;

@Slf4j
public class LevelDBStoreUtils {
    private static final DBFactory factory;

    static StoreInfo getStoreInfo(LevelDBStore store) {
        if (store.hasMap(STORE_INFO)) {
            MVMap<String, Document> infoMap = store.openMap(STORE_INFO);
            Document document = infoMap.get(STORE_INFO);
            if (document != null) {
                return new MVStoreInfo(document);
            }
        }
        return null;
    }

    public static DB openOrCreate(String username, String password, LevelDBStoreConfig storeConfig) {
        DBFactory factory = new Iq80DBFactory();
        Options options = new Options();
        options.createIfMissing(true);
        options.logger(log::info);

        db = factory.open(new File(storeConfig.getFilePath()), options);
        return null;
    }
}
