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

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.common.tuples.Pair;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.dizitart.no2.common.Constants.*;

/**
 * Represents a document in Nitrite database.
 * <p>
 * Nitrite document are composed of key-value pairs. A key is always a {@link String} and value
 * can be anything including <code>null</code>.
 * <p>
 * Nitrite document supports nested documents as well. The key of a nested document is a {@link String}
 * separated by {@link NitriteConfig#getFieldSeparator()}. By default, Nitrite uses `.` as field separator.
 * This can be changed by setting {@link NitriteConfig#fieldSeparator(String)}.
 * <p>
 * For example, if a document has a nested document
 * <code>{ "a" : { "b" : 1 } }</code>, then the value of inside the nested document can be retrieved by
 * calling {@link #get(String)} with key <code>a.b</code>.
 * <p>
 * Below fields are reserved and cannot be used as key in a document.
 * <ul>
 *     <li><b>_id</b>: The unique identifier of the document. If not provided,
 *     Nitrite will generate a unique {@link NitriteId} for the document during insertion.</li>
 *     <li><b>_revision</b>: The revision number of the document.</li>
 *     <li><b>_source</b>: The source of the document.</li>
 *     <li><b>_modified</b>: The last modified time of the document in milliseconds since epoch.</li>
 * </ul>
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
public interface Document extends Iterable<Pair<String, Object>>, Cloneable, Serializable {

    /**
     * Creates an empty document.
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
     * Associates the specified value with the specified key in this document.
     * <p>
     * NOTE: An embedded field is also supported.
     * </p>
     *
     * @param key                  the key
     * @param value                the value
     * @param ignoreFieldSeparator if set to {@code true}, it will ignore the field separator
     * @return the document
     */
    Document put(final String key, final Object value, boolean ignoreFieldSeparator);

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
     * <p>
     * NOTE: This method may not work for fields containing primitive types.
     * Use {@link #get(String)} instead.
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
     * Checks if a top level key exists in the document.
     *
     * @param key the key
     * @return the boolean
     */
    boolean containsKey(String key);

    /**
     * Checks if a top level field or embedded field exists in the document.
     *
     * @param field the field
     * @return the boolean
     */
    boolean containsField(String field);

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
