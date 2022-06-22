/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
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

package org.dizitart.no2.repository;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.repository.annotations.InheritIndices;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * @author Anindya Chatterjee
 */
class Reflector {
    @SuppressWarnings("rawtypes")
    public <T extends Annotation> List<T> findInheritedAnnotations(Class<T> annotation, Class<?> type) {
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
            List<T> list = findInheritedAnnotations(annotation, parentClass);
            annotations.addAll(list);
        }

        return annotations;
    }

    public <T> Field getEmbeddedField(Class<T> startingClass, String embeddedField) {
        String regex = MessageFormat.format("\\{0}", NitriteConfig.getFieldSeparator());
        String[] split = embeddedField.split(regex, 2);
        String key = split[0];
        String remaining = split.length == 2 ? split[1] : "";

        if (isNullOrEmpty(key)) {
            throw new ValidationException("Invalid embedded field provided");
        }

        Field field;
        try {
            field = startingClass.getDeclaredField(key);
        } catch (NoSuchFieldException e) {
            throw new ValidationException("No such field '" + key + "' for type " + startingClass.getName(), e);
        }

        if (!isNullOrEmpty(remaining) || remaining.contains(NitriteConfig.getFieldSeparator())) {
            return getEmbeddedField(field.getType(), remaining);
        } else {
            return field;
        }
    }

    public List<Field> getFieldsUpto(Class<?> startClass, Class<?> exclusiveParent) {
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

    public <T> Field getField(Class<T> type, String name) {
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
                throw new ValidationException("No such field '" + name + "' for type " + type.getName());
            }
            return field;
        }
    }

    public <T> List<Field> getAllFields(Class<T> type) {
        List<Field> fields;
        if (type.isAnnotationPresent(InheritIndices.class)) {
            fields = getFieldsUpto(type, Object.class);
        } else {
            fields = Arrays.asList(type.getDeclaredFields());
        }
        return fields;
    }

    public void filterSynthetics(List<Field> fields) {
        if (fields == null || fields.isEmpty()) return;
        Iterator<Field> iterator = fields.iterator();
        if (iterator.hasNext()) {
            do {
                Field f = iterator.next();
                if (f.isSynthetic()) iterator.remove();
            } while (iterator.hasNext());
        }
    }
}
