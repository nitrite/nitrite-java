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

package org.dizitart.no2.integration.repository;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.common.mapper.EntityConverterMapper;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.index.NitriteTextIndexer;
import org.dizitart.no2.index.fulltext.Languages;
import org.dizitart.no2.index.fulltext.UniversalTextTokenizer;
import org.dizitart.no2.repository.Cursor;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.repository.annotations.Index;
import org.dizitart.no2.repository.annotations.Indices;
import org.dizitart.no2.rocksdb.RocksDBModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.dizitart.no2.common.module.NitriteModule.module;
import static org.dizitart.no2.filters.Filter.ALL;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.dizitart.no2.integration.TestUtil.deleteDb;
import static org.dizitart.no2.integration.TestUtil.getRandomTempDbFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Anindya Chatterjee
 */
public class UniversalTextTokenizerTest extends BaseObjectRepositoryTest {
    private final String fileName = getRandomTempDbFile();
    private ObjectRepository<TextData> textRepository;

    @Before
    @Override
    public void setUp() {
        openDb();
        EntityConverterMapper documentMapper = (EntityConverterMapper) db.getConfig().nitriteMapper();
        documentMapper.registerEntityConverter(new TextData.Converter());

        textRepository = db.getRepository(TextData.class);

        for (int i = 0; i < 10; i++) {
            TextData data = new TextData();
            data.id = i;
            if (i % 2 == 0) {
                data.text = "তারা বলল, “এস আমরা আমাদের জন্যে এক বড় শহর বানাই| আর এমন একটি উঁচু স্তম্ভ বানাই " +
                    "যা আকাশ স্পর্শ করবে| তাহলে আমরা বিখ্যাত হব এবং এটা আমাদের এক সঙ্গে ধরে রাখবে| সারা পৃথিবীতে " +
                    "আমরা ছড়িয়ে থাকব না|”";
            } else if (i % 3 == 0) {
                data.text = "部汁楽示時葉没式将場参右属。覧観将者雄日語山銀玉襲政著約域費。新意虫跡更味株付安署審完顔団。" +
                    "困更表転定一史賀面政巣迎文学豊税乗。白間接特時京転閉務講封新内。側流効表害場測投活聞秀職探画労。" +
                    "福川順式極木注美込行警検直性禁遅。土一詳非物質紙姿漢人内池銀周街躍澹二。平討聞述並時全経詳業映回作朝送恵時。";
            } else if (i % 5 == 0) {
                data.text = " أقبل لسفن العالم، في أما, بـ بال أملاً الثالث،. الذود بالرّد الثالث، مع" +
                    " قام, كردة الضغوط الإمداد أن وصل. ٠٨٠٤ عُقر انتباه يكن قد, أن زهاء وفنلندا بال. لان ما يقوم المعاهدات," +
                    " بـ بخطوط استعملت عدد. صفحة لفشل ولاتّساع لم قام, في مكثّفة الكونجرس جعل, الثالث، واقتصار دون ان.\n";
            } else {
                data.text = "Lorem ipsum dolor sit amet, nobis audire perpetua eu sea. Te semper causae " +
                    "efficiantur per. Qui affert dolorum at. Mel tale constituto interesset in.";
            }
            textRepository.insert(data);
        }
    }

    @After
    @Override
    public void clear() throws Exception {
        if (textRepository != null && !textRepository.isDropped()) {
            textRepository.remove(ALL);
        }

        if (db != null) {
            db.commit();
            db.close();
        }

        deleteDb(fileName);
    }

    @Override
    protected void openDb() {
        RocksDBModule storeModule = RocksDBModule.withConfig()
            .filePath(fileName)
            .build();

        NitriteBuilder nitriteBuilder = Nitrite.builder()
            .fieldSeparator(".")
            .loadModule(storeModule);

        UniversalTextTokenizer tokenizer;
        if (isProtected) {
            tokenizer = new UniversalTextTokenizer(Languages.Bengali, Languages.Chinese, Languages.English);
        } else {
            tokenizer = new UniversalTextTokenizer(Languages.ALL);
        }
        nitriteBuilder.loadModule(module(new NitriteTextIndexer(tokenizer)));

        if (isProtected) {
            db = nitriteBuilder.openOrCreate("test-user", "test-password");
        } else {
            db = nitriteBuilder.openOrCreate();
        }
    }

    @Test
    public void testUniversalFullTextIndexing() {
        Cursor<TextData> cursor = textRepository.find(where("text").text("Lorem"));
        assertEquals(cursor.size(), 2);
        for (TextData data : cursor) {
            System.out.println("Id for English text -> " + data.id);
            if (data.id % 2 == 0 || data.id % 3 == 0 || data.id % 5 == 0) {
                fail();
            }
        }

        cursor = textRepository.find(where("text").text("শহর"));
        assertEquals(cursor.size(), 5);
        for (TextData data : cursor) {
            System.out.println("Id for Bengali text -> " + data.id);
            if (data.id % 2 != 0) {
                fail();
            }
        }

        cursor = textRepository.find(where("text").text("転閉"));
        assertEquals(cursor.size(), 0);
        cursor = textRepository.find(where("text").text("*転閉*"));
        assertEquals(cursor.size(), 2);
        for (TextData data : cursor) {
            System.out.println("Id for Chinese text -> " + data.id);
            if (data.id % 3 != 0) {
                fail();
            }
        }

        cursor = textRepository.find(where("text").text("أقبل"));
        if (isProtected) {
            assertEquals(cursor.size(), 1);
            for (TextData data : cursor) {
                System.out.println("Id for Arabic text -> " + data.id);
                if (data.id % 5 != 0) {
                    fail();
                }
            }
        } else {
            // أقبل eliminated as stop word
            assertEquals(cursor.size(), 0);
        }
    }

    @Indices(
        @Index(fields = "text", type = IndexType.FULL_TEXT)
    )
    public static class TextData {
        public Integer id;
        public String text;

        public static class Converter implements EntityConverter<TextData> {
            @Override
            public Class<TextData> getEntityType() {
                return TextData.class;
            }

            @Override
            public Document toDocument(TextData entity, NitriteMapper nitriteMapper) {
                return Document.createDocument("id", entity.id)
                    .put("text", entity.text);
            }

            @Override
            public TextData fromDocument(Document document, NitriteMapper nitriteMapper) {
                TextData entity = new TextData();
                entity.id = document.get("id", Integer.class);
                entity.text = document.get("text", String.class);
                return entity;
            }
        }
    }
}
