package org.dizitart.no2.mapdb;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.store.StoreInfo;
import org.mapdb.BTreeMap;
import org.mapdb.DB;

import java.io.File;

import static org.dizitart.no2.common.Constants.*;
import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
class MapDBStoreUtils {
    private MapDBStoreUtils() {}

    static DB openOrCreate(String username, String password, MapDBConfig storeConfig) {

        DB db;
        File dbFile;
        if (!isNullOrEmpty(storeConfig.filePath())) {
            dbFile = new File(storeConfig.filePath());
            if (dbFile.exists()) {
                db = StoreFactory.openSecurely(storeConfig, username, password);
            } else {
                db = StoreFactory.createSecurely(storeConfig, username, password);
                writeStoreInfo(db);
            }
        } else {
            db = StoreFactory.createSecurely(storeConfig, username, password);
        }
        return db;
    }

    @SuppressWarnings("unchecked")
    static StoreInfo getStoreInfo(DB store) {
        if (store.exists(STORE_INFO)) {
            BTreeMap<String, Document> infoMap = (BTreeMap<String, Document>) store.treeMap(STORE_INFO)
                .counterEnable()
                .valuesOutsideNodesEnable()
                .createOrOpen();

            Document document = infoMap.get(STORE_INFO);
            if (document != null) {
                return new StoreInfo(document);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static void writeStoreInfo(DB store) {
        try {
            Document document = Document.createDocument();
            document.put(CREATE_TIME, System.currentTimeMillis());
            document.put(FILE_STORE, "MapDB/" + getMapDbVersion());
            document.put(STORE_VERSION, NITRITE_VERSION);

            BTreeMap<String, Document> infoMap = (BTreeMap<String, Document>) store.treeMap(STORE_INFO)
                .counterEnable()
                .valuesOutsideNodesEnable()
                .createOrOpen();

            infoMap.put(STORE_INFO, document);
        } finally {
            store.commit();
        }
    }

    private static String getMapDbVersion() {
        return "3.0.8";
    }
}
