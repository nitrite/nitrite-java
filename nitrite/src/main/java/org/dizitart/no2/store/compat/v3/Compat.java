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

import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Nitrite v3 compatible classes used in data migration.
 *
 * @author Anindya Chatterjee
 * @since 4.0.0
 */
class Compat {
    /**
     * The enum Index type.
     */
    enum IndexType {
        /**
         * Unique index type.
         */
        Unique,
        /**
         * Non unique index type.
         */
        NonUnique,
        /**
         * Fulltext index type.
         */
        Fulltext
    }

    /**
     * The type User credential.
     */
    @Data
    static class UserCredential implements Serializable {
        private byte[] passwordHash;
        private byte[] passwordSalt;
    }

    /**
     * The type Document.
     */
    static class Document extends LinkedHashMap<String, Object> implements Serializable {
    }

    /**
     * The type Index.
     */
    @Data
    static class Index implements Serializable {
        private IndexType indexType;
        private String field;
        private String collectionName;
    }

    /**
     * The type Index meta.
     */
    @Data
    static class IndexMeta implements Serializable {
        private Index index;
        private String indexMap;
        private AtomicBoolean isDirty;
    }

    /**
     * The type Attributes.
     */
    @Data
    static class Attributes implements Serializable {
        private long createdTime;
        private long lastModifiedTime;
        private long lastSynced;
        private long syncLock;
        private long expiryWait;
        private String collection;
        private String uuid;
    }

    /**
     * The type Nitrite id.
     */
    @Data
    static class NitriteId implements Serializable {
        private Long idValue;
    }
}
