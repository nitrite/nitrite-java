/*
 *
 * Copyright 2017 Nitrite author or authors.
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

package org.dizitart.no2.tool;

import static org.dizitart.no2.exceptions.ErrorCodes.OME_PARSE_JSON_FAILED;
import static org.dizitart.no2.exceptions.ErrorMessage.JSON_SERIALIZATION_FAILED;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dizitart.no2.Document;
import org.dizitart.no2.exceptions.ObjectMappingException;
import org.dizitart.no2.mapper.NitriteIdModule;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JacksonDeSerializer implements JsonDeSerializer {
    private ObjectMapper objectMapper;

    public JacksonDeSerializer() {
        getObjectMapper();
    }

    @Override
    public Document parse(String json) {
        ObjectMapper objectMapper = getObjectMapper();
        try {
            JsonNode node = objectMapper.readValue(json, JsonNode.class);
            return loadDocument(node);
        } catch (IOException e) {
            log.error("Error while parsing json", e);
            throw new ObjectMappingException(errorMessage("failed to parse json " + json,
                    OME_PARSE_JSON_FAILED));
        }
    }

    @Override
    public String toJson(Object object) {
        StringWriter stringWriter = new StringWriter();
        try {
            getObjectMapper().writeValue(stringWriter, object);
            return stringWriter.toString();
        } catch (IOException e) {
            log.error("Error while serializing object to json", e);
            throw new ObjectMappingException(JSON_SERIALIZATION_FAILED);
        }
    }

    /**
     * Gets the underlying {@link ObjectMapper} instance to configure.
     *
     * @return the object mapper instance.
     */
    public ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.setVisibility(
                    objectMapper.getSerializationConfig().
                            getDefaultVisibilityChecker().
                            withFieldVisibility(JsonAutoDetect.Visibility.ANY).
                            withGetterVisibility(JsonAutoDetect.Visibility.NONE).
                            withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
            );
            objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
            objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.registerModule(new NitriteIdModule());
            objectMapper.findAndRegisterModules();
        }
        return objectMapper;
    }

    private Document loadDocument(JsonNode node) {
        Map<String, Object> objectMap = new LinkedHashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String name = entry.getKey();
            JsonNode value = entry.getValue();
            Object object = loadObject(value);
            objectMap.put(name, object);
        }

        return new Document(objectMap);
    }

    private Object loadObject(JsonNode node) {
        if (node == null) return null;
        try {
            switch (node.getNodeType()) {
                case ARRAY:
                    return loadArray(node);
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
                    return loadDocument(node);
                case POJO:
                    return loadDocument(node);
                case STRING:
                    return node.textValue();
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List loadArray(JsonNode array) {
        if (array.isArray()) {
            List list = new ArrayList();
            Iterator iterator = array.elements();
            while (iterator.hasNext()) {
                Object element = iterator.next();
                if (element instanceof JsonNode) {
                    list.add(loadObject((JsonNode) element));
                } else {
                    list.add(element);
                }
            }
            return list;
        }
        return null;
    }
}
