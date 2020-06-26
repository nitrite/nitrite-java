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

package org.dizitart.no2.repository;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.exceptions.*;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.repository.annotations.*;
import org.dizitart.no2.mapper.NitriteMapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.*;

import static org.dizitart.no2.common.Constants.DOC_ID;
import static org.dizitart.no2.common.util.DocumentUtils.skeletonDocument;
import static org.dizitart.no2.common.util.ObjectUtils.isCompatibleTypes;
import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.dizitart.no2.index.IndexOptions.indexOptions;

/**
 * @author Anindya Chatterjee
 */
class RepositoryOperations {
    private final NitriteMapper nitriteMapper;
    private final Class<?> type;
    private final NitriteCollection collection;
    private Field idField;

    RepositoryOperations(Class<?> type, NitriteMapper nitriteMapper, NitriteCollection collection) {
        this.type = type;
        this.nitriteMapper = nitriteMapper;
        this.collection = collection;
        validateCollection();
    }

    private static void filterSynthetics(List<Field> fields) {
        if (fields == null || fields.isEmpty()) return;
        Iterator<Field> iterator = fields.iterator();
        if (iterator.hasNext()) {
            do {
                Field f = iterator.next();
                if (f.isSynthetic()) iterator.remove();
            } while (iterator.hasNext());
        }
    }

    void createIndexes() {
        Set<Index> indexes = extractIndices(type);
        for (Index idx : indexes) {
            String field = idx.value();
            if (!collection.hasIndex(field)) {
                collection.createIndex(idx.value(), indexOptions(idx.type(), false));
            }
        }

        idField = getIdField(type);
        if (idField != null) {
            String field = idField.getName();
            if (!collection.hasIndex(field)) {
                collection.createIndex(field, indexOptions(IndexType.Unique));
            }
        }
    }

    void serializeFields(Document document) {
        if (document != null) {
            for (KeyValuePair<String, Object> keyValuePair : document) {
                String key = keyValuePair.getKey();
                Object value = keyValuePair.getValue();
                Object serializedValue;
                serializedValue = nitriteMapper.convert(value, Document.class);
                document.put(key, serializedValue);
            }
        }
    }

    <T> Document[] toDocuments(T[] others) {
        if (others == null || others.length == 0) return null;
        Document[] documents = new Document[others.length];
        for (int i = 0; i < others.length; i++) {
            documents[i] = toDocument(others[i], false);
        }
        return documents;
    }

    <T> Document toDocument(T object, boolean update) {
        Document document = nitriteMapper.convert(object, Document.class);
        if (idField != null) {
            if (idField.getType() == NitriteId.class) {
                try {
                    idField.setAccessible(true);
                    if (idField.get(object) == null) {
                        NitriteId id = document.getId();
                        idField.set(object, id);
                        document.put(idField.getName(), nitriteMapper.convert(id, Comparable.class));
                    } else if (!update) {
                        throw new InvalidIdException("auto generated id should not be set manually");
                    }
                } catch (IllegalAccessException iae) {
                    throw new InvalidIdException("auto generated id value cannot be accessed");
                }
            }
            Object idValue = document.get(idField.getName());
            if (idValue == null) {
                throw new InvalidIdException("id cannot be null");
            }
            if (idValue instanceof String && isNullOrEmpty((String) idValue)) {
                throw new InvalidIdException("id value cannot be empty string");
            }
        }
        return document;
    }

    Filter createUniqueFilter(Object object) {
        if (idField == null) {
            throw new NotIdentifiableException("update operation failed as no id value found for the object");
        }

        idField.setAccessible(true);
        try {
            Object value = idField.get(object);
            if (value == null) {
                throw new InvalidIdException("id value cannot be null");
            }
            return where(idField.getName()).eq(value);
        } catch (IllegalAccessException iae) {
            throw new InvalidIdException("id field is not accessible");
        }
    }

    <T> Field getIdField(Class<T> type) {
        List<Field> fields;
        if (type.isAnnotationPresent(InheritIndices.class)) {
            fields = getFieldsUpto(type, Object.class);
        } else {
            fields = Arrays.asList(type.getDeclaredFields());
        }

        boolean alreadyIdFound = false;
        Field idField = null;
        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) {
                validateObjectIndexField(nitriteMapper, field.getType(), field.getName());
                if (alreadyIdFound) {
                    throw new NotIdentifiableException("multiple id fields found for the type");
                } else {
                    alreadyIdFound = true;
                    idField = field;
                }
            }
        }
        return idField;
    }

    <T> Set<Index> extractIndices(Class<T> type) {
        notNull(type, "type cannot be null");

        // populate from @Indices
        List<Indices> indicesList;
        if (type.isAnnotationPresent(InheritIndices.class)) {
            indicesList = findAnnotations(Indices.class, type);
        } else {
            indicesList = new ArrayList<>();
            Indices indices = type.getAnnotation(Indices.class);
            if (indices != null) indicesList.add(indices);
        }

        Set<Index> indexSet = new LinkedHashSet<>();
        for (Indices indices : indicesList) {
            Index[] indexList = indices.value();
            populateIndex(nitriteMapper, type, Arrays.asList(indexList), indexSet);
        }

        // populate from @Index
        List<Index> indexList;
        if (type.isAnnotationPresent(InheritIndices.class)) {
            indexList = findAnnotations(Index.class, type);
        } else {
            indexList = new ArrayList<>();
            Index index = type.getAnnotation(Index.class);
            if (index != null) indexList.add(index);
        }

        // populate from @Entity
        if (type.isAnnotationPresent(InheritIndices.class)) {
            List<Entity> entities = findAnnotations(Entity.class, type);
            if (!entities.isEmpty()) {
                for (Entity entity : entities) {
                    indexList.addAll(Arrays.asList(entity.indices()));
                }
            }
        } else if (type.isAnnotationPresent(Entity.class)) {
            Entity entity = type.getAnnotation(Entity.class);
            indexList.addAll(Arrays.asList(entity.indices()));
        }

        populateIndex(nitriteMapper, type, indexList, indexSet);

        return indexSet;
    }

    <T> Field getField(Class<T> type, String name) {
        if (name.contains(NitriteConfig.getFieldSeparator())) {
            return getEmbeddedField(type, name);
        } else {
            // first check declared fields (fix for kotlin properties, ref: issue #54)
            // if nothing found and is-recursive then check recursively
            Field[] declaredFields = type.getDeclaredFields();
            Field field = null;
            for (Field declaredField : declaredFields) {
                if (declaredField.getName().equals(name)) {
                    field = declaredField;
                    break;
                }
            }

            if (field == null) {
                List<Field> fields = getFieldsUpto(type, Object.class);
                for (Field recursiveField : fields) {
                    if (recursiveField.getName().equals(name)) {
                        field = recursiveField;
                        break;
                    }
                }
            }
            if (field == null) {
                throw new ValidationException("no such field '" + name + "' for type " + type.getName());
            }
            return field;
        }
    }

    List<Field> getFieldsUpto(Class<?> startClass, Class<?> exclusiveParent) {
        notNull(startClass, "startClass cannot be null");
        List<Field> currentClassFields = new ArrayList<>(Arrays.asList(startClass.getDeclaredFields()));
        filterSynthetics(currentClassFields);
        Class<?> parentClass = startClass.getSuperclass();

        if (parentClass != null && !(parentClass.equals(exclusiveParent))) {
            List<Field> parentClassFields = getFieldsUpto(parentClass, exclusiveParent);
            currentClassFields.addAll(parentClassFields);
        }

        return currentClassFields;
    }

    void removeNitriteId(Document document) {
        document.remove(DOC_ID);
        if (idField != null && idField.getType() == NitriteId.class) {
            document.remove(idField.getName());
        }
    }

    <I> Filter createIdFilter(I id) {
        if (idField != null) {
            if (id == null) {
                throw new InvalidIdException("a null id is not a valid id");
            }

            if (isCompatibleTypes(idField.getType(), id.getClass())) {
                return where(idField.getName()).eq(id);
            } else {
                throw new InvalidIdException(id.getClass().getName() + " is not assignable to id type "
                    + idField.getType().getName());
            }
        } else {
            throw new NotIdentifiableException(type.getName() + " does not have any id field");
        }
    }

    private void validateCollection() {
        if (collection == null) {
            throw new ValidationException("repository has not been initialized properly");
        }
    }

    private <T> Field getEmbeddedField(Class<T> startingClass, String embeddedField) {
        String regex = MessageFormat.format("\\{0}", NitriteConfig.getFieldSeparator());
        String[] split = embeddedField.split(regex, 2);
        String key = split[0];
        String remaining = split.length == 2 ? split[1] : "";

        if (isNullOrEmpty(key)) {
            throw new ValidationException("invalid embedded field provided");
        }

        Field field;
        try {
            field = startingClass.getDeclaredField(key);
        } catch (NoSuchFieldException e) {
            throw new ValidationException("no such field '" + key + "' for type " + startingClass.getName(), e);
        }

        if (!isNullOrEmpty(remaining) || remaining.contains(NitriteConfig.getFieldSeparator())) {
            return getEmbeddedField(field.getType(), remaining);
        } else {
            return field;
        }
    }

    private <T> void populateIndex(NitriteMapper nitriteMapper, Class<T> type,
                                   List<Index> indexList, Set<Index> indexSet) {
        for (Index index : indexList) {
            String name = index.value();
            Field field = getField(type, name);
            if (field != null) {
                validateObjectIndexField(nitriteMapper, field.getType(), field.getName());
                indexSet.add(index);
            }
        }
    }

    private void validateObjectIndexField(NitriteMapper nitriteMapper, Class<?> fieldType, String field) {
        if (!Comparable.class.isAssignableFrom(fieldType) && !fieldType.isPrimitive()) {
            throw new IndexingException("cannot index on non comparable field " + field);
        }

        if (Iterable.class.isAssignableFrom(fieldType) || fieldType.isArray()) {
            throw new IndexingException("indexing on arrays or collections for field " + field
                + " are not supported");
        }

        if (fieldType.isPrimitive()
            || fieldType == NitriteId.class
            || fieldType.isInterface()
            || nitriteMapper.isValueType(fieldType)
            || Modifier.isAbstract(fieldType.getModifiers())) {
            // we will validate the solid class during insertion/update
            return;
        }

        Document document;
        try {
            document = skeletonDocument(nitriteMapper, fieldType);
        } catch (Throwable e) {
            throw new IndexingException("invalid type specified " + fieldType.getName() + " for indexing", e);
        }

        if (document == null || document.size() > 0) {
            throw new InvalidOperationException("compound index on field " + field + " is not supported");
        }
    }

    @SuppressWarnings("rawtypes")
    private <T extends Annotation> List<T> findAnnotations(Class<T> annotation, Class<?> type) {
        notNull(type, "type cannot be null");
        notNull(annotation, "annotationClass cannot be null");
        List<T> annotations = new ArrayList<>();

        T t = type.getAnnotation(annotation);
        if (t != null) annotations.add(t);

        Class[] interfaces = type.getInterfaces();
        for (Class<?> anInterface : interfaces) {
            T ann = anInterface.getAnnotation(annotation);
            if (ann != null) annotations.add(ann);
        }

        Class<?> parentClass = type.getSuperclass();
        if (parentClass != null && !parentClass.equals(Object.class)) {
            List<T> list = findAnnotations(annotation, parentClass);
            annotations.addAll(list);
        }

        return annotations;
    }
}
