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

package org.dizitart.no2;

import org.dizitart.no2.common.Constants;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.exceptions.InvalidIdException;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.ValidationException;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.dizitart.no2.NitriteId.createId;
import static org.dizitart.no2.NitriteId.newId;
import static org.dizitart.no2.common.Constants.*;
import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.DOC_GET_TYPE_NULL;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;
import static org.dizitart.no2.util.ValidationUtils.notNull;

/**
 * Represents a type-safe container of nitrite document. It is a collection
 * of key-value pairs.
 *
 * [[app-listing]]
 * [source,java]
 * .Example
 * --
 * include::/src/docs/asciidoc/examples/document.adoc[]
 * --
 *
 * @author Anindya Chatterjee.
 * @since 1.0
 */
public class Document extends LinkedHashMap<String, Object> implements Iterable<KeyValuePair> {
    private static final long serialVersionUID = 1477462374L;

    /**
     * Instantiates an empty document.
     */
    public Document() {
    }

    /**
     * Instantiates a new Document from a map.
     *
     * @param map the map
     */
    public Document(Map<String, Object> map) {
        super(map);
    }

    /**
     * Instantiates a new {@link Document} from a key-value pair.
     *
     * @param key   the key
     * @param value the value
     * @return the document
     */
    public static Document createDocument(final String key, final Object value) {
        Document document = new Document();
        document.put(key, value);
        return document;
    }

    /**
     * Append document with new key and value pair.
     *
     * @param key   the key
     * @param value the value
     * @return the document
     */
    @NotNull
    @Override
    public Document put(final String key, final Object value) {
        if (DOC_ID.contentEquals(key) && !validId(value)) {
            throw new InvalidOperationException(
                    errorMessage("document id is an auto generated value and can not be " + value,
                            IOE_DOC_ID_AUTO_GENERATED));
        }

        if (value != null && !Serializable.class.isAssignableFrom(value.getClass())) {
            throw new ValidationException(
                    errorMessage("type " + value.getClass().getName() + " does not implement java.io.Serializable",
                            VE_TYPE_NOT_SERIALIZABLE));
        }

        super.put(key, value);
        return this;
    }

    /**
     * Get object specified with the `key`.
     *
     * @param key the key
     * @return the object
     */
    public Object get(String key) {
        return super.get(key);
    }

    /**
     * Get object of type `T`.
     *
     * @param <T>  the type parameter
     * @param key  the key
     * @param type the type
     * @return the object of type `T`.
     */
    public <T> T get(String key, Class<T> type) {
        notNull(type, DOC_GET_TYPE_NULL);
        return type.cast(super.get(key));
    }

    /**
     * Gets the _id field of the document. If it
     * does not exists, it will create and save one.
     *
     * @return the _id field of the document.
     * @see NitriteId
     */
    @SuppressWarnings("unchecked")
    public NitriteId getId() {
        Long id = null;
        try {
            if (!containsKey(DOC_ID)) {
                id = newId().getIdValue();
                super.put(DOC_ID, id);
            } else {
                id = (Long) get(DOC_ID);
            }
            return createId(id);
        } catch (ClassCastException cce) {
            throw new InvalidIdException(errorMessage("invalid _id found " + id,
                    IIE_INVALID_ID_FOUND));
        }
    }

    /**
     * Gets the document revision number.
     *
     * @return the revision number.
     */
    public int getRevision() {
        if (!containsKey(DOC_REVISION)) {
            return 0;
        }
        return get(DOC_REVISION, Integer.class);
    }

    /**
     * Gets the source of the documents. For Nitrite replicator,
     * the value for this field will be {@link Constants#REPLICATOR}.
     *
     * @return the source of the documents.
     */
    public String getSource() {
        if (!containsKey(DOC_SOURCE)) {
            return "";
        }
        return get(DOC_SOURCE, String.class);
    }

    /**
     * Gets the last modified time of the documents.
     *
     * @return the last modified time of the documents.
     */
    public long getLastModifiedTime() {
        if (!containsKey(DOC_MODIFIED)) {
            return 0;
        }
        return get(DOC_MODIFIED, Long.class);
    }

    @NotNull
    @Override
    public Iterator<KeyValuePair> iterator() {
        return new PairIterator(super.entrySet().iterator());
    }

    private class PairIterator implements Iterator<KeyValuePair> {
        private Iterator<Map.Entry<String, Object>> iterator;

        PairIterator(Iterator<Map.Entry<String, Object>> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public KeyValuePair next() {
            Map.Entry<String, Object> next = iterator.next();
            return new KeyValuePair(next.getKey(), next.getValue());
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }

    private boolean validId(Object value) {
        return value instanceof Long;
    }
}
