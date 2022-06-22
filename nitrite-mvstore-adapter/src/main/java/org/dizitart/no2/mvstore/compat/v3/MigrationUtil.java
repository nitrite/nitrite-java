/*
 * Copyright (c) 2019-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.mvstore.compat.v3;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.meta.Attributes;
import org.dizitart.no2.common.DBNull;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.common.DBValue;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.IndexMeta;
import org.dizitart.no2.store.UserCredential;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.dizitart.no2.common.Constants.INDEX_PREFIX;
import static org.dizitart.no2.common.Constants.STORE_INFO;
import static org.dizitart.no2.common.util.ObjectUtils.convertToObjectArray;

/**
 * An utility class to migrate the.
 *
 * @since 4.0.0
 * @author Anindya Chatterjee
 */
public class MigrationUtil {

    /**
     * Migrate an old 3.x compatible store to new 4.x compatible store.
     *
     * @param newStore the new store
     * @param oldStore the old store
     */
    @SuppressWarnings({"rawtypes"})
    public static void migrate(MVStore newStore, MVStore oldStore) {
        try {
            validateOldStore(oldStore);

            Set<String> mapNames = oldStore.getMapNames();
            for (String mapName : mapNames) {
                MVMap oldMap = oldStore.openMap(mapName, new MVMapBuilder<>());
                MVMap newMap = newStore.openMap(mapName);
                copyData(oldMap, newMap);
            }

            oldStore.commit();
            newStore.commit();
        } catch (Throwable t) {
            throw new NitriteIOException("Migration of old data has failed", t);
        } finally {
            oldStore.close();
            newStore.close();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void copyData(MVMap oldMap, MVMap newMap) {
        if (oldMap != null) {
            Set<Map.Entry> entrySet = oldMap.entrySet();
            for (Map.Entry entry : entrySet) {
                Object key = entry.getKey();
                Object newKey = entry.getKey();

                if (key instanceof Compat.NitriteId) {
                    newKey = nitriteId((Compat.NitriteId) key);
                } else if (oldMap.getName().contains(INDEX_PREFIX)) {
                    // index map, wrap with DBValue
                    newKey = newKey == null ? DBNull.getInstance() : new DBValue((Comparable<?>) newKey);
                }

                Object newValue = migrateValue(entry.getValue());
                newMap.put(newKey, newValue);
            }
        }
    }

    private static Object migrateValue(Object value) {
        if (value != null) {
            if (value instanceof Compat.UserCredential) {
                // old user credentials
                return credential((Compat.UserCredential) value);
            } else if (value instanceof Compat.NitriteId) {
                // old nitrite id
                return nitriteId((Compat.NitriteId) value);
            } else if (value instanceof Compat.Index) {
                // old index entry
                return indexEntry((Compat.Index) value);
            } else if (value instanceof Compat.IndexMeta) {
                // old index meta data
                return indexMeta((Compat.IndexMeta) value);
            } else if (value instanceof Compat.Document) {
                // old document
                return document((Compat.Document) value);
            } else if (value instanceof Compat.Attributes) {
                // old attribute
                return attributes((Compat.Attributes) value);
            } else if (value instanceof ConcurrentSkipListSet) {
                // old index nitrite id list
                return arrayList((ConcurrentSkipListSet<?>) value);
            } else if (value instanceof Iterable) {
                return iterable((Iterable<?>) value);
            } else if (value.getClass().isArray()) {
                return array(convertToObjectArray(value));
            }
            return value;
        }
        return null;
    }

    private static Object[] array(Object[] array) {
        Object[] newArray = new Object[array.length];
        for (int i = 0; i < array.length; i++) {
            newArray[i] = migrateValue(array[i]);
        }
        return newArray;
    }

    private static Iterable<?> iterable(Iterable<?> value) {
        Collection<Object> collection = null;
        if (value instanceof List) {
            collection = new ArrayList<>();
        } else if (value instanceof Set) {
            collection = new HashSet<>();
        }

        if (collection != null) {
            for (Object object : value) {
                Object newValue = migrateValue(object);
                collection.add(newValue);
            }
        }

        return collection;
    }

    private static CopyOnWriteArrayList<?> arrayList(ConcurrentSkipListSet<?> value) {
        CopyOnWriteArrayList<Object> newList = new CopyOnWriteArrayList<>();
        for (Object object : value) {
            Object newValue = migrateValue(object);
            newList.add(newValue);
        }
        return newList;
    }

    private static Attributes attributes(Compat.Attributes value) {
        Attributes attributes = new Attributes();
        attributes.set(Attributes.CREATED_TIME, Long.toString(value.getCreatedTime()));
        attributes.set(Attributes.LAST_MODIFIED_TIME, Long.toString(value.getLastModifiedTime()));
        attributes.set(Attributes.LOCAL_COLLECTION_MARKER, Long.toString(value.getLastSynced()));
        attributes.set(Attributes.SYNC_LOCK, Long.toString(value.getSyncLock()));
        attributes.set(Attributes.EXPIRY_WAIT, Long.toString(value.getExpiryWait()));
        if (value.getCollection() != null) {
            attributes.set(Attributes.OWNER, value.getCollection());
        }

        if (value.getUuid() != null) {
            attributes.set(Attributes.UNIQUE_ID, value.getUuid());
        }
        return attributes;
    }

    private static Document document(Compat.Document value) {
        Document document = Document.createDocument();
        for (Map.Entry<String, Object> entry : value.entrySet()) {
            Object val = entry.getValue();
            Object migratedVal = migrateValue(val);
            document.put(entry.getKey(), migratedVal);
        }
        return document;
    }

    private static IndexMeta indexMeta(Compat.IndexMeta value) {
        Compat.Index index = value.getIndex();
        IndexDescriptor indexDescriptor = indexEntry(index);

        IndexMeta indexMeta = new IndexMeta();
        indexMeta.setIndexDescriptor(indexDescriptor);
        indexMeta.setIndexMap(value.getIndexMap());
        indexMeta.setIsDirty(value.getIsDirty());

        return indexMeta;
    }

    private static IndexDescriptor indexEntry(Compat.Index value) {
        String indexType = value.getIndexType().name();
        return new IndexDescriptor(indexType, Fields.withNames(value.getField()), value.getCollectionName());
    }

    private static NitriteId nitriteId(Compat.NitriteId value) {
        return NitriteId.createId(Long.toString(value.getIdValue()));
    }

    private static UserCredential credential(Compat.UserCredential value) {
        UserCredential userCredential = new UserCredential();
        userCredential.setPasswordHash(value.getPasswordHash());
        userCredential.setPasswordSalt(value.getPasswordSalt());
        return userCredential;
    }

    private static void validateOldStore(MVStore store) {
        if (store.hasMap(STORE_INFO)) {
            throw new ValidationException("Database file is corrupted");
        }
    }
}
