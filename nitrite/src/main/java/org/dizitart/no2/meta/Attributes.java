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

package org.dizitart.no2.meta;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

/**
 * Represents metadata attributes of a collection.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
@Getter @Setter
public class Attributes implements Serializable {
    private static final long serialVersionUID = 1481284930L;

    /**
     * The collection creation timestamp.
     *
     * @param createdTime collection creation timestamp
     * @return the collection creation timestamp.
     * */
    private long createdTime;

    /**
     * The last modified timestamp.
     *
     * @param createdTime last collection modification timestamp
     * @return the last collection modification timestamp.
     * */
    private long lastModifiedTime;

    /**
     * The last replication timestamp.
     *
     * @param createdTime last replication timestamp
     * @return the last replication timestamp.
     * */
    private long lastSynced;

    /**
     * The sync lock data of the collection.
     *
     * @param syncLock the sync lock data
     * @return the sync lock data.
     * */
    private long syncLock;

    /**
     * The sync lock expiration time in milliseconds.
     *
     * @param syncLock the sync lock expiration time in milliseconds
     * @return the sync lock expiration time in milliseconds.
     * */
    private long expiryWait;

    /**
     * The name of the collection associated with this attribute.
     *
     * @param collection the name of the collection
     * @return the name of the collection.
     * */
    private String collection;

    /**
     * The unique identifier of the collection.
     *
     * @param uuid unique identifier of the collection
     * @return the unique identifier of the collection.
     * */
    private String uuid;

    /**
     * Instantiates a new {@link Attributes}.
     */
    public Attributes(String collection) {
        this.createdTime = System.currentTimeMillis();
        this.collection = collection;
        this.uuid = UUID.randomUUID().toString();
    }

    private Attributes() {
        // constructor for jackson
    }
}
