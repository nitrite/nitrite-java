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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holder class for interface property metadata.
 * Stores mapping between template fields and actual interface properties.
 *
 * @author Anindya Chatterjee
 * @since 4.3.2
 */
class InterfacePropertyHolder {
    // Template field used for interface properties
    Object property;
    
    // Maps template fields to their actual property metadata
    private static final Map<Field, PropertyMetadata> propertyRegistry = new ConcurrentHashMap<>();
    
    /**
     * Registers a synthetic field with its actual property metadata
     */
    static void registerProperty(Field templateField, String propertyName, Method getterMethod) {
        propertyRegistry.put(templateField, new PropertyMetadata(propertyName, getterMethod));
    }
    
    /**
     * Gets the actual property name for a field (handles both real and synthetic fields)
     */
    static String getPropertyName(Field field) {
        PropertyMetadata metadata = propertyRegistry.get(field);
        return metadata != null ? metadata.propertyName : field.getName();
    }
    
    /**
     * Gets the actual property type for a field (handles both real and synthetic fields)
     */
    static Class<?> getPropertyType(Field field) {
        PropertyMetadata metadata = propertyRegistry.get(field);
        return metadata != null ? metadata.getterMethod.getReturnType() : field.getType();
    }
    
    /**
     * Checks if a field is a synthetic interface property field
     */
    static boolean isInterfaceProperty(Field field) {
        return propertyRegistry.containsKey(field);
    }
    
    /**
     * Metadata about an interface property
     */
    private static class PropertyMetadata {
        final String propertyName;
        final Method getterMethod;
        
        PropertyMetadata(String propertyName, Method getterMethod) {
            this.propertyName = propertyName;
            this.getterMethod = getterMethod;
        }
    }
}
