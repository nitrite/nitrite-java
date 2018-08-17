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
import org.dizitart.no2.exceptions.ValidationException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.OBJ_INVALID_EMBEDDED_FIELD;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;
import static org.dizitart.no2.util.DocumentUtils.FIELD_SEPARATOR;
import static org.dizitart.no2.util.StringUtils.isNullOrEmpty;
import static org.dizitart.no2.util.ValidationUtils.notNull;

/**
 * A utility class for reflection.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
@UtilityClass
public class ReflectionUtils {

    /**
     * Gets the class hierarchy.
     *
     * @param startClass      the start class
     * @param exclusiveParent the exclusive parent
     * @return all declared fields in the specified class hierarchy.
     */
    public static List<Field> getFieldsUpto(Class<?> startClass, Class<?> exclusiveParent) {
        notNull(startClass, errorMessage("startClass can not be null", VE_REFLECT_FIELD_NULL_START_CLASS));
        List<Field> currentClassFields = new ArrayList<>(Arrays.asList(startClass.getDeclaredFields()));
        filterSynthetics(currentClassFields);
        Class<?> parentClass = startClass.getSuperclass();

        if (parentClass != null && !(parentClass.equals(exclusiveParent))) {
            List<Field> parentClassFields = getFieldsUpto(parentClass, exclusiveParent);
            currentClassFields.addAll(parentClassFields);
        }

        return currentClassFields;
    }

    static <T extends Annotation> List<T> findAnnotations(Class<T> annotation, Class<?> type) {
        notNull(type, errorMessage("type can not be null", VE_REFLECT_NULL_START_CLASS));
        notNull(annotation, errorMessage("annotationClass can not be null", VE_REFLECT_NULL_ANNOTATION_CLASS));
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


    /**
     * Gets all annotated fields in the entire class hierarchy.
     *
     * @param startClass      the start class in the class hierarchy.
     * @param annotationClass the annotation class
     * @return the annotated fields
     */
    static List<Field> getAnnotatedFields(Class<?> startClass, Class<? extends Annotation> annotationClass) {
        notNull(startClass, errorMessage("startClass can not be null", VE_REFLECT_NULL_START_CLASS));
        notNull(annotationClass, errorMessage("annotationClass can not be null", VE_REFLECT_NULL_ANNOTATION_CLASS));
        Iterable<Field> fields = getFieldsUpto(startClass, Object.class);
        List<Field> filtered = new ArrayList<>();
        for (Field field : fields) {
            Object annotation = field.getAnnotation(annotationClass);
            if (annotation != null) {
                filtered.add(field);
            }
        }
        return filtered;
    }

    static <T> Field getField(Class<T> type, String name, boolean recursive) {
        if (name.contains(FIELD_SEPARATOR)) {
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

            if (field == null && recursive) {
                List<Field> fields = getFieldsUpto(type, Object.class);
                for (Field recursiveField : fields) {
                    if (recursiveField.getName().equals(name)) {
                        field = recursiveField;
                        break;
                    }
                }
            }
            if (field == null) {
                throw new ValidationException(errorMessage(
                        "no such value \'" + name + "\' for type " + type.getName(),
                        VE_REFLECT_FIELD_NO_SUCH_FIELD));
            }
            return field;
        }
    }

    private static void filterSynthetics(List<Field> fields) {
        if (fields == null || fields.isEmpty()) return;
        Iterator<Field> iterator = fields.iterator();
        while (iterator.hasNext()) {
            Field f = iterator.next();
            if (f.isSynthetic()) iterator.remove();
        }
    }

    private static <T> Field getEmbeddedField(Class<T> startingClass, String embeddedField) {
        String regex = "\\" + FIELD_SEPARATOR;
        String[] split = embeddedField.split(regex, 2);
        String key = split[0];
        String remaining = split.length == 2 ? split[1] : "";

        if (isNullOrEmpty(key)) {
            throw new ValidationException(OBJ_INVALID_EMBEDDED_FIELD);
        }

        Field field;
        try {
            field = startingClass.getDeclaredField(key);
        } catch (NoSuchFieldException nsfe) {
            throw new ValidationException(errorMessage(
                    "no such value \'" + key + "\' for type " + startingClass.getName(),
                    VE_OBJ_INVALID_FIELD));
        }

        if (!isNullOrEmpty(remaining) || remaining.contains(FIELD_SEPARATOR)) {
            return getEmbeddedField(field.getType(), remaining);
        } else {
            return field;
        }
    }
}
