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

package org.dizitart.no2.mapper.jackson;

import static org.dizitart.no2.common.util.ValidationUtils.notNull;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.exceptions.ObjectMappingException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.mapper.jackson.modules.NitriteIdModule;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A {@link org.dizitart.no2.common.mapper.NitriteMapper} implementation that uses Jackson ObjectMapper to
 * convert objects to and from Nitrite document.
 *
 * @author Anindya Chatterjee
 * @see org.dizitart.no2.common.mapper.NitriteMapper
 * @since 4.0
 */
public class JacksonMapper implements NitriteMapper {
    private final List<JacksonModule> modules = new ArrayList<>();
    private ObjectMapper objectMapper;

    /**
     * Returns the object mapper instance used for document conversion.
     *
     * @return the object mapper instance used for document conversion
     */
    protected ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = JsonMapper.builder()
                .changeDefaultVisibility(visibilityChecker -> visibilityChecker
                    .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                    .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                    .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
                )
                .configure(JsonReadFeature.ALLOW_UNQUOTED_PROPERTY_NAMES, true)
                .configure(JsonReadFeature.ALLOW_SINGLE_QUOTES, true)
                .configure(JsonReadFeature.ALLOW_JAVA_COMMENTS, true)
                .addModule(new NitriteIdModule())
                .addModules(modules)
                .build();
            modules.clear();
        }
        return objectMapper;
    }

    /**
     * Registers a Jackson module with the object mapper.
     * Can only register modules as long as the underlying ObjectMapper is still un-initialized.
     *
     * @param module the Jackson module to register
     *
     * @throws ValidationException if the module is null
     */
    public void registerJacksonModule(JacksonModule module) {
        if (objectMapper != null) {
            throw new IllegalStateException("Can not register modules after object mapper initialization.");
        }
        notNull(module, "module cannot be null");
        modules.add(module);
    }

    /**
     * Tries to convert the given source object to the target type using Jackson
     * ObjectMapper.
     * <p>
     * If the source object is null, returns null. If the source
     * object is a value node, returns the node value. If the target type is
     * Document, converts the source object to a Document. If the source object is
     * already a Document, converts it to the target type. If the conversion fails,
     * throws an ObjectMappingException.
     *
     * @param source the source object to convert
     * @param type the target type to convert to
     * @param <Source> the type of the source object
     * @param <Target> the type of the target object
     *
     * @return the converted object of the target type
     *
     * @throws ObjectMappingException if the conversion fails
     */
    @Override
    public <Source, Target> Object tryConvert(Source source, Class<Target> type) {
        if (source == null) {
            return null;
        }

        try {
            JsonNode node = getObjectMapper().convertValue(source, JsonNode.class);
            if (node == null) {
                return null;
            }

            if (node.isValueNode()) {
                return getNodeValue(node);
            } else {
                if (Document.class.isAssignableFrom(type)) {
                    return convertToDocument(source);
                } else if (source instanceof Document) {
                    return convertFromDocument((Document) source, type);
                }
            }
        } catch (Exception e) {
            throw new ObjectMappingException("Failed to convert object of type "
                                             + source.getClass() + " to type " + type, e);
        }

        throw new ObjectMappingException("Can't convert object to type " + type
                                         + ", try registering a jackson Module for it.");
    }

    @Override
    public void initialize(NitriteConfig nitriteConfig) {
    }

    /**
     * Converts a Nitrite Document to an object of the specified class type using
     * Jackson ObjectMapper.
     *
     * @param source the Nitrite Document to be converted
     * @param type the class type of the object to be converted to
     * @param <Target> the type of the object to be converted to
     *
     * @return the converted object of the specified class type
     *
     * @throws ObjectMappingException if there is an error in the object mapping
     * process
     */
    protected <Target> Target convertFromDocument(Document source, Class<Target> type) {
        try {
            return getObjectMapper().convertValue(source, type);
        } catch (IllegalArgumentException iae) {
            if (iae.getCause() instanceof DatabindException) {
                DatabindException cause = (DatabindException) iae.getCause();
                if (cause.getMessage().contains("Cannot construct instance")) {
                    throw new ObjectMappingException(cause.getMessage());
                }
            }
            throw iae;
        }
    }

    /**
     * Converts the given source object to a Nitrite {@link Document} using
     * Jackson's {@link ObjectMapper}.
     *
     * @param source the source object to convert
     * @param <Source> the type of the source object
     *
     * @return the converted Nitrite {@link Document}
     */
    protected <Source> Document convertToDocument(Source source) {
        JsonNode node = getObjectMapper().convertValue(source, JsonNode.class);
        return readDocument(node);
    }

    @SuppressWarnings("unchecked")
    private <T> T getNodeValue(JsonNode node) {
        switch (node.getNodeType()) {
            case NUMBER:
                return (T) node.numberValue();
            case STRING:
                return (T) node.stringValue();
            case BOOLEAN:
                return (T) Boolean.valueOf(node.booleanValue());
            default:
                return null;
        }
    }

    private Document readDocument(JsonNode node) {
        Map<String, Object> objectMap = new LinkedHashMap<>();
        for (Map.Entry<String, JsonNode> entry : node.properties()) {
            String name = entry.getKey();
            JsonNode value = entry.getValue();
            Object object = readObject(value);
            objectMap.put(name, object);
        }

        return Document.createDocument(objectMap);
    }

    private Object readObject(JsonNode node) {
        if (node == null) {
            return null;
        }

        switch (node.getNodeType()) {
            case ARRAY:
                return readArray(node);
            case BINARY:
                return node.binaryValue();
            case BOOLEAN:
                return node.booleanValue();
            case NUMBER:
                return node.numberValue();
            case OBJECT:
            case POJO:
                return readDocument(node);
            case STRING:
                return node.stringValue();
            default:
                return null;
        }
    }

    private List<Object> readArray(JsonNode array) {
        if (array.isArray()) {
            return array.valueStream()
                .map(this::readObject)
                .collect(Collectors.toList());
        }
        return null;
    }
}
