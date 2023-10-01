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

package org.dizitart.no2.mvstore.compat.v1;

import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Anindya Chatterjee
 * @since 4.0
 */
class Compat {
    enum IndexType {
        Unique,
        NonUnique,
        Fulltext
    }

    @Data
    static class UserCredential implements Serializable {
        private byte[] passwordHash;
        private byte[] passwordSalt;
    }

    static class Document extends LinkedHashMap<String, Object> {
    }

    @Data
    static class Index implements Serializable {
        private IndexType indexType;
        private String field;
        private String collectionName;
    }

    @Data
    static class IndexMeta implements Serializable {
        private Index index;
        private String indexMap;
        private AtomicBoolean isDirty;
    }

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

    @Data
    static class NitriteId implements Serializable, Comparable<NitriteId> {
        private Long idValue;

        @Override
        public int compareTo(NitriteId o) {
            return idValue.compareTo(o.idValue);
        }
    }
}
