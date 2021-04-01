/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.store;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.tuples.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.dizitart.no2.common.Constants.*;
import static org.dizitart.no2.common.util.ObjectUtils.getKeyName;
import static org.dizitart.no2.common.util.ObjectUtils.getKeyedRepositoryType;

/**
 * The nitrite store catalog containing the name of all collections,
 * repositories and keyed-repositories.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
public class StoreCatalog {
    private final NitriteMap<String, Document> catalogMap;

    /**
     * Instantiates a new {@link StoreCatalog}.
     *
     * @param nitriteStore the nitrite store
     */
    public StoreCatalog(NitriteStore<?> nitriteStore) {
        this.catalogMap = nitriteStore.openMap(COLLECTION_CATALOG, String.class, Document.class);
    }

    /**
     * Writes a new collection entry to the catalog.
     *
     * @param name the name
     */
    public void writeCollectionEntry(String name) {
        Document document = catalogMap.get(TAG_COLLECTIONS);
        if (document == null) document = Document.createDocument();
        document.put(name, true);
        catalogMap.put(TAG_COLLECTIONS, document);
    }

    /**
     * Writes a repository entry to the catalog.
     *
     * @param name the name
     */
    public void writeRepositoryEntry(String name) {
        Document document = catalogMap.get(TAG_REPOSITORIES);
        if (document == null) document = Document.createDocument();
        document.put(name, true);
        catalogMap.put(TAG_REPOSITORIES, document);
    }

    /**
     * Writes a keyed repository entries to the catalog
     *
     * @param name the name
     */
    public void writeKeyedRepositoryEntries(String name) {
        Document document = catalogMap.get(TAG_KEYED_REPOSITORIES);
        if (document == null) document = Document.createDocument();
        document.put(name, true);
        catalogMap.put(TAG_KEYED_REPOSITORIES, document);
    }

    /**
     * Gets all collection names.
     *
     * @return the collection names
     */
    public Set<String> getCollectionNames() {
        Document document = catalogMap.get(TAG_COLLECTIONS);
        if (document == null) return new HashSet<>();

        return document.getFields();
    }

    /**
     * Gets all repository names.
     *
     * @return the repository names
     */
    public Set<String> getRepositoryNames() {
        Document document = catalogMap.get(TAG_REPOSITORIES);
        if (document == null) return new HashSet<>();

        return document.getFields();
    }

    /**
     * Gets all keyed repository names.
     *
     * @return the keyed repository names
     */
    public Map<String, Set<String>> getKeyedRepositoryNames() {
        Document document = catalogMap.get(TAG_KEYED_REPOSITORIES);
        if (document == null) return new HashMap<>();

        Map<String, Set<String>> resultMap = new HashMap<>();
        for (String field : document.getFields()) {
            String key = getKeyName(field);
            String type = getKeyedRepositoryType(field);

            Set<String> types;
            if (resultMap.containsKey(key)) {
                types = resultMap.get(key);
            } else {
                types = new HashSet<>();
            }
            types.add(type);
            resultMap.put(key, types);
        }
        return resultMap;
    }

    /**
     * Removes the entry from the catalog specified by name.
     *
     * @param name the name
     */
    public void remove(String name) {
        // iterate over all types of catalog and find which type contains the name
        // remove the name from there
        for (Pair<String, Document> entry : catalogMap.entries()) {
            String catalogue = entry.getFirst();
            Document document = entry.getSecond();

            if (document.containsKey(name)) {
                document.remove(name);
                catalogMap.put(catalogue, document);
                break;
            }
        }
    }
}
