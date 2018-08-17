/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.ui;

import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.dizitart.no2.Document;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.Cursor;
import org.dizitart.no2.collection.Filter;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.dizitart.no2.common.Constants.*;
import static org.dizitart.no2.filters.Filters.*;
import static org.dizitart.no2.util.StringUtils.isNullOrEmpty;


/**
 * @author Anindya Chatterjee.
 */
class NitriteHelper {
    private NitriteStore dbStore;

    NitriteHelper(Nitrite db) {
        try {
            Field storeField = db.getClass().getDeclaredField("store");
            storeField.setAccessible(true);
            dbStore = (NitriteStore) storeField.get(db);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    List<TreeItem<String>> getCollections() {
        Set<String> mapNames = dbStore.getMapNames();

        List<TreeItem<String>> treeItems = new ArrayList<>();
        mapNames.stream().filter(this::isCollection)
                .forEachOrdered(name -> {
                    TreeItem<String> collectionItem = new TreeItem<>(name, getIcon("collection.png"));
                    if (!name.contains(META_MAP_NAME)) {

                        TreeItem<String> indicesItem = new TreeItem<>("Indices");
                        indicesItem.getChildren().addAll(getIndices(mapNames, name));

                        TreeItem<String> metaItem = new TreeItem<>("Metadata");
                        metaItem.getChildren().add(getMeta(mapNames, name));

                        collectionItem.getChildren().add(indicesItem);
                        collectionItem.getChildren().add(metaItem);
                    }
                    treeItems.add(collectionItem);
                });
        return treeItems;
    }

    private TreeItem<String> getMeta(Set<String> mapNames, String name) {
        for (String mapName : mapNames) {
            if (mapName.contains(INDEX_META_PREFIX)
                    && !mapName.startsWith(META_MAP_NAME)
                    && mapName.contains(name)) {
                return new TreeItem<>(mapName, getIcon("meta.png"));
            }
        }
        return null;
    }

    private List<TreeItem<String>> getIndices(Set<String> mapNames, String collectionName) {
        return mapNames.stream().filter(mapName -> mapName.contains(INDEX_PREFIX)
                && mapName.contains(collectionName)
                && !mapName.startsWith(META_MAP_NAME)
                && !mapName.contains(INDEX_META_PREFIX)).map((value) ->
                new TreeItem<>(value, getIcon("index.png"))).collect(Collectors.toList());
    }

    NitriteMap<?, ?> getMap(String mapName) {
        if (dbStore != null && dbStore.hasMap(mapName)) {
            return dbStore.openMap(mapName);
        }
        return null;
    }

    boolean isCollection(String mapName) {
        return !mapName.contains(INDEX_META_PREFIX)
                && !mapName.contains(INDEX_PREFIX + INTERNAL_NAME_SEPARATOR);
    }

    static ImageView getIcon(String name) {
        return new ImageView(new Image(NitriteHelper.class.getClassLoader().getResourceAsStream(name)));
    }

    Filter getFilter(String key, String term, String type) {
        if (!isNullOrEmpty(type)) {
            if (type.equalsIgnoreCase("Equals")) {
                return eq(key, term);
            } else if (type.equalsIgnoreCase("Greater Than")) {
                return gt(key, term);
            } else if (type.equalsIgnoreCase("Greater Equal")) {
                return gte(key, term);
            } else if (type.equalsIgnoreCase("Lesser Than")) {
                return lt(key, term);
            } else if (type.equalsIgnoreCase("Lesser Equal")) {
                return lte(key, term);
            } else if (type.equalsIgnoreCase("Text")) {
                return text(key, term);
            }
        }
        return null;
    }

    List<NitriteId> getResultList(NitriteCollection collection, Filter filter) {
        Cursor cursor = collection.find(filter);
        List<NitriteId> list = new ArrayList<>();
        for (Document document : cursor) {
            NitriteId id = document.getId();
            list.add(id);
        }
        return list;
    }
}
