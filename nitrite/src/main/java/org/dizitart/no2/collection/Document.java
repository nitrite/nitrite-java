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

package org.dizitart.no2.collection;

import org.dizitart.no2.common.tuples.Pair;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.dizitart.no2.common.Constants.*;

/**
 * A representation of a nitrite document.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
public interface Document extends Iterable<Pair<String, Object>>, Cloneable, Serializable {

    /**
     * Creates a new empty document.
     *
     * @return the document
     */
    static Document createDocument() {
        return new NitriteDocument();
    }

    /**
     * Creates a new document initialized with the given key/value pair.
     *
     * @param key   the key
     * @param value the value
     * @return the document
     */
    static Document createDocument(String key, Object value) {
        LinkedHashMap<String, Object> document = new LinkedHashMap<>();
        document.put(key, value);
        return new NitriteDocument(document);
    }

    /**
     * Creates a new document initialized with the given map.
     *
     * @param documentMap the map
     * @return the document
     */
    static Document createDocument(Map<String, Object> documentMap) {
        LinkedHashMap<String, Object> document = new LinkedHashMap<>(documentMap);
        return new NitriteDocument(document);
    }

    /**
     * Associates the specified value with the specified key in this document.
     * <p>
     * NOTE: An embedded field is also supported.
     * </p>
     *
     * @param key   the key
     * @param value the value
     * @return the document
     */
    Document put(final String key, final Object value);

    /**
     * Returns the value to which the specified key is associated with,
     * or null if this document contains no mapping for the key.
     *
     * @param key the key
     * @return the object
     */
    Object get(String key);

    /**
     * Returns the value of type {@code <T>} to which the specified
     * key is associated, or null if this document contains no mapping
     * for the key.
     *
     * @param <T>  the type parameter
     * @param key  the key
     * @param type the type
     * @return the value
     */
    <T> T get(String key, Class<T> type);

    /**
     * Return the nitrite id associated with this document.
     *
     * @return the nitrite id
     */
    NitriteId getId();

    /**
     * Retrieves all fields (top level and embedded) associated
     * with this document.
     *
     * @return the fields
     */
    Set<String> getFields();

    /**
     * Checks if this document has a nitrite id.
     *
     * @return the boolean
     */
    boolean hasId();

    /**
     * Removes the key and its value from the document.
     *
     * @param key the key
     */
    void remove(String key);

    /**
     * Creates and returns a copy of this document.
     *
     * @return document
     * */
    Document clone();

    /**
     * Returns the number of entries in the document.
     *
     * @return the int
     */
    int size();

    /**
     * Merges a document in this document.
     *
     * @param update the update
     * @return the document
     */
    Document merge(Document update);

    /**
     * Checks if a key exists in the document.
     *
     * @param key the key
     * @return the boolean
     */
    boolean containsKey(String key);

    /**
     * Gets the document revision number.
     *
     * @return the revision
     */
    default Integer getRevision() {
        if (!containsKey(DOC_REVISION)) {
            return 0;
        }
        return get(DOC_REVISION, Integer.class);
    }

    /**
     * Gets the source of this document.
     *
     * @return the source
     */
    default String getSource() {
        if (!containsKey(DOC_SOURCE)) {
            return "";
        }
        return get(DOC_SOURCE, String.class);
    }

    /**
     * Gets last modified time of this document since epoch.
     *
     * @return the last modified since epoch
     */
    default Long getLastModifiedSinceEpoch() {
        if (!containsKey(DOC_MODIFIED)) {
            return 0L;
        }
        return get(DOC_MODIFIED, Long.class);
    }
}
