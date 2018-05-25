package org.dizitart.no2.mapper;

import static org.dizitart.no2.exceptions.ErrorCodes.OME_CYCLE_DETECTED;
import static org.dizitart.no2.exceptions.ErrorCodes.OME_NO_DEFAULT_CTOR;
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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JacksonFacade implements MapperFacade {

	private ObjectMapper objectMapper;

	public JacksonFacade() {
		this.objectMapper = createObjectMapper();
	}

	protected ObjectMapper createObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
				.withFieldVisibility(JsonAutoDetect.Visibility.ANY).withGetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withIsGetterVisibility(JsonAutoDetect.Visibility.NONE));
		objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.registerModule(new NitriteIdModule());
		objectMapper.findAndRegisterModules();
		return objectMapper;
	}

	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	@Override
	public Document asDocument(Object object) {
		try {
			JsonNode node = objectMapper.convertValue(object, JsonNode.class);
			return loadDocument(node);
		} catch (IllegalArgumentException iae) {
			log.error("Error while converting object to document ", iae);
			if (iae.getCause() instanceof JsonMappingException) {
				JsonMappingException jme = (JsonMappingException) iae.getCause();
				if (jme.getCause() instanceof StackOverflowError) {
					throw new ObjectMappingException(
							errorMessage("cyclic reference detected. " + jme.getPathReference(), OME_CYCLE_DETECTED));
				}
			}
			throw iae;
		}
	}

	@Override
	public <T> T asObject(Document document, Class<T> type) {
		try {
			return objectMapper.convertValue(document, type);
		} catch (IllegalArgumentException iae) {
			log.error("Error while converting document to object ", iae);
			if (iae.getCause() instanceof JsonMappingException) {
				JsonMappingException jme = (JsonMappingException) iae.getCause();
				if (jme.getMessage().contains("Cannot construct instance")) {
					throw new ObjectMappingException(errorMessage(jme.getMessage(), OME_NO_DEFAULT_CTOR));
				}
			}
			throw iae;
		}
	}

	@Override
	public Object asValue(Object object) {
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
	public boolean isValueType(Object object) {
		JsonNode node = objectMapper.convertValue(object, JsonNode.class);
		return node != null && node.isValueNode();
	}

	@Override
	public Document parse(String json) {
		try {
			JsonNode node = objectMapper.readValue(json, JsonNode.class);
			return loadDocument(node);
		} catch (IOException e) {
			log.error("Error while parsing json", e);
			throw new ObjectMappingException(errorMessage("failed to parse json " + json, OME_PARSE_JSON_FAILED));
		}

	}

	@Override
	public String toJson(Object object) {
		try {
			StringWriter stringWriter = new StringWriter();
			objectMapper.writeValue(stringWriter, object);
			return stringWriter.toString();
		} catch (IOException e) {
			log.error("Error while serializing object to json", e);
			throw new ObjectMappingException(JSON_SERIALIZATION_FAILED);
		}
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
		if (node == null)
			return null;
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
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
