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

package org.dizitart.no2.spatial;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.common.mapper.SimpleNitriteMapper;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.filters.FluentFilter;
import org.dizitart.no2.repository.Cursor;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.repository.annotations.Index;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.locationtech.jts.io.ParseException;

import java.security.SecureRandom;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.common.Constants.DOC_ID;
import static org.dizitart.no2.common.Constants.DOC_MODIFIED;
import static org.dizitart.no2.common.Constants.DOC_REVISION;
import static org.dizitart.no2.common.Constants.DOC_SOURCE;
import static org.dizitart.no2.index.IndexType.NON_UNIQUE;
import static org.dizitart.no2.spatial.TestUtil.createDb;
import static org.dizitart.no2.spatial.TestUtil.deleteDb;
import static org.dizitart.no2.spatial.TestUtil.getRandomTempDbFile;

public class CompoundFilterExampleTest {
    private String fileName;
    protected Nitrite db;
    protected NitriteCollection collection;
    protected ObjectRepository<PartiallyIndexedData> repository;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Index(fields = "indexString", type = NON_UNIQUE)
    public static class PartiallyIndexedData {
        private String indexStr;
        private String otherStr;

        public static class PidConverter implements EntityConverter<PartiallyIndexedData> {

            @Override
            public Class<PartiallyIndexedData> getEntityType() {
                return PartiallyIndexedData.class;
            }

            @Override
            public Document toDocument(PartiallyIndexedData entity, NitriteMapper nitriteMapper) {
                return Document
                    .createDocument("indexStr", entity.getIndexStr())
                    .put("otherStr", entity.getOtherStr());
            }

            @Override
            public PartiallyIndexedData fromDocument(Document document, NitriteMapper nitriteMapper) {
                return new PartiallyIndexedData(
                    document.get("indexStr", String.class),
                    document.get("otherStr", String.class)
                );
            }
        }
    }

    @Rule
    public Retry retry = new Retry(3);


    @Before
    public void before() throws ParseException {
        fileName = getRandomTempDbFile();
        db = createDb(fileName);
        try (SimpleNitriteMapper documentMapper = (SimpleNitriteMapper) db.getConfig().nitriteMapper()) {
            documentMapper.registerEntityConverter(new PartiallyIndexedData.PidConverter());
        }
        repository = db.getRepository(PartiallyIndexedData.class);

        insertObjects();
    }

    // Method to generate a random alphanumeric string of given length
    public static String randomAlphanumeric(int length) {
        // Define characters to choose from (alphanumeric)
        String characters = "abcdefghijklmnopqrstuvwxyz";
        SecureRandom random = new SecureRandom(new byte[] {0, 0, 0});

        // StringBuilder to build the random string
        StringBuilder randomString = new StringBuilder(length);

        // Generate the random string
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            randomString.append(characters.charAt(randomIndex));
        }

        // Return the random string
        return randomString.toString();
    }

    protected void insertObjects() throws ParseException {
        for (int i = 0; i < 10_000; i++) {
            repository.insert(new PartiallyIndexedData(
                randomAlphanumeric(2),
                randomAlphanumeric(2)));
        }
    }

    @After
    public void after() {
        if (db != null && !db.isClosed()) {
            db.close();
        }

        deleteDb(fileName);
    }

    @Test
    public void testMixedQuery() {
        Cursor<PartiallyIndexedData> cursor = repository.find(Filter.and(
            FluentFilter.where("indexStr").eq("aa"),
            FluentFilter.where("otherStr").gt("kk")
        ));
        System.out.println("cursor.getFindPlan() = " + cursor.getFindPlan());
        System.out.println("cursor.toList() = " + cursor.toList());
    }

    protected Document trimMeta(Document document) {
        document.remove(DOC_ID);
        document.remove(DOC_REVISION);
        document.remove(DOC_MODIFIED);
        document.remove(DOC_SOURCE);
        return document;
    }
}
