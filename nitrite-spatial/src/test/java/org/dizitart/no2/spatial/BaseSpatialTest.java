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

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.repository.ObjectRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.common.Constants.*;
import static org.dizitart.no2.spatial.TestUtil.*;

/**
 * @author Anindya Chatterjee
 */
public abstract class BaseSpatialTest {
    private String fileName;
    protected Nitrite db;
    protected NitriteCollection collection;
    protected ObjectRepository<SpatialData> repository;
    protected SpatialData object1, object2, object3;
    protected Document doc1, doc2, doc3;

    @Rule
    public Retry retry = new Retry(3);

    @Before
    public void before() throws ParseException {
        fileName = getRandomTempDbFile();
        db = createDb(fileName);

        collection = db.getCollection("test");
        repository = db.getRepository(SpatialData.class);
        insertObjects();
        insertDocuments();
    }

    protected void insertObjects() throws ParseException {
        WKTReader reader = new WKTReader();

        object1 = new SpatialData();
        object1.setGeometry(reader.read("POINT(500 505)"));
        object1.setId(1L);
        repository.insert(object1);

        object2 = new SpatialData();
        object2.setGeometry(reader.read("LINESTRING(550 551, 525 512, 565 566)"));
        object2.setId(2L);
        repository.insert(object2);

        object3 = new SpatialData();
        object3.setGeometry(reader.read("POLYGON ((550 521, 580 540, 570 564, 512 566, 550 521))"));
        object3.setId(3L);
        repository.insert(object3);
    }

    protected void insertDocuments() throws ParseException {
        WKTReader reader = new WKTReader();

        doc1 = createDocument("key", 1L)
            .put("location", reader.read("POINT(500 505)"));
        collection.insert(doc1);

        doc2 = createDocument("key", 2L)
            .put("location", reader.read("LINESTRING(550 551, 525 512, 565 566)"));
        collection.insert(doc2);

        doc3 = createDocument("key", 3L)
            .put("location", reader.read("POLYGON ((550 521, 580 540, 570 564, 512 566, 550 521))"));
        collection.insert(doc3);
    }

    @After
    public void after() {
        if (db != null && !db.isClosed()) {
            db.close();
        }

        deleteDb(fileName);
    }

    protected Document trimMeta(Document document) {
        document.remove(DOC_ID);
        document.remove(DOC_REVISION);
        document.remove(DOC_MODIFIED);
        document.remove(DOC_SOURCE);
        return document;
    }
}
