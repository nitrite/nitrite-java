/*
 * Copyright (c) 2017-2022 Nitrite author or authors.
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Helper class to access field values, handling both regular fields and interface properties.
 * For interface properties (synthetic fields from InterfacePropertyHolder), this class
 * finds and accesses the actual field on the concrete implementation object.
 *
 * @author Anindya Chatterjee
 * @since 4.3.2
 */
class FieldAccessHelper {

    /**
     * Gets the value of a field from an object, handling both regular and interface property fields.
     * Uses reflection with appropriate access control.
     * 
     * @param field the field to access
     * @param obj the object to get the value from
     * @return the field value
     * @throws IllegalAccessException if field access fails
     */
    static Object get(Field field, Object obj) throws IllegalAccessException {
        if (InterfacePropertyHolder.isInterfaceProperty(field)) {
            // This is a synthetic field for an interface property
            // Find and access the real field in the concrete object
            String propertyName = InterfacePropertyHolder.getPropertyName(field);
            return getPropertyValue(obj, propertyName);
        } else {
            return getFieldValue(field, obj);
        }
    }

    /**
     * Sets the value of a field on an object, handling both regular and interface property fields.
     * Uses reflection with appropriate access control.
     * 
     * @param field the field to set
     * @param obj the object to set the value on
     * @param value the value to set
     * @throws IllegalAccessException if field access fails
     */
    static void set(Field field, Object obj, Object value) throws IllegalAccessException {
        if (InterfacePropertyHolder.isInterfaceProperty(field)) {
            // This is a synthetic field for an interface property
            // Find and set the real field in the concrete object
            String propertyName = InterfacePropertyHolder.getPropertyName(field);
            setPropertyValue(obj, propertyName, value);
        } else {
            setFieldValue(field, obj, value);
        }
    }
    
    /**
     * Gets the value of a field, handling access control appropriately.
     */
    private static Object getFieldValue(Field field, Object obj) throws IllegalAccessException {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        return field.get(obj);
    }
    
    /**
     * Sets the value of a field, handling access control appropriately.
     */
    private static void setFieldValue(Field field, Object obj, Object value) throws IllegalAccessException {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        field.set(obj, value);
    }

    /**
     * Gets a property value from an object, trying both field access and getter method.
     */
    private static Object getPropertyValue(Object obj, String propertyName) throws IllegalAccessException {
        if (propertyName == null || propertyName.isEmpty()) {
            throw new IllegalAccessException("Property name cannot be null or empty");
        }
        
        // Try to find the field in the object's class
        Field realField = findFieldInHierarchy(obj.getClass(), propertyName);
        if (realField != null) {
            return getFieldValue(realField, obj);
        }

        // Fall back to getter method - try both 'get' and 'is' prefixes
        try {
            Method getter = findGetterMethod(obj.getClass(), propertyName);
            if (getter != null) {
                return invokeMethod(getter, obj);
            }
            throw new IllegalAccessException("No getter method found for property '" + propertyName + "'");
        } catch (Exception e) {
            throw new IllegalAccessException("Cannot access property '" + propertyName + "' on " + obj.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Sets a property value on an object, trying both field access and setter method.
     */
    private static void setPropertyValue(Object obj, String propertyName, Object value) throws IllegalAccessException {
        if (propertyName == null || propertyName.isEmpty()) {
            throw new IllegalAccessException("Property name cannot be null or empty");
        }
        
        // Try to find the field in the object's class
        Field realField = findFieldInHierarchy(obj.getClass(), propertyName);
        if (realField != null) {
            setFieldValue(realField, obj, value);
            return;
        }

        // Fall back to setter method
        try {
            String setterName = "set" + capitalizePropertyName(propertyName);
            Method setter = findSetterMethod(obj.getClass(), setterName, value);
            if (setter != null) {
                invokeMethod(setter, obj, value);
            } else {
                throw new IllegalAccessException("No setter method found for property '" + propertyName + "'");
            }
        } catch (Exception e) {
            throw new IllegalAccessException("Cannot set property '" + propertyName + "' on " + obj.getClass().getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Invokes a method, handling access control appropriately.
     */
    private static Object invokeMethod(Method method, Object obj, Object... args) throws IllegalAccessException {
        try {
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            return method.invoke(obj, args);
        } catch (IllegalAccessException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalAccessException("Cannot invoke method " + method.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Finds a getter method for a property (tries both 'get' and 'is' prefixes).
     */
    private static Method findGetterMethod(Class<?> clazz, String propertyName) {
        String capitalizedName = capitalizePropertyName(propertyName);
        String getterName = "get" + capitalizedName;
        String isGetterName = "is" + capitalizedName;
        
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if ((method.getName().equals(getterName) || method.getName().equals(isGetterName)) 
                && method.getParameterTypes().length == 0) {
                return method;
            }
        }
        return null;
    }
    
    /**
     * Capitalizes a property name following JavaBeans conventions.
     */
    private static String capitalizePropertyName(String propertyName) {
        if (propertyName == null || propertyName.isEmpty()) {
            return propertyName;
        }
        // Follow JavaBeans convention: if first two chars are uppercase, don't change
        if (propertyName.length() > 1 && Character.isUpperCase(propertyName.charAt(0)) && Character.isUpperCase(propertyName.charAt(1))) {
            return propertyName;
        }
        return Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
    }

    /**
     * Finds a field in the class hierarchy.
     */
    private static Field findFieldInHierarchy(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    /**
     * Finds a setter method that can accept the given value.
     */
    private static Method findSetterMethod(Class<?> clazz, String setterName, Object value) {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(setterName) && method.getParameterTypes().length == 1) {
                Class<?> paramType = method.getParameterTypes()[0];
                if (value == null || paramType.isAssignableFrom(value.getClass()) || 
                    (paramType.isPrimitive() && isCompatiblePrimitive(paramType, value.getClass()))) {
                    return method;
                }
            }
        }
        return null;
    }

    /**
     * Checks if a value class is compatible with a primitive parameter type.
     */
    private static boolean isCompatiblePrimitive(Class<?> primitiveType, Class<?> valueClass) {
        // Use a more efficient lookup instead of cascading if statements
        return (primitiveType == int.class && valueClass == Integer.class)
            || (primitiveType == long.class && valueClass == Long.class)
            || (primitiveType == double.class && valueClass == Double.class)
            || (primitiveType == float.class && valueClass == Float.class)
            || (primitiveType == boolean.class && valueClass == Boolean.class)
            || (primitiveType == byte.class && valueClass == Byte.class)
            || (primitiveType == short.class && valueClass == Short.class)
            || (primitiveType == char.class && valueClass == Character.class);
    }
}
