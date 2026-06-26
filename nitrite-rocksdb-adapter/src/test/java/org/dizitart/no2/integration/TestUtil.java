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

import static org.junit.Assert.assertTrue;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.exceptions.ObjectMappingException;
import org.dizitart.no2.rocksdb.RocksDBModule;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public static void deleteDb(String fileName) {
        try {
            File file = new File(fileName);
            FileUtils.deleteDirectory(file);
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
        RocksDBModule storeModule = RocksDBModule.withConfig()
            .build();

        return Nitrite.builder()
            .loadModule(storeModule)
            .fieldSeparator(".")
            .openOrCreate();
    }

    public static Nitrite createDb(String user, String password) {
        RocksDBModule storeModule = RocksDBModule.withConfig()
            .build();

        return Nitrite.builder()
            .loadModule(storeModule)
            .fieldSeparator(".")
            .openOrCreate(user, password);
    }

    public static Nitrite createDb(String filePath) {
        return Nitrite.builder()
            .loadModule(new RocksDBModule(filePath))
            .fieldSeparator(".")
            .openOrCreate();
    }

    public static Nitrite createDb(String filePath, String user, String password) {
        RocksDBModule storeModule = RocksDBModule.withConfig()
            .filePath(filePath)
            .build();

        return Nitrite.builder()
            .loadModule(storeModule)
            .fieldSeparator(".")
            .openOrCreate(user, password);
    }

    public static Document parse(String json) {
        try {
            ObjectMapper objectMapper = createObjectMapper();
            JsonNode node = objectMapper.readValue(json, JsonNode.class);
            return loadDocument(node);
        } catch (JacksonIOException e) {
            log.error("Error while parsing json", e);
            throw new ObjectMappingException("Failed to parse json " + json);
        }
    }

    private static Document loadDocument(JsonNode node) {
        Map<String, Object> objectMap = new LinkedHashMap<>();
        for (Map.Entry<String, JsonNode> entry : node.properties()) {
            String name = entry.getKey();
            JsonNode value = entry.getValue();
            Object object = loadObject(value);
            objectMap.put(name, object);
        }

        return Document.createDocument(objectMap);
    }

    private static Object loadObject(JsonNode node) {
        if (node == null) {
            return null;
        }

        switch (node.getNodeType()) {
            case ARRAY:
                return loadArray(node);
            case BINARY:
                return node.binaryValue();
            case BOOLEAN:
                return node.booleanValue();
            case MISSING:
            case NUMBER:
                return node.numberValue();
            case OBJECT:
            case POJO:
                return loadDocument(node);
            case STRING:
                return node.stringValue();
            default:
                return null;
        }
    }

    private static List<Object> loadArray(JsonNode array) {
        if (array.isArray()) {
            return array.valueStream()
                .map(TestUtil::loadObject)
                .collect(Collectors.toList());
        }
        return null;
    }

private static ObjectMapper createObjectMapper() {
        return JsonMapper.builder()
            .changeDefaultVisibility(visibilityChecker -> visibilityChecker
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
            )
            .configure(JsonReadFeature.ALLOW_UNQUOTED_PROPERTY_NAMES, true)
            .configure(JsonReadFeature.ALLOW_SINGLE_QUOTES, true)
            .configure(JsonReadFeature.ALLOW_JAVA_COMMENTS, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, false)
            .findAndAddModules()
            .build();
    }
}
