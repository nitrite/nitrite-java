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

package org.dizitart.no2.common;

import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.exceptions.NitriteIOException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Constants used in Nitrite.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 */
public class Constants {
    private Constants() {
    }

    static {
        String v = "unknown";
        try (InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("version")) {
            if (is != null) {
                try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                    try (BufferedReader bufferedReader = new BufferedReader(reader)) {
                        v = bufferedReader.readLine();
                    }
                }
            }
        } catch (IOException e) {
            throw new NitriteIOException("Failed to load version information", e);
        }
        NITRITE_VERSION = v;
    }

    /**
     * The constant NITRITE_VERSION.
     */
    public static final String NITRITE_VERSION;

    /**
     * The constant INDEX_META_PREFIX.
     */
    public static final String INDEX_META_PREFIX = "$nitrite_index_meta";

    /**
     * The constant INDEX_PREFIX.
     */
    public static final String INDEX_PREFIX = "$nitrite_index";

    /**
     * The constant INTERNAL_NAME_SEPARATOR.
     */
    public static final String INTERNAL_NAME_SEPARATOR = "|";

    /**
     * The constant USER_MAP.
     */
    public static final String USER_MAP = "$nitrite_users";

    /**
     * The constant OBJECT_STORE_NAME_SEPARATOR.
     */
    public static final String OBJECT_STORE_NAME_SEPARATOR = ":";

    /**
     * The constant META_MAP_NAME.
     */
    public static final String META_MAP_NAME = "$nitrite_meta_map";

    /**
     * The constant STORE_INFO.
     */
    public static final String STORE_INFO = "$nitrite_store_info";

    /**
     * The constant COLLECTION_CATALOG.
     */
    public static final String COLLECTION_CATALOG = "$nitrite_catalog";

    /**
     * The constant KEY_OBJ_SEPARATOR.
     */
    public static final String KEY_OBJ_SEPARATOR = "+";

    /**
     * The constant RESERVED_NAMES.
     */
    public static final List<String> RESERVED_NAMES = Arrays.asList(
            INDEX_META_PREFIX,
            INDEX_PREFIX,
            INTERNAL_NAME_SEPARATOR,
            USER_MAP,
            OBJECT_STORE_NAME_SEPARATOR,
            META_MAP_NAME,
            STORE_INFO,
            COLLECTION_CATALOG,
            KEY_OBJ_SEPARATOR
    );

    /**
     * The constant ID_PREFIX used in {@link NitriteId#toString()}.
     */
    public static final String ID_PREFIX = "[";

    /**
     * The constant TAG_COLLECTIONS.
     */
    public static final String TAG_COLLECTIONS = "collections";

    /**
     * The constant TAG_REPOSITORIES.
     */
    public static final String TAG_REPOSITORIES = "repositories";

    /**
     * The constant TAG_KEYED_REPOSITORIES.
     */
    public static final String TAG_KEYED_REPOSITORIES = "keyed-repositories";

    /**
     * The constant TAG_COLLECTION_METADATA.
     */
    public static final String TAG_MAP_METADATA = "mapNames";

    /**
     * The constant TAG_TYPE.
     */
    public static final String TAG_TYPE = "type";

    /**
     * The constant TAG_INDICES.
     */
    public static final String TAG_INDICES = "indices";

    /**
     * The constant TAG_INDEX.
     */
    public static final String TAG_INDEX = "index";

    /**
     * The constant TAG_DATA.
     */
    public static final String TAG_DATA = "data";

    /**
     * The constant TAG_NAME.
     */
    public static final String TAG_NAME = "name";

    /**
     * The constant TAG_KEY.
     */
    public static final String TAG_KEY = "key";

    /**
     * The constant TAG_VALUE.
     */
    public static final String TAG_VALUE = "value";

    /**
     * The constant DOC_ID denotes the '_id' field in a document.
     */
    public static final String DOC_ID = "_id";

    /**
     * The constant DOC_CREATED.
     */
    public static final String DOC_REVISION = "_revision";

    /**
     * The constant DOC_MODIFIED.
     */
    public static final String DOC_MODIFIED = "_modified";

    /**
     * The constant DOC_SOURCE.
     */
    public static final String DOC_SOURCE = "_source";

    /**
     * The constant HASH_ITERATIONS.
     */
    public static final int HASH_ITERATIONS = 10000;

    /**
     * The constant HASH_KEY_LENGTH.
     */
    public static final int HASH_KEY_LENGTH = 256;

    /**
     * The constant NO2.
     */
    private static final String NO2 = "NO\u2082";

    /**
     * The constant ID_SUFFIX used in {@link NitriteId#toString()}.
     */
    public static final String ID_SUFFIX = "]" + NO2;

    /**
     * The constant REPLICATOR.
     */
    public static final String REPLICATOR = "Replicator." + NO2;

    /**
     * The constant DAEMON_THREAD_NAME.
     */
    public static final String DAEMON_THREAD_NAME = "Worker." + NO2;

    /**
     * The constant DAEMON_THREAD_NAME.
     */
    public static final String SYNC_THREAD_NAME = "Sync." + NO2;

    /**
     * The constant INITIAL_REVISION.
     */
    public static final Integer INITIAL_SCHEMA_VERSION = 1;
}
