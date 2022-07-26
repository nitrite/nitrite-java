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

package org.dizitart.no2;

import com.github.javafaker.Faker;
import lombok.SneakyThrows;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.meta.Attributes;
import org.dizitart.no2.common.Constants;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;
import org.junit.Assert;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee
 */
public class TestUtils {
    private final static Faker faker = new Faker();
    private TestUtils() {}

    public static Nitrite createDb() {
        MVStoreModule storeModule = MVStoreModule.withConfig()
            .build();

        return Nitrite.builder()
            .loadModule(storeModule)
            .fieldSeparator(".")
            .openOrCreate();
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

    @SneakyThrows
    public static void deleteDb(String filePath) {
        Files.delete(Paths.get(filePath));
    }

    public static void assertEquals(NitriteCollection c1, NitriteCollection c2) {
        List<Document> l1 = c1.find().toList().stream().map(TestUtils::trimMeta).collect(Collectors.toList());
        List<Document> l2 = c2.find().toList().stream().map(TestUtils::trimMeta).collect(Collectors.toList());
        Assert.assertEquals(l1, l2);
    }

    public static void assertNotEquals(NitriteCollection c1, NitriteCollection c2) {
        List<Document> l1 = c1.find().toList().stream().map(TestUtils::trimMeta).collect(Collectors.toList());
        List<Document> l2 = c2.find().toList().stream().map(TestUtils::trimMeta).collect(Collectors.toList());
        Assert.assertNotEquals(l1, l2);
    }

    public static Document trimMeta(Document document) {
        document.remove(Constants.DOC_ID);
        document.remove(Constants.DOC_REVISION);
        document.remove(Constants.DOC_MODIFIED);
        document.remove(Constants.DOC_SOURCE);
        document.remove("_synced");
        return document;
    }

    public static Document randomDocument() {
        return Document.createDocument()
            .put("firstName", faker.name().firstName())
            .put("lastName", faker.name().lastName())
            .put("age", faker.random().nextLong());
    }

    public static String getRandomTempDbFile() {
        String dataDir = System.getProperty("java.io.tmpdir") + File.separator
            + "nitrite" + File.separator + "data";
        File file = new File(dataDir);
        if (!file.exists()) {
            assertTrue(file.mkdirs());
        }
        return file.getPath() + File.separator + UUID.randomUUID() + ".db";
    }

    public static NitriteMap<NitriteId, Document> getTombstone(NitriteCollection collection) {
        NitriteStore<?> nitriteStore = collection.getStore();
        String tombStoneName = collection.getAttributes().get(Attributes.TOMBSTONE);
        return nitriteStore.openMap(tombStoneName, NitriteId.class, Document.class);
    }
}
