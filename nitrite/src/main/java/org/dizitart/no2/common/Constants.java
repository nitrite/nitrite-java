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

package org.dizitart.no2.common;

import lombok.experimental.UtilityClass;
import org.dizitart.no2.NitriteId;

/**
 * Constants used in Nitrite.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 */
@UtilityClass
public class Constants {
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
     * The constant RESERVED_NAMES.
     */
    public static final String[] RESERVED_NAMES = new String[] {
        INDEX_META_PREFIX,
        INDEX_PREFIX,
        INTERNAL_NAME_SEPARATOR,
        USER_MAP,
        OBJECT_STORE_NAME_SEPARATOR,
        META_MAP_NAME,
    };

    /**
     * The constant ID_PREFIX used in {@link NitriteId#toString()}.
     */
    public static final String ID_PREFIX = "[";

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
    static final String DAEMON_THREAD_NAME = "Worker." + NO2;

    /**
     * The constant SCHEDULED_THREAD_NAME.
     */
    static final String SCHEDULED_THREAD_NAME = "ScheduledWorker." + NO2;

    /**
     * The constant TAG_COLLECTIONS.
     */
    public static final String TAG_COLLECTIONS = "collections";

    /**
     * The constant TAG_REPOSITORIES.
     */
    public static final String TAG_REPOSITORIES = "repositories";

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
     * The constant DOC_SYNCED.
     */
    public static final String DOC_SYNCED = "_synced";

    /**
     * The constant DELETED_ITEM.
     */
    public static final String DELETED_ITEM = "deletedItem";

    /**
     * The constant DELETED_ID.
     */
    public static final String DELETED_ID = DELETED_ITEM + "." + DOC_ID;

    /**
     * The constant COLLECTION.
     */
    public static final String COLLECTION = "collection";

    /**
     * The constant DELETE_TIME.
     */
    public static final String DELETE_TIME = "deleteTime";

    /**
     * The constant KEY_OBJ_SEPARATOR.
     * */
    public static final String KEY_OBJ_SEPARATOR = "+";

    /**
     * The constant HASH_ITERATIONS.
     */
    static final int HASH_ITERATIONS = 10000;

    /**
     * The constant HASH_KEY_LENGTH.
     */
    static final int HASH_KEY_LENGTH = 256;
}
