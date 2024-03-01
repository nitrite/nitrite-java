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
import org.dizitart.no2.collection.*;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.exceptions.*;
import org.dizitart.no2.filters.*;

import java.lang.reflect.Field;

import static org.dizitart.no2.common.Constants.DOC_ID;
import static org.dizitart.no2.common.util.ObjectUtils.isCompatibleTypes;
import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;

/**
 * @author Anindya Chatterjee
 * @since 4.0
 */
public class RepositoryOperations {
    private final NitriteConfig nitriteConfig;
    private final NitriteMapper nitriteMapper;
    private final NitriteCollection collection;
    private final Class<?> type;
    private AnnotationScanner annotationScanner;
    private ObjectIdField objectIdField;
    private EntityDecoratorScanner entityDecoratorScanner;

    public RepositoryOperations(Class<?> type, NitriteCollection collection, NitriteConfig nitriteConfig) {
        this.type = type;
        this.nitriteConfig = nitriteConfig;
        this.nitriteMapper = nitriteConfig.nitriteMapper();
        this.collection = collection;
        this.annotationScanner = new AnnotationScanner(type, collection, nitriteMapper);
        validateCollection();
    }

    public RepositoryOperations(EntityDecorator<?> entityDecorator, NitriteCollection collection, NitriteConfig nitriteConfig) {
        this.type = entityDecorator.getEntityType();
        this.nitriteConfig = nitriteConfig;
        this.nitriteMapper = nitriteConfig.nitriteMapper();
        this.collection = collection;
        this.entityDecoratorScanner = new EntityDecoratorScanner(entityDecorator, collection, nitriteMapper);
        validateCollection();
    }

    public void scanIndexes() {
        if (annotationScanner != null) {
            annotationScanner.performScan();
            objectIdField = annotationScanner.getObjectIdField();
        } else if (entityDecoratorScanner != null) {
            entityDecoratorScanner.readEntity();
            objectIdField = entityDecoratorScanner.getObjectIdField();
        }
    }

    public void createIndexes() {
        if (annotationScanner != null) {
            annotationScanner.createIndices();
            annotationScanner.createIdIndex();
        } else if (entityDecoratorScanner != null) {
            entityDecoratorScanner.createIndices();
            entityDecoratorScanner.createIdIndex();
        }
    }

    public void serializeFields(Document document) {
        if (document != null) {
            for (Pair<String, Object> pair : document) {
                String key = pair.getFirst();
                Object value = pair.getSecond();
                Object serializedValue;
                serializedValue = nitriteMapper.tryConvert(value, Document.class);
                document.put(key, serializedValue);
            }
        }
    }

    public <T> Document[] toDocuments(T[] others) {
        if (others == null || others.length == 0) return null;
        Document[] documents = new Document[others.length];
        for (int i = 0; i < others.length; i++) {
            documents[i] = toDocument(others[i], false); // this method is for insert only
        }
        return documents;
    }

    public <T> Document toDocument(T object, boolean update) {
        Document document = (Document) nitriteMapper.tryConvert(object, Document.class);
        if (document == null) {
            throw new ObjectMappingException("Failed to map object to document");
        }

        if (objectIdField != null) {
            Field idField = objectIdField.getField();

            if (idField.getType() == NitriteId.class) {
                try {
                    idField.setAccessible(true);
                    if (idField.get(object) == null) {
                        NitriteId id = document.getId();
                        idField.set(object, id);
                        document.put(objectIdField.getIdFieldName(), nitriteMapper.tryConvert(id, Comparable.class));
                    } else if (!update) {
                        // if it is an insert, then we should not allow to insert the document with user
                        // provided id
                        throw new InvalidIdException("Auto generated id should not be set manually");
                    }
                } catch (IllegalAccessException iae) {
                    throw new InvalidIdException("Auto generated id value cannot be accessed");
                }
            }

            Object idValue = document.get(objectIdField.getIdFieldName());
            if (idValue == null) {
                throw new InvalidIdException("Id cannot be null");
            }
            if (idValue instanceof String && isNullOrEmpty((String) idValue)) {
                throw new InvalidIdException("Id value cannot be empty string");
            }
        }
        return document;
    }

    public Filter createUniqueFilter(Object object) {
        if (objectIdField == null) {
            throw new NotIdentifiableException("No id value found for the object");
        }

        Field idField = objectIdField.getField();
        idField.setAccessible(true);
        try {
            Object value = idField.get(object);
            if (value == null) {
                throw new InvalidIdException("Id value cannot be null");
            }
            return objectIdField.createUniqueFilter(value, nitriteMapper);
        } catch (IllegalAccessException iae) {
            throw new InvalidIdException("Id field is not accessible");
        }
    }

    public void removeNitriteId(Document document) {
        document.remove(DOC_ID);
        if (objectIdField != null) {
            Field idField = objectIdField.getField();
            if (idField != null && !objectIdField.isEmbedded() && idField.getType() == NitriteId.class) {
                document.remove(idField.getName());
            }
        }
    }

    public <I> Filter createIdFilter(I id) {
        if (objectIdField != null) {
            if (id == null) {
                throw new InvalidIdException("Id cannot be null");
            }
            if (!isCompatibleTypes(id.getClass(), objectIdField.getField().getType())) {
                throw new InvalidIdException("A value of invalid type is provided as id");
            }

            return objectIdField.createUniqueFilter(id, nitriteMapper);
        } else {
            throw new NotIdentifiableException(type.getName() + " does not have any id field");
        }
    }

    public Filter asObjectFilter(Filter filter) {
        if (filter instanceof NitriteFilter) {
            NitriteFilter nitriteFilter = (NitriteFilter) filter;
            nitriteFilter.setObjectFilter(true);
            nitriteFilter.setNitriteConfig(nitriteConfig);

            if (filter instanceof FieldBasedFilter) {
                return createObjectFilter((FieldBasedFilter) filter);
            }
            return nitriteFilter;
        }
        return filter;
    }

    public <T> Cursor<T> find(Filter filter, FindOptions findOptions, Class<T> type) {
        DocumentCursor documentCursor = collection.find(asObjectFilter(filter), findOptions);
        return new ObjectCursor<>(nitriteMapper, documentCursor, type);
    }

    private void validateCollection() {
        if (collection == null) {
            throw new ValidationException("Repository has not been initialized properly");
        }
    }

    private Filter createObjectFilter(FieldBasedFilter fieldBasedFilter) {
        if (objectIdField != null && objectIdField.getIdFieldName().equals(fieldBasedFilter.getField())) {
            if (fieldBasedFilter instanceof EqualsFilter) {
                return objectIdField.createUniqueFilter(fieldBasedFilter.getValue(), nitriteMapper);
            } else if (fieldBasedFilter instanceof ComparableFilter) {
                Object fieldValue = fieldBasedFilter.getValue();
                Object converted = nitriteMapper.tryConvert(fieldValue, Document.class);
                if (converted instanceof Document) {
                    throw new InvalidOperationException("Cannot compare object of type " + fieldValue.getClass());
                }
            }
        }
        return fieldBasedFilter;
    }
}
