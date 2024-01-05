/*
 * Copyright (c) 2017-2022 Nitrite author or authors.
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

package org.dizitart.no2.common.meta;

import lombok.Data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents metadata attributes of a {@link org.dizitart.no2.store.NitriteMap}.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 */
@Data
public class Attributes implements Serializable {
    private static final long serialVersionUID = 1481284930L;

    /**
     * The constant CREATED_TIME.
     */
    public static final String CREATED_TIME = "created_at";

    /**
     * The constant LAST_MODIFIED_TIME.
     */
    public static final String LAST_MODIFIED_TIME = "last_modified_at";

    /**
     * The constant OWNER.
     */
    public static final String OWNER = "owner";

    /**
     * The constant UNIQUE_ID.
     */
    public static final String UNIQUE_ID = "uuid";


    private Map<String, String> attributes;

    /**
     * Instantiates a new Attributes.
     */
    public Attributes() {
        attributes = new ConcurrentHashMap<>();
        set(CREATED_TIME, Long.toString(System.currentTimeMillis()));
        set(UNIQUE_ID, UUID.randomUUID().toString());
    }

    /**
     * Instantiates a new Attributes.
     *
     * @param collection the collection
     */
    public Attributes(String collection) {
        attributes = new ConcurrentHashMap<>();
        set(OWNER, collection);
        set(CREATED_TIME, Long.toString(System.currentTimeMillis()));
        set(UNIQUE_ID, UUID.randomUUID().toString());
    }


    /**
     * Adds a key-value pair to the attributes and returns the updated
     * {@link Attributes} object.
     * 
     * @param key The key is a string that represents the attribute name. It is used to identify the
     * attribute in the attributes.
     * @param value The value parameter is a string that represents the value to be associated with the
     * given key in the attributes.
     * @return The method is returning an instance of the Attributes class.
     */
    public Attributes set(String key, String value) {
        attributes.put(LAST_MODIFIED_TIME, Long.toString(System.currentTimeMillis()));
        attributes.put(key, value);
        return this;
    }

    /**
     * Retrieves the value associated with a given key from a {@link Attributes}.
     * 
     * @param key The "key" parameter is a String that represents the key of the attribute that you
     * want to retrieve from the attributes.
     * @return The method is returning the value associated with the given key in the attributes.
     */
    public String get(String key) {
        return attributes.get(key);
    }

    /**
     * Check whether a key exists in the attributes.
     *
     * @param key the key
     * @return the boolean
     */
    public boolean hasKey(String key) {
        return attributes.containsKey(key);
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(attributes);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        attributes = (Map<String, String>) stream.readObject();
    }
}
