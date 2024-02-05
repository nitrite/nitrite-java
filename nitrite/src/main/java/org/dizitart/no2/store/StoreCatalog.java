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
     * Checks if the store catalog contains an entry with the given name.
     *
     * @param name the name
     * @return `true` if the store catalog contains an entry with the given name; `false` otherwise
     */
    public boolean hasEntry(String name) {
        for (Pair<String, Document> entry : catalogMap.entries()) {
            Document document = entry.getSecond();
            MapMetaData metaData = new MapMetaData(document);
            if (metaData.getMapNames().contains(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Writes a new entry for a collection with the given name to the store catalog.
     *
     * @param name the name of the collection to add to the catalog
     */
    public void writeCollectionEntry(String name) {
        Document document = catalogMap.get(TAG_COLLECTIONS);
        if (document == null) {
            document = Document.createDocument();
        }

        // parse the document to create collection metadata object
        MapMetaData metaData = new MapMetaData(document);
        metaData.getMapNames().add(name);

        // convert the metadata object to document and save
        catalogMap.put(TAG_COLLECTIONS, metaData.getInfo());
    }

    /**
     * Writes a repository entry with the given name to the store catalog.
     *
     * @param name the name of the repository to be added to the catalog
     */
    public void writeRepositoryEntry(String name) {
        Document document = catalogMap.get(TAG_REPOSITORIES);
        if (document == null) {
            document = Document.createDocument();
        }

        // parse the document to create collection metadata object
        MapMetaData metaData = new MapMetaData(document);
        metaData.getMapNames().add(name);

        // convert the metadata object to document and save
        catalogMap.put(TAG_REPOSITORIES, metaData.getInfo());
    }

    /**
     * Writes a keyed repository entry to the store catalog.
     *
     * @param name the name of the keyed repository to be added
     */
    public void writeKeyedRepositoryEntry(String name) {
        Document document = catalogMap.get(TAG_KEYED_REPOSITORIES);
        if (document == null) {
            document = Document.createDocument();
        }

        // parse the document to create collection metadata object
        MapMetaData metaData = new MapMetaData(document);
        metaData.getMapNames().add(name);

        catalogMap.put(TAG_KEYED_REPOSITORIES, metaData.getInfo());
    }

    /**
     * Returns a set of all collection names in the Nitrite database.
     *
     * @return a set of all collection names in the Nitrite database
     */
    public Set<String> getCollectionNames() {
        Document document = catalogMap.get(TAG_COLLECTIONS);
        if (document == null) return new HashSet<>();

        MapMetaData metaData = new MapMetaData(document);
        return metaData.getMapNames();
    }

    /**
     * Returns a set of all repository names in the Nitrite database.
     *
     * @return a set of all repository names in the Nitrite database
     */
    public Set<String> getRepositoryNames() {
        Document document = catalogMap.get(TAG_REPOSITORIES);
        if (document == null) return new HashSet<>();

        MapMetaData metaData = new MapMetaData(document);
        return metaData.getMapNames();
    }

    /**
     * Returns a set of all keyed-repository names in the Nitrite database.
     *
     * @return a set of all keyed-repository names in the Nitrite database
     */
    public Map<String, Set<String>> getKeyedRepositoryNames() {
        Document document = catalogMap.get(TAG_KEYED_REPOSITORIES);
        if (document == null) return new HashMap<>();

        MapMetaData metaData = new MapMetaData(document);
        Set<String> keyedRepositoryNames = metaData.getMapNames();

        Map<String, Set<String>> resultMap = new HashMap<>();
        for (String field : keyedRepositoryNames) {
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
     * Removes the entry from the catalog specified by a name.
     *
     * @param name the name
     */
    public void remove(String name) {
        // iterate over all types of catalog and find which type contains the name
        // remove the name from there
        for (Pair<String, Document> entry : catalogMap.entries()) {
            String catalogue = entry.getFirst();
            Document document = entry.getSecond();

            MapMetaData metaData = new MapMetaData(document);

            if (metaData.getMapNames().contains(name)) {
                metaData.getMapNames().remove(name);
                catalogMap.put(catalogue, metaData.getInfo());
                break;
            }
        }
    }
}
