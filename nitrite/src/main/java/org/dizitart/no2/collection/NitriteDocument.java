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
 * @since 4.0
 */
class NitriteDocument extends LinkedHashMap<String, Object> implements Document {
    private static final long serialVersionUID = 1477462374L;
    private static final List<String> reservedFields = listOf(DOC_ID, DOC_REVISION, DOC_SOURCE, DOC_MODIFIED);

    NitriteDocument() {
        super();
    }

    NitriteDocument(Map<String, Object> objectMap) {
        super(objectMap);
    }

    private static void validateField(String field, Object value) {
        // field name cannot be empty or null
        if (isNullOrEmpty(field)) {
            throw new InvalidOperationException("Document does not support empty or null key");
        }

        // field name cannot be empty or null
        if (DOC_ID.contentEquals(field) && !validId(value)) {
            throw new InvalidOperationException("_id is an auto generated value and cannot be set");
        }

        // value must be serializable
        if (value != null && !Serializable.class.isAssignableFrom(value.getClass())) {
            throw new ValidationException("Type " + value.getClass().getName()
                + " does not implement java.io.Serializable");
        }
    }

    @Override
    public Document put(String key, Object value, boolean ignoreFieldSeparator) {
        if (ignoreFieldSeparator) {
            super.put(key, value);
            return this;
        } else {
            return put(key, value);
        }
    }

    @Override
    public Document put(String field, Object value) {
        validateField(field, value);

        // if field name contains field separator, split the fields, and put the value
        // accordingly associated with th embedded field.
        if (isEmbedded(field)) {
            String regex = MessageFormat.format("\\{0}", NitriteConfig.getFieldSeparator());
            String[] splits = field.split(regex);
            deepPut(splits, value);
        } else {
            super.put(field, value);
        }
        return this;
    }

    @Override
    public Object get(String field) {
        if (field != null
            && isEmbedded(field)
            && !containsKey(field)) {
            // if field is an embedded field, get it by deep scan
            return deepGet(field);
        }
        return super.get(field);
    }

    @Override
    public <T> T get(String field, Class<T> type) {
        notNull(type, "type cannot be null");
        return type.cast(get(field));
    }

    @Override
    public NitriteId getId() {
        String id;
        try {
            // if _id field is not populated already, create a new id
            // and set, otherwise return the existing id
            if (!containsKey(DOC_ID)) {
                id = newId().getIdValue();
                super.put(DOC_ID, id);
            } else {
                id = (String) get(DOC_ID);
            }

            // create a nitrite id instance from the string value
            return createId(id);
        } catch (ClassCastException cce) {
            throw new InvalidIdException("Invalid _id found " + get(DOC_ID));
        }
    }

    @Override
    public Set<String> getFields() {
        // get all fields except from the reserved ones
        return getFieldsInternal("");
    }

    @Override
    public boolean hasId() {
        return super.containsKey(DOC_ID);
    }

    @Override
    public void remove(String field) {
        if (isEmbedded(field)) {
            // if the field is an embedded field,
            // run a deep scan and remove the last field
            String regex = MessageFormat.format("\\{0}", NitriteConfig.getFieldSeparator());
            String[] splits = field.split(regex);
            deepRemove(splits);
        } else {
            // remove the field from this document
            super.remove(field);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Document clone() {
        Map<String, Object> cloned = (Map<String, Object>) super.clone();

        // create the clone of any embedded documents as well
        for (Map.Entry<String, Object> entry : cloned.entrySet()) {
            if (entry.getValue() instanceof Document) {
                Document value = (Document) entry.getValue();

                // this will recursively take care any embedded document
                // of the clone as well
                Document clonedValue = value.clone();
                cloned.put(entry.getKey(), clonedValue);
            }
        }
        return new NitriteDocument(cloned);
    }

    @Override
    public Document merge(Document document) {
        if (document instanceof NitriteDocument) {
            NitriteDocument nitriteDocument = (NitriteDocument) document;
            for (Pair<String, Object> entry : nitriteDocument) {
                String key = entry.getFirst();
                Object value = entry.getSecond();
                if (value instanceof NitriteDocument) {
                    // if the value is a document, merge it recursively
                    if (containsKey(key)) {
                        // if the current document already contains the key,
                        // and the value is not null, merge it
                        Document pairs = get(key, Document.class);
                        if (pairs != null) {
                            pairs.merge((Document) value);
                        } else {
                            //otherwise, just set the value to whatever was provided
                            put(key, value);
                        }
                    } else {
                        // if the current document does not contain the key,
                        // then put the embedded document as it is
                        put(key, value);
                    }
                } else {
                    // if there is no more embedded document, put the field in the document
                    put(key, value);
                }
            }
        } else {
            throw new InvalidOperationException("Document merge only supports NitriteDocument");
        }
        return this;
    }

    @Override
    public boolean containsKey(String key) {
        return super.containsKey(key);
    }

    @Override
    public boolean containsField(String field) {
        if (containsKey(field)) {
            // search top level
            return true;
        } else {
            // search deep level
            return getFields().contains(field);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;

        if (!(other instanceof NitriteDocument))
            return false;

        NitriteDocument m = (NitriteDocument) other;
        if (m.size() != size())
            return false;

        try {
            for (Map.Entry<String, Object> e : entrySet()) {
                String key = e.getKey();
                Object value = e.getValue();
                if (value == null) {
                    if (!(m.get(key) == null && m.containsKey(key)))
                        return false;
                } else {
                    if (!Objects.deepEquals(value, m.get(key))) {
                        return false;
                    }
                }
            }
        } catch (ClassCastException | NullPointerException unused) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public Iterator<Pair<String, Object>> iterator() {
        return new PairIterator(super.entrySet().iterator());
    }

    private Set<String> getFieldsInternal(String prefix) {
        Set<String> fields = new HashSet<>();

        // iterate top level keys
        for (Pair<String, Object> entry : this) {
            // ignore the reserved fields
            if (reservedFields.contains(entry.getFirst())) continue;

            Object value = entry.getSecond();
            if (value instanceof NitriteDocument) {
                // if the value is a document, traverse its fields recursively,
                // prefix would be the field name of the document
                if (isNullOrEmpty(prefix)) {
                    // level-1 fields
                    fields.addAll(((NitriteDocument) value).getFieldsInternal(entry.getFirst()));
                } else {
                    // level-n fields, separated by field separator
                    fields.addAll(((NitriteDocument) value).getFieldsInternal(prefix
                        + NitriteConfig.getFieldSeparator() + entry.getFirst()));
                }
            } else if (!(value instanceof Iterable)) {
                // if there is no more embedded document, add the field to the list
                // and if this is an embedded document then prefix its name by parent fields,
                // separated by field separator
                if (isNullOrEmpty(prefix)) {
                    fields.add(entry.getFirst());
                } else {
                    fields.add(prefix + NitriteConfig.getFieldSeparator() + entry.getFirst());
                }
            }
        }
        return fields;
    }

    private Object deepGet(String field) {
        if (isEmbedded(field)) {
            // for embedded field, run a deep scan
            return getByEmbeddedKey(field);
        } else {
            return null;
        }
    }

    private void deepPut(String[] splits, Object value) {
        if (splits.length == 0) {
            throw new ValidationException("Invalid key provided");
        }
        String key = splits[0];
        if (splits.length == 1) {
            // if last key, simply put in the current document
            put(key, value);
        } else {
            // get the object for the current level
            Object val = get(key);

            // get the remaining embedded fields for next level scan
            String[] remaining = Arrays.copyOfRange(splits, 1, splits.length);

            if (val instanceof NitriteDocument) {
                // if the current level value is embedded doc, scan to the next level
                ((NitriteDocument) val).deepPut(remaining, value);
            } else if (val == null) {
                // if current level value is null, create a new document
                // and try to create next level embedded doc by next level scan
                NitriteDocument subDoc = new NitriteDocument();
                subDoc.deepPut(remaining, value);

                // put the newly created document in current level
                put(key, subDoc);
            }
        }
    }

    private void deepRemove(String[] splits) {
        if (splits.length == 0) {
            throw new ValidationException("Invalid key provided");
        }
        String key = splits[0];
        if (splits.length == 1) {
            // if last key, simply remove the current document
            remove(key);
        } else {
            // get the object for the current level
            Object val = get(key);

            // get the remaining embedded fields for next level scan
            String[] remaining = Arrays.copyOfRange(splits, 1, splits.length);

            if (val instanceof NitriteDocument) {
                // if the current level value is embedded doc, scan to the next level
                NitriteDocument subDoc = (NitriteDocument) val;
                subDoc.deepRemove(remaining);
                if (subDoc.size() == 0) {
                    // if the next level document is an empty one
                    // remove the current level document also
                    super.remove(key);
                }
            } else if (val instanceof List && isInteger(splits[1])) {
                // if the current level value is an iterable,
                // remove the element at the next level
                List<?> list = (List<?>) val;
                int index = Integer.parseInt(splits[1]);
                Object item = list.get(index);
                if (splits.length > 2 && item instanceof NitriteDocument) {
                    // if there are more splits, then this is an embedded document
                    // so remove the element at the next level
                    ((NitriteDocument) item).deepRemove(Arrays.copyOfRange(splits, 2, splits.length));
                } else {
                    // if there are no more splits, then this is a primitive value
                    // so remove the element at the next level
                    list.remove(index);
                    this.put(key, list);
                }
            } else if (val != null && val.getClass().isArray()) {
                // if the current level value is an array,
                // remove the element at the next level
                Object[] array = convertToObjectArray(val);
                int index = Integer.parseInt(splits[1]);
                Object item = array[index];
                if (splits.length > 2 && item instanceof NitriteDocument) {
                    // if there are more splits, then this is an embedded document
                    // so remove the element at the next level
                    ((NitriteDocument) item).deepRemove(Arrays.copyOfRange(splits, 2, splits.length));
                } else {
                    // if there are no more splits, then this is a primitive value
                    // so remove the element at the next level
                    List<?> list = Arrays.asList(array);
                    list.remove(index);
                    this.put(key, list.toArray());
                }
            } else {
                // if current level value is not an iterable,
                // remove the key
                super.remove(key);
            }
        }
    }

    private Object getByEmbeddedKey(String embeddedKey) {
        String regex = MessageFormat.format("\\{0}", NitriteConfig.getFieldSeparator());

        // split the key
        String[] path = embeddedKey.split(regex);
        if (path.length < 1) {
            return null;
        }

        // get current level value and scan to next level using remaining keys
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
            // if the current level value is document, scan to the next level with remaining keys
            return recursiveGet(((Document) object).get(remainingPath[0]),
                Arrays.copyOfRange(remainingPath, 1, remainingPath.length));
        }

        if (object.getClass().isArray()) {
            // if the current level value is an array

            // get the first key
            String accessor = remainingPath[0];

            // convert current value to object array
            Object[] array = convertToObjectArray(object);

            if (isInteger(accessor)) {
                // if the current key is an integer

                // convert the key as an integer index
                int index = asInteger(accessor);

                // check index bound
                if (index < 0 || index >= array.length) {
                    throw new ValidationException("Invalid index " + index + " to access item inside a document");
                }

                // get the value at the index from the array
                // if there are remaining keys, scan to the next level
                return recursiveGet(array[index], Arrays.copyOfRange(remainingPath, 1, remainingPath.length));
            } else {
                // if the current key is not an integer, then decompose the
                // object array into a list and scan each of the element of the
                // list using remaining keys and return a list of all returned
                // elements from each of the list items.
                return decompose(listOf(array), remainingPath);
            }
        }

        if (object instanceof Iterable) {
            // if the current level value is an iterable

            // get the first key
            String accessor = remainingPath[0];

            // convert current value to object iterable
            Iterable<Object> iterable = (Iterable<Object>) object;

            // create a list from the iterable
            List<Object> collection = Iterables.toList(iterable);

            if (isInteger(accessor)) {
                // if the current key is an integer

                // convert the key as an integer index
                int index = asInteger(accessor);

                // check index bound
                if (index < 0 || index >= collection.size()) {
                    throw new ValidationException("Invalid index " + index + " to access item inside a document");
                }

                // get the value at the index from the list
                // if there are remaining keys, scan to the next level
                return recursiveGet(collection.get(index), Arrays.copyOfRange(remainingPath, 1, remainingPath.length));
            } else {
                // if the current key is not an integer, then decompose the
                // list and scan each of the element of the
                // list using remaining keys and return a list of all returned
                // elements from each of the list items.
                return decompose(collection, remainingPath);
            }
        }

        // if no match found return null
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<Object> decompose(List<Object> collection, String[] remainingPath) {
        Set<Object> items = new HashSet<>();

        // iterate each item
        for (Object item : collection) {

            // scan the item using remaining keys
            Object result = recursiveGet(item, remainingPath);

            if (result != null) {
                if (result instanceof Iterable) {
                    // if the result is iterable, return everything as a list
                    List<Object> list = Iterables.toList((Iterable<Object>) result);
                    items.addAll(list);
                } else if (result.getClass().isArray()) {
                    // if the result is an array, return everything as list
                    List<Object> list = Arrays.asList(convertToObjectArray(result));
                    items.addAll(list);
                } else {
                    // if its neither a iterable not an array, return the item
                    items.add(result);
                }
            }
        }
        return new ArrayList<>(items);
    }

    private int asInteger(String number) {
        try {
            // parse the string as an integer
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            // if parsing fails, return invalid integer for document access
            return -1;
        }
    }

    private boolean isInteger(String value) {
        try {
            // try parse the string as an integer
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            // if parsing fails, then value is not an integer
            return false;
        }
    }

    private boolean isEmbedded(String field) {
        // if the field contains separator character, then it is an embedded field
        return field.contains(NitriteConfig.getFieldSeparator());
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeInt(size());
        for (Pair<String, Object> pair : this) {
            stream.writeObject(pair);
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        int size = stream.readInt();
        for (int i = 0; i < size; i++) {
            Pair<String, Object> pair = (Pair<String, Object>) stream.readObject();
            super.put(pair.getFirst(), pair.getSecond());
        }
    }

    private static class PairIterator implements Iterator<Pair<String, Object>> {
        private final Iterator<Map.Entry<String, Object>> iterator;

        PairIterator(Iterator<Map.Entry<String, Object>> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Pair<String, Object> next() {
            Map.Entry<String, Object> next = iterator.next();
            return new Pair<>(next.getKey(), next.getValue());
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }
}
