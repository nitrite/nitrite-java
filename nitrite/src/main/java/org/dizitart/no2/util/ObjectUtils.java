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

package org.dizitart.no2.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.objects.ObjectFilter;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.exceptions.InvalidIdException;
import org.dizitart.no2.exceptions.NotIdentifiableException;
import org.dizitart.no2.index.annotations.Id;
import org.dizitart.no2.index.annotations.Index;
import org.dizitart.no2.index.annotations.Indices;
import org.dizitart.no2.index.annotations.InheritIndices;
import org.dizitart.no2.mapper.NitriteMapper;
import org.objenesis.ObjenesisStd;

import java.lang.reflect.Field;
import java.util.*;

import static org.dizitart.no2.common.Constants.KEY_OBJ_SEPARATOR;
import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.*;
import static org.dizitart.no2.filters.ObjectFilters.eq;
import static org.dizitart.no2.util.ReflectionUtils.*;
import static org.dizitart.no2.util.StringUtils.isNullOrEmpty;
import static org.dizitart.no2.util.ValidationUtils.*;

/**
 * A utility class for {@link Object}.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
@UtilityClass
@Slf4j
public class ObjectUtils {
    /**
     * Generates the name of an {@link org.dizitart.no2.collection.objects.ObjectRepository}.
     *
     * @param <T>           the type parameter
     * @param type          the type of object stored in the repository
     * @return the name of the object repository.
     */
    public static <T> String findObjectStoreName(Class<T> type) {
        notNull(type, errorMessage("type can not be null", VE_OBJ_STORE_NULL_TYPE));
        return type.getName();
    }

    /**
     * Generates the name of an {@link org.dizitart.no2.collection.objects.ObjectRepository}
     * with an unique key identifier.
     *
     * @param <T>  the type parameter
     * @param key  the key identifier
     * @param type the type of object stored in the repository
     * @return the name of the object repository.
     */
    public static <T> String findObjectStoreName(String key, Class<T> type) {
        notNull(key, errorMessage("key can not be null", VE_OBJ_STORE_NULL_KEY));
        notEmpty(key, errorMessage("key can not be empty", VE_OBJ_STORE_EMPTY_KEY));
        notNull(type, errorMessage("type can not be null", VE_OBJ_STORE_NULL_TYPE));
        return type.getName() + KEY_OBJ_SEPARATOR + key;
    }

    /**
     * Extract indices information by scanning for {@link Index} annotated fields.
     *
     * @param <T>           the type parameter
     * @param nitriteMapper the {@link NitriteMapper}
     * @param type          the type of the object stored in the repository
     * @return the set of all {@link Index} annotations found.
     */
    public static <T> Set<Index> extractIndices(NitriteMapper nitriteMapper, Class<T> type) {
        notNull(type, errorMessage("type can not be null", VE_INDEX_ANNOTATION_NULL_TYPE));

        List<Indices> indicesList;
        if (type.isAnnotationPresent(InheritIndices.class)) {
            indicesList = findAnnotations(Indices.class, type);
        } else {
            indicesList = new ArrayList<>();
            Indices indices = type.getAnnotation(Indices.class);
            if (indices != null) indicesList.add(indices);
        }

        Set<Index> indexSet = new LinkedHashSet<>();
        if (indicesList != null) {
            for (Indices indices : indicesList) {
                Index[] indexList = indices.value();
                populateIndex(nitriteMapper, type, Arrays.asList(indexList), indexSet);
            }
        }

        List<Index> indexList;
        if (type.isAnnotationPresent(InheritIndices.class)) {
            indexList = findAnnotations(Index.class, type);
        } else {
            indexList = new ArrayList<>();
            Index index = type.getAnnotation(Index.class);
            if (index != null) indexList.add(index);
        }

        if (indexList != null) {
            populateIndex(nitriteMapper, type, indexList, indexSet);
        }
        return indexSet;
    }

    /**
     * Gets the field marked with {@link Id} annotation.
     *
     * @param <T>           the type parameter
     * @param nitriteMapper the nitrite mapper
     * @param type          the type
     * @return the id field
     */
    public static <T> Field getIdField(NitriteMapper nitriteMapper, Class<T> type) {
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
                    throw new NotIdentifiableException(OBJ_MULTIPLE_ID_FOUND);
                } else {
                    alreadyIdFound = true;
                    idField = field;
                }
            }
        }
        return idField;
    }

    /**
     * Creates unique filter from the object.
     *
     * @param object  the object
     * @param idField the id field
     * @return the equals filter
     */
    public static ObjectFilter createUniqueFilter(Object object, Field idField) {
        idField.setAccessible(true);
        try {
            Object value = idField.get(object);
            if (value == null) {
                throw new InvalidIdException(ID_FILTER_VALUE_CAN_NOT_BE_NULL);
            }
            return eq(idField.getName(), value);
        } catch (IllegalAccessException iae) {
            throw new InvalidIdException(ID_FIELD_IS_NOT_ACCESSIBLE);
        }
    }

    /**
     * Checks whether a collection name is a valid object store name.
     *
     * @param collectionName the collection name
     * @return `true` if it is a valid object store name; `false` otherwise.
     */
    public static boolean isObjectStore(String collectionName) {
        try {
            if (isNullOrEmpty(collectionName)) return false;
            Class clazz = Class.forName(collectionName);
            return clazz != null;
        } catch (ClassNotFoundException e) {
            return isKeyedObjectStore(collectionName);
        }
    }

    /**
     * Checks whether a collection name is a valid keyed object store name.
     *
     * @param collectionName the collection name
     * @return `true` if it is a valid object store name; `false` otherwise.
     */
    public static boolean isKeyedObjectStore(String collectionName) {
        try {
            if (isNullOrEmpty(collectionName)) return false;
            if (!collectionName.contains(KEY_OBJ_SEPARATOR)) return false;

            String[] split = collectionName.split("\\" + KEY_OBJ_SEPARATOR);
            if (split.length != 2) {
                return false;
            }
            String storeName = split[0];
            Class clazz = Class.forName(storeName);
            return clazz != null;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static <T> T newInstance(Class<T> type) {
        try {
            return type.newInstance();
        } catch (Exception e) {
            return new ObjenesisStd().newInstance(type);
        }
    }

    public static <T> Document toDocument(T object, NitriteMapper nitriteMapper,
                                          Field idField, boolean update) {
        Document document = nitriteMapper.asDocument(object);
        if (idField != null) {
            if (idField.getType() == NitriteId.class) {
                try {
                    idField.setAccessible(true);
                    if (idField.get(object) == null) {
                        NitriteId id = document.getId();
                        idField.set(object, id);
                        document.put(idField.getName(), id.getIdValue());
                    } else if (!update) {
                        throw new InvalidIdException(AUTO_ID_ALREADY_SET);
                    }
                } catch (IllegalAccessException iae) {
                    throw new InvalidIdException(CANNOT_ACCESS_AUTO_ID);
                }
            }
            Object idValue = document.get(idField.getName());
            if (idValue == null) {
                throw new InvalidIdException(ID_CAN_NOT_BE_NULL);
            }
            if (idValue instanceof String && isNullOrEmpty((String) idValue)) {
                throw new InvalidIdException(ID_VALUE_CAN_NOT_BE_EMPTY_STRING);
            }
        }
        return document;
    }

    private <T> void populateIndex(NitriteMapper nitriteMapper, Class<T> type,
                                   List<Index> indexList, Set<Index> indexSet) {
        for (Index index : indexList) {
            String name = index.value();
            Field field = getField(type, name, type.isAnnotationPresent(InheritIndices.class));
            if (field != null) {
                validateObjectIndexField(nitriteMapper, field.getType(), field.getName());
                indexSet.add(index);
            } else {
                throw new IndexingException(errorMessage(
                        "field " + name + " does not exists for type " + type.getName(),
                        IE_OBJ_INDEX_INVALID_FIELD));
            }
        }
    }
}
