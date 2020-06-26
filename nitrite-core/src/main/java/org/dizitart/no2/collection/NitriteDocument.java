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
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.common.util.Iterables;
import org.dizitart.no2.exceptions.InvalidIdException;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.ValidationException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;

import static org.dizitart.no2.collection.NitriteId.*;
import static org.dizitart.no2.common.Constants.*;
import static org.dizitart.no2.common.util.Iterables.listOf;
import static org.dizitart.no2.common.util.ObjectUtils.convertToObjectArray;
import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * @author Anindya Chatterjee
 */
class NitriteDocument extends LinkedHashMap<String, Object> implements Document {
    private static final long serialVersionUID = 1477462374L;
    private static final List<String> reservedFields = listOf(DOC_ID, DOC_REVISION, DOC_SOURCE, DOC_MODIFIED);

    NitriteDocument(Map<String, Object> objectMap) {
        super(objectMap);
    }

    @Override
    public Document put(String key, Object value) {
        if (DOC_ID.contentEquals(key) && !validId(value)) {
            throw new InvalidOperationException("_id is an auto generated value and cannot be set");
        }

        if (value != null && !Serializable.class.isAssignableFrom(value.getClass())) {
            throw new ValidationException("type " + value.getClass().getName()
                + " does not implement java.io.Serializable");
        }

        super.put(key, value);
        return this;
    }

    @Override
    public Object get(String key) {
        if (key != null && !containsKey(key)) {
            return deepGet(key);
        }
        return super.get(key);
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        notNull(type, "type cannot be null");
        return type.cast(get(key));
    }

    @Override
    public NitriteId getId() {
        String id;
        try {
            if (!containsKey(DOC_ID)) {
                id = newId().getIdValue();
                super.put(DOC_ID, id);
            } else {
                id = (String) get(DOC_ID);
            }
            return createId(id);
        } catch (ClassCastException cce) {
            throw new InvalidIdException("invalid _id found " + get(DOC_ID));
        }
    }

    @Override
    public Set<String> getFields() {
        return getFieldsInternal("");
    }

    @Override
    public boolean hasId() {
        return super.containsKey(DOC_ID);
    }

    @Override
    public void remove(String key) {
        super.remove(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Document clone() {
        Map<String, Object> clone = (Map<String, Object>) super.clone();
        return new NitriteDocument(clone);
    }

    @Override
    public Document merge(Document document) {
        if (document instanceof NitriteDocument) {
            super.putAll((NitriteDocument) document);
        }
        return this;
    }

    @Override
    public boolean containsKey(String key) {
        return super.containsKey(key);
    }

    @Override
    public Iterator<KeyValuePair<String, Object>> iterator() {
        return new PairIterator(super.entrySet().iterator());
    }

    private Set<String> getFieldsInternal(String prefix) {
        Set<String> fields = new HashSet<>();

        for (KeyValuePair<String, Object> entry : this) {
            if (reservedFields.contains(entry.getKey())) continue;

            Object value = entry.getValue();
            if (value instanceof NitriteDocument) {
                if (isNullOrEmpty(prefix)) {
                    fields.addAll(((NitriteDocument) value).getFieldsInternal(entry.getKey()));
                } else {
                    fields.addAll(((NitriteDocument) value).getFieldsInternal(prefix
                        + NitriteConfig.getFieldSeparator() + entry.getKey()));
                }
            } else if (!(value instanceof Iterable)) {
                if (isNullOrEmpty(prefix)) {
                    fields.add(entry.getKey());
                } else {
                    fields.add(prefix + NitriteConfig.getFieldSeparator() + entry.getKey());
                }
            }
        }
        return fields;
    }

    private Object deepGet(String field) {
        if (field.contains(NitriteConfig.getFieldSeparator())) {
            return getByEmbeddedKey(field);
        } else {
            return null;
        }
    }

    private Object getByEmbeddedKey(String embeddedKey) {
        String regex = MessageFormat.format("\\{0}", NitriteConfig.getFieldSeparator());
        String[] path = embeddedKey.split(regex);
        if (path.length < 1) {
            return null;
        }

        return recursiveGet(get(path[0]), Arrays.copyOfRange(path, 1, path.length));
    }

    @SuppressWarnings("unchecked")
    private Object recursiveGet(Object object, String[] remainingPath) {
        if (object == null) {
            return null;
        }

        if (remainingPath.length == 0) {
            return object;
        }

        if (object instanceof Document) {
            return recursiveGet(((Document) object).get(remainingPath[0]),
                Arrays.copyOfRange(remainingPath, 1, remainingPath.length));
        }

        if (object.getClass().isArray()) {
            String accessor = remainingPath[0];
            Object[] array = convertToObjectArray(object);
            if (isInteger(accessor)) {
                int index = asInteger(accessor);
                if (index < 0) {
                    throw new ValidationException("invalid array index " + index + " to access item inside a document");
                }

                if (index >= array.length) {
                    throw new ValidationException("index " + index +
                        " is not less than the size of the array " + array.length);
                }

                return recursiveGet(array[index], Arrays.copyOfRange(remainingPath, 1, remainingPath.length));
            } else {
                return decompose(listOf(array), remainingPath);
            }
        }

        if (object instanceof Iterable) {
            String accessor = remainingPath[0];
            Iterable<Object> iterable = (Iterable<Object>) object;
            List<Object> collection = Iterables.toList(iterable);
            if (isInteger(accessor)) {
                int index = asInteger(accessor);
                if (index < 0) {
                    throw new ValidationException("invalid collection index " + index + " to access item inside a document");
                }

                if (index >= collection.size()) {
                    throw new ValidationException("index " + accessor +
                        " is not less than the size of the list " + collection.size());
                }

                return recursiveGet(collection.get(index), Arrays.copyOfRange(remainingPath, 1, remainingPath.length));
            } else {
                return decompose(collection, remainingPath);
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private List<Object> decompose(List<Object> collection, String[] remainingPath) {
        Set<Object> items = new HashSet<>();
        for (Object item : collection) {
            Object value = recursiveGet(item, remainingPath);
            if (value != null) {
                if (value instanceof Iterable) {
                    List<Object> list = Iterables.toList((Iterable<Object>) value);
                    items.addAll(list);
                } else if (value.getClass().isArray()) {
                    List<Object> list = Arrays.asList(convertToObjectArray(value));
                    items.addAll(list);
                } else {
                    items.add(value);
                }
            }
        }
        return new ArrayList<>(items);
    }

    private int asInteger(String number) {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeInt(size());
        for (KeyValuePair<String, Object> keyValuePair : this) {
            stream.writeObject(keyValuePair);
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        int size = stream.readInt();
        for (int i = 0; i < size; i++) {
            KeyValuePair<String, Object> keyValuePair = (KeyValuePair<String, Object>) stream.readObject();
            super.put(keyValuePair.getKey(), keyValuePair.getValue());
        }
    }

    private static class PairIterator implements Iterator<KeyValuePair<String, Object>> {
        private Iterator<Map.Entry<String, Object>> iterator;

        PairIterator(Iterator<Map.Entry<String, Object>> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public KeyValuePair<String, Object> next() {
            Map.Entry<String, Object> next = iterator.next();
            return new KeyValuePair<>(next.getKey(), next.getValue());
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }
}
