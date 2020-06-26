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

package org.dizitart.no2.mapper;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.util.ObjectUtils;
import org.dizitart.no2.exceptions.ObjectMappingException;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.dizitart.no2.common.util.Iterables.listOf;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class JacksonMapper extends MappableMapper {
    private List<JacksonModule> jacksonModules;
    private List<Class<?>> moduleTypes;

    @Getter(AccessLevel.PROTECTED)
    private ObjectMapper objectMapper;

    public JacksonMapper() {
        this.jacksonModules = new ArrayList<>();
        this.moduleTypes = new ArrayList<>();
        this.objectMapper = createObjectMapper();
    }

    public JacksonMapper(JacksonModule... jacksonModules) {
        this.jacksonModules = new ArrayList<>(listOf(jacksonModules));
        this.moduleTypes = new ArrayList<>();
        this.objectMapper = createObjectMapper();
    }

    protected ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(
            objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE));
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        this.jacksonModules.add(new NitriteIdModule());
        for (JacksonModule jacksonModule : jacksonModules) {
            loadJacksonModule(jacksonModule, objectMapper);
        }
        return objectMapper;
    }

    @Override
    protected void addValueType(Class<?> valueType) {
        super.addValueType(valueType);
        this.moduleTypes.add(valueType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Source, Target> Target convert(Source source, Class<Target> type) {
        if (source == null) {
            return null;
        }

        if (isValue(source)) {
            if (this.moduleTypes.contains(type)) {
                return this.objectMapper.convertValue(source, type);
            } else {
                return (Target) convertValue(source);
            }
        } else {
            if (Document.class.isAssignableFrom(type)) {
                return (Target) convertToDocument(source);
            } else if (source instanceof Document) {
                return convertToObject((Document) source, type);
            }
        }

        throw new ObjectMappingException("failed to convert using jackson");
    }

    @Override
    public boolean isValueType(Class<?> type) {
        if (super.isValueType(type)) return true;
        if (moduleTypes.contains(type)) return true;
        if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) return false;
        Object item = ObjectUtils.newInstance(type, false);
        return isValue(item);
    }

    @Override
    public boolean isValue(Object object) {
        try {
            JsonNode node = objectMapper.convertValue(object, JsonNode.class);
            return node != null && node.isValueNode();
        } catch (Exception ex) {
            throw new ObjectMappingException("error while checking for value type", ex);
        }
    }

    @Override
    public void initialize(NitriteConfig nitriteConfig) {

    }

    @Override
    protected <Target> Target convertToObject(Document source, Class<Target> type) {
        try {
            return super.convertToObject(source, type);
        } catch (ObjectMappingException ome) {
            try {
                return objectMapper.convertValue(source, type);
            } catch (IllegalArgumentException iae) {
                log.error("Error while converting document to object ", iae);
                if (iae.getCause() instanceof JsonMappingException) {
                    JsonMappingException jme = (JsonMappingException) iae.getCause();
                    if (jme.getMessage().contains("Cannot construct instance")) {
                        throw new ObjectMappingException(jme.getMessage());
                    }
                }
                throw iae;
            }
        }
    }

    @Override
    protected <Source> Document convertToDocument(Source source) {
        try {
            return super.convertToDocument(source);
        } catch (ObjectMappingException ome) {
            JsonNode node = objectMapper.convertValue(source, JsonNode.class);
            return readDocument(node);
        }
    }

    private void loadJacksonModule(JacksonModule jacksonModule, ObjectMapper objectMapper) {
        for (Class<?> dataType : jacksonModule.getDataTypes()) {
            addValueType(dataType);
        }
        objectMapper.registerModule(jacksonModule.getModule());
    }

    private Object convertValue(Object object) {
        JsonNode node = objectMapper.convertValue(object, JsonNode.class);
        if (node == null) {
            return null;
        }

        switch (node.getNodeType()) {
            case NUMBER:
                return node.numberValue();
            case STRING:
                return node.textValue();
            case BOOLEAN:
                return node.booleanValue();
            case ARRAY:
            case BINARY:
            case MISSING:
            case NULL:
            case OBJECT:
            case POJO:
            default:
                return null;
        }
    }

    private Document readDocument(JsonNode node) {
        Map<String, Object> objectMap = new LinkedHashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String name = entry.getKey();
            JsonNode value = entry.getValue();
            Object object = readObject(value);
            objectMap.put(name, object);
        }

        return Document.createDocument(objectMap);
    }

    private Object readObject(JsonNode node) {
        if (node == null)
            return null;
        try {
            switch (node.getNodeType()) {
                case ARRAY:
                    return readArray(node);
                case BINARY:
                    return node.binaryValue();
                case BOOLEAN:
                    return node.booleanValue();
                case MISSING:
                case NULL:
                    return null;
                case NUMBER:
                    return node.numberValue();
                case OBJECT:
                case POJO:
                    return readDocument(node);
                case STRING:
                    return node.textValue();
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List readArray(JsonNode array) {
        if (array.isArray()) {
            List list = new ArrayList();
            Iterator iterator = array.elements();
            while (iterator.hasNext()) {
                Object element = iterator.next();
                if (element instanceof JsonNode) {
                    list.add(readObject((JsonNode) element));
                } else {
                    list.add(element);
                }
            }
            return list;
        }
        return null;
    }
}
