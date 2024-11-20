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

import lombok.SneakyThrows;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.repository.ObjectRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.common.Constants.DOC_ID;
import static org.dizitart.no2.common.Constants.DOC_MODIFIED;
import static org.dizitart.no2.common.Constants.DOC_REVISION;
import static org.dizitart.no2.common.Constants.DOC_SOURCE;
import static org.dizitart.no2.spatial.TestUtil.createDb;
import static org.dizitart.no2.spatial.TestUtil.deleteDb;
import static org.dizitart.no2.spatial.TestUtil.getRandomTempDbFile;

public abstract class GeoSpatialTestBase {
    private String fileName;
    protected Nitrite db;
    protected NitriteCollection collection;
    protected ObjectRepository<SpatialData> repository;

    static WKTReader reader = new WKTReader();
    protected SpatialData obj2_950m_ESE, obj3_930m_W, obj4_2750m_WSW;
    protected Document doc2_950m_ESE, doc3_930m_W, doc4_2750m_WSW;

    @Rule
    public Retry retry = new Retry(3);

    /**
     * I chose a set of simple landmarks in a major city at high latitude, near 60°N,
     * such that the separation between them is primarily east-west.
     * <p>
     * At the equator, 1 degree of either latitude or longitude measures approx. 111km wide.
     * However, at 60°N, 1 degree of longitude is only half as wide. (cf. cos(60°) == 0.5)
     * <p>
     * This math is not exact enough for the needs of a geographer, but it's close enough to create
     * simple test cases that can distinguish whether we are properly converting meters to/from degrees,
     * including accounting for the curvature of the Earth.
     */
    public static class TestLocations {
        static Point centerPt = readPoint("POINT(59.91437 10.73402)");   // National Theater (Oslo)
        static Point pt2_950m_ESE = readPoint("POINT(59.9115306 10.7501574)"); // Olso Central Station
        static Point pt3_930m_W = readPoint("POINT(59.91433 10.71730)");  // National Library of Norway
        static Point pt4_2750m_WSW = readPoint("POINT(59.90749 10.68670)");  //  Norwegian Museum of Cultural Hist.
    }

    @SneakyThrows
    protected static Point readPoint(String wkt) {
        return (Point) reader.read(wkt);
    }

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
        obj2_950m_ESE = new SpatialData(2L, TestLocations.pt2_950m_ESE);
        obj3_930m_W = new SpatialData(3L, TestLocations.pt3_930m_W);
        obj4_2750m_WSW = new SpatialData(4L, TestLocations.pt4_2750m_WSW);
        repository.insert(obj2_950m_ESE, obj3_930m_W, obj4_2750m_WSW);
    }

    protected void insertDocuments() throws ParseException {
        doc2_950m_ESE = createDocument("key", 2L).put("location", TestLocations.pt2_950m_ESE);
        doc3_930m_W = createDocument("key", 3L).put("location", TestLocations.pt3_930m_W);
        doc4_2750m_WSW = createDocument("key", 4L).put("location", TestLocations.pt4_2750m_WSW);

        collection.insert(doc2_950m_ESE, doc3_930m_W, doc4_2750m_WSW);
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
