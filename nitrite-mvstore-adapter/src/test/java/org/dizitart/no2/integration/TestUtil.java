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

package org.dizitart.no2.integration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.exceptions.ObjectMappingException;
import org.dizitart.no2.mvstore.MVStoreModule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class TestUtil {

    public static String getRandomTempDbFile() {
        String dataDir = System.getProperty("java.io.tmpdir") + File.separator + "nitrite" + File.separator + "data";
        File file = new File(dataDir);
        if (!file.exists()) {
            assertTrue(file.mkdirs());
        }
        return file.getPath() + File.separator + UUID.randomUUID() + ".db";
    }

    public static void deleteDb(String filePath) {
        try {
            Files.delete(Paths.get(filePath));
        } catch (Exception e) {
            log.error("Error while deleting db", e);
        }
    }

    public static <T extends Comparable<? super T>> boolean isSorted(Iterable<T> iterable, boolean ascending) {
        Iterator<T> iterator = iterable.iterator();
        if (!iterator.hasNext()) {
            return true;
        }
        T t = iterator.next();
        while (iterator.hasNext()) {
            T t2 = iterator.next();
            if (ascending) {
                if (t.compareTo(t2) > 0) {
                    return false;
                }
            } else {
                if (t.compareTo(t2) < 0) {
                    return false;
                }
            }
            t = t2;
        }
        return true;
    }

    public static Nitrite createDb() {
        MVStoreModule storeModule = MVStoreModule.withConfig()
            .build();

        return Nitrite.builder()
            .loadModule(storeModule)
            .fieldSeparator(".")
            .openOrCreate();
    }

    public static Nitrite createDb(String user, String password) {
        MVStoreModule storeModule = MVStoreModule.withConfig()
            .build();

        return Nitrite.builder()
            .loadModule(storeModule)
            .fieldSeparator(".")
            .openOrCreate(user, password);
    }

    public static Nitrite createDb(String filePath) {
        MVStoreModule storeModule = MVStoreModule.withConfig()
            .filePath(filePath)
            .compress(true)
            .build();

        return Nitrite.builder()
            .loadModule(storeModule)
            .fieldSeparator(".")
            .openOrCreate();
    }

    public static Nitrite createDb(String filePath, String user, String password) {
        MVStoreModule storeModule = MVStoreModule.withConfig()
            .filePath(filePath)
            .compress(true)
            .build();

        return Nitrite.builder()
            .loadModule(storeModule)
            .fieldSeparator(".")
            .openOrCreate(user, password);
    }

    public static Nitrite createDb(String filePath, String user, String password,
                                   List<EntityConverter<?>> entityConverters) {
        MVStoreModule storeModule = MVStoreModule.withConfig()
            .filePath(filePath)
            .compress(true)
            .build();

        return Nitrite.builder()
            .loadModule(storeModule)
            .loadModule(() -> new HashSet<>(entityConverters))
            .fieldSeparator(".")
            .openOrCreate(user, password);
    }

    public static Document parse(String json) {
        try {
            ObjectMapper objectMapper = createObjectMapper();
            JsonNode node = objectMapper.readValue(json, JsonNode.class);
            return loadDocument(node);
        } catch (IOException e) {
            log.error("Error while parsing json", e);
            throw new ObjectMappingException("failed to parse json " + json);
        }
    }

    private static Document loadDocument(JsonNode node) {
        Map<String, Object> objectMap = new LinkedHashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String name = entry.getKey();
            JsonNode value = entry.getValue();
            Object object = loadObject(value);
            objectMap.put(name, object);
        }

        return Document.createDocument(objectMap);
    }

    private static Object loadObject(JsonNode node) {
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static List loadArray(JsonNode array) {
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

    private static ObjectMapper createObjectMapper() {
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

        objectMapper.findAndRegisterModules();
        return objectMapper;
    }
}
