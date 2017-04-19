package org.dizitart.no2.internals;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.Document;
import org.dizitart.no2.exceptions.ObjectMappingException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.JSON_SERIALIZATION_FAILED;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

/**
 * A jackson based {@link NitriteMapper} implementation. It uses
 * jackson's {@link ObjectMapper} to convert an object into a
 * Nitrite {@link Document}.
 *
 * @author Anindya Chatterjee.
 * @since 1.0
 */
@Slf4j
public class JacksonMapper implements NitriteMapper {
    private ObjectMapper objectMapper;

    @Override
    public <T> Document asDocument(T object) {
        ObjectMapper objectMapper = getObjectMapper();
        try {
            JsonNode node = objectMapper.convertValue(object, JsonNode.class);
            return loadDocument(node);
        } catch (IllegalArgumentException iae) {
            if (iae.getCause() instanceof JsonMappingException) {
                JsonMappingException jme = (JsonMappingException) iae.getCause();
                if (jme.getCause() instanceof StackOverflowError) {
                    throw new ObjectMappingException(errorMessage(
                            "cyclic reference detected. " + jme.getPathReference(), OME_CYCLE_DETECTED));
                }
            }
            throw iae;
        }
    }

    @Override
    public <T> T asObject(Document document, Class<T> type) {
        try {
            return getObjectMapper().convertValue(document, type);
        } catch (IllegalArgumentException iae) {
            if (iae.getCause() instanceof JsonMappingException) {
                JsonMappingException jme = (JsonMappingException) iae.getCause();
                if (jme.getMessage().contains("Can not construct instance")) {
                    throw new ObjectMappingException(errorMessage(
                            "no default parameter-less constructor found for "
                            + type.getName(), OME_NO_DEFAULT_CTOR));
                }
            }
            throw iae;
        }
    }

    @Override
    public boolean isValueType(Object object) {
        ObjectMapper objectMapper = getObjectMapper();
        JsonNode node = objectMapper.convertValue(object, JsonNode.class);
        return node != null && node.isValueNode();
    }

    @Override
    public Object asValue(Object object) {
        ObjectMapper objectMapper = getObjectMapper();
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

    @Override
    public Document parse(String json) {
        ObjectMapper objectMapper = getObjectMapper();
        try {
            JsonNode node = objectMapper.readValue(json, JsonNode.class);
            return loadDocument(node);
        } catch (IOException e) {
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
