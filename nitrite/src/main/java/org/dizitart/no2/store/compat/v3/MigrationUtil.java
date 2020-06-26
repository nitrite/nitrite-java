/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
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

package org.dizitart.no2.store.compat.v3;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.index.IndexEntry;
import org.dizitart.no2.store.IndexMeta;
import org.dizitart.no2.store.UserCredential;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

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
            throw new NitriteIOException("migration of old data has failed", t);
        } finally {
            oldStore.close();
            newStore.close();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void copyData(MVMap oldMap, MVMap newMap) {
        if (oldMap != null) {
            for (Object key : oldMap.keySet()) {
                Object newKey = key;
                if (key instanceof Compat.NitriteId) {
                    newKey = nitriteId((Compat.NitriteId) key);
                }
                Object value = oldMap.get(key);

                Object newValue = migrateValue(value);
                newMap.put(newKey, newValue);
            }
        }
    }

    private static Object migrateValue(Object value) {
        if (value != null) {
            if (value instanceof Compat.UserCredential) {
                return credential((Compat.UserCredential) value);
            } else if (value instanceof Compat.NitriteId) {
                return nitriteId((Compat.NitriteId) value);
            } else if (value instanceof Compat.Index) {
                return indexEntry((Compat.Index) value);
            } else if (value instanceof Compat.IndexMeta) {
                return indexMeta((Compat.IndexMeta) value);
            } else if (value instanceof Compat.Document) {
                return document((Compat.Document) value);
            } else if (value instanceof Compat.Attributes) {
                return attributes((Compat.Attributes) value);
            } else if (value instanceof ConcurrentSkipListSet) {
                return skipList((ConcurrentSkipListSet<?>) value);
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

    private static ConcurrentSkipListSet<?> skipList(ConcurrentSkipListSet<?> value) {
        ConcurrentSkipListSet<Object> newList = new ConcurrentSkipListSet<>();
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
        attributes.set(Attributes.LAST_SYNCED, Long.toString(value.getLastSynced()));
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
        IndexEntry indexEntry = indexEntry(index);

        IndexMeta indexMeta = new IndexMeta();
        indexMeta.setIndexEntry(indexEntry);
        indexMeta.setIndexMap(value.getIndexMap());
        indexMeta.setIsDirty(value.getIsDirty());

        return indexMeta;
    }

    private static IndexEntry indexEntry(Compat.Index value) {
        String indexType = value.getIndexType().name();
        return new IndexEntry(indexType, value.getField(), value.getCollectionName());
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
            throw new ValidationException("database file is corrupted");
        }
    }
}
