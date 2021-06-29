/*
 * Copyright (c) 2017-2020 Nitrite author or authors.
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

package org.dizitart.no2.rocksdb;

import lombok.Data;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.Mappable;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.repository.ObjectRepository;
import org.junit.Test;

/**
 * @author Anindya Chatterjee
 */
public class GithubIssues {
    @Test
    public void testIssue412() {
        RocksDBModule dbModule = RocksDBModule.withConfig()
            .filePath("/tmp/rocks-demo")
            .build();
        Nitrite db = Nitrite.builder()
            .loadModule(dbModule)
            .openOrCreate();

        // Step 1
//        NitriteCollection collection = db.getCollection("test");
//        Document document = Document.createDocument("a", 1).put("b", 2);
//        collection.insert(document);
//        System.out.println(collection.size());

        // Step 2
        ObjectRepository<TestData> repository = db.getRepository(TestData.class);
        TestData testData = new TestData();
        testData.setId(1);
        testData.setName("test");
        repository.insert(testData);
        System.out.println(repository.size());
    }

    @Data
    public static class TestData implements Mappable {
        private Integer id;
        private String name;

        @Override
        public Document write(NitriteMapper mapper) {
            return Document.createDocument("id", id).put("name", name);
        }

        @Override
        public void read(NitriteMapper mapper, Document document) {
            id = document.get("id", Integer.class);
            name = document.get("name", String.class);
        }
    }
}
