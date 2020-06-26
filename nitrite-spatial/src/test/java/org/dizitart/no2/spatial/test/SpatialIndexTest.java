/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.spatial.test;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.filters.FluentFilter;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.mapper.JacksonMapperModule;
import org.dizitart.no2.repository.Cursor;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.spatial.SpatialModule;
import org.dizitart.no2.spatial.mapper.GeometryModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.common.Constants.*;
import static org.dizitart.no2.spatial.FluentFilter.where;
import static org.dizitart.no2.spatial.SpatialIndexer.SpatialIndex;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee
 */
public class SpatialIndexTest {
    private String fileName = getRandomTempDbFile();
    private Nitrite db;
    private NitriteCollection collection;
    private ObjectRepository<SpatialData> repository;
    private SpatialData object1, object2, object3;
    private Document doc1, doc2, doc3;

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.getContentPane().add(new Paint());
        f.setSize(700, 700);
        f.setVisible(true);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public static String getRandomTempDbFile() {
        String dataDir = System.getProperty("java.io.tmpdir") + File.separator + "nitrite" + File.separator + "data";
        File file = new File(dataDir);
        if (!file.exists()) {
            assertTrue(file.mkdirs());
        }
        return file.getPath() + File.separator + UUID.randomUUID().toString() + ".db";
    }

    @Before
    public void before() throws ParseException {
        db = NitriteBuilder.get()
            .filePath(fileName)
            .loadModule(new JacksonMapperModule(new GeometryModule()))
            .loadModule(new SpatialModule())
            .openOrCreate();

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
    public void after() throws IOException {
        if (!db.isClosed()) {
            db.close();
        }

        Files.delete(Paths.get(fileName));
    }

    @Test
    public void testIntersect() throws ParseException {
        WKTReader reader = new WKTReader();
        Geometry search = reader.read("POLYGON ((490 490, 536 490, 536 515, 490 515, 490 490))");

        Cursor<SpatialData> cursor = repository.find(where("geometry").intersects(search));
        assertEquals(cursor.size(), 2);
        assertEquals(cursor.toList(), Arrays.asList(object1, object2));

        collection.createIndex("location", IndexOptions.indexOptions(SpatialIndex));
        DocumentCursor cursor1 = collection.find(where("location").intersects(search));
        assertEquals(cursor1.size(), 2);
        assertEquals(cursor1.toList().stream().map(this::trimMeta).collect(Collectors.toList()), Arrays.asList(doc1, doc2));
    }

    @Test
    public void testWithin() throws ParseException {
        WKTReader reader = new WKTReader();
        Geometry search = reader.read("POLYGON ((490 490, 536 490, 536 515, 490 515, 490 490))");

        Cursor<SpatialData> cursor = repository.find(where("geometry").within(search));
        assertEquals(cursor.size(), 1);
        assertEquals(cursor.toList(), Collections.singletonList(object1));

        collection.createIndex("location", IndexOptions.indexOptions(SpatialIndex));
        DocumentCursor cursor1 = collection.find(where("location").within(search));
        assertEquals(cursor1.size(), 1);
        assertEquals(cursor1.toList().stream().map(this::trimMeta).collect(Collectors.toList()), Collections.singletonList(doc1));
    }

    @Test
    public void testNearPoint() throws ParseException {
        WKTReader reader = new WKTReader();
        Point search = (Point) reader.read("POINT (490 490)");

        Cursor<SpatialData> cursor = repository.find(where("geometry").near(search, 20.0));
        assertEquals(cursor.size(), 1);
        assertEquals(cursor.toList(), Collections.singletonList(object1));

        collection.createIndex("location", IndexOptions.indexOptions(SpatialIndex));
        DocumentCursor cursor1 = collection.find(where("location").near(search, 20.0));
        assertEquals(cursor1.size(), 1);
        assertEquals(cursor1.toList().stream().map(this::trimMeta).collect(Collectors.toList()), Collections.singletonList(doc1));
    }

    @Test
    public void testNearCoordinate() throws ParseException {
        WKTReader reader = new WKTReader();
        Point search = (Point) reader.read("POINT (490 490)");
        Coordinate coordinate = search.getCoordinate();

        Cursor<SpatialData> cursor = repository.find(where("geometry").near(coordinate, 20.0));
        assertEquals(cursor.size(), 1);
        assertEquals(cursor.toList(), Collections.singletonList(object1));

        collection.createIndex("location", IndexOptions.indexOptions(SpatialIndex));
        DocumentCursor cursor1 = collection.find(where("location").near(coordinate, 20.0));
        assertEquals(cursor1.size(), 1);
        assertEquals(cursor1.toList().stream().map(this::trimMeta).collect(Collectors.toList()), Collections.singletonList(doc1));
    }

    @Test(expected = FilterException.class)
    public void testNoIndex() throws ParseException {
        WKTReader reader = new WKTReader();
        Geometry search = reader.read("POLYGON ((490 490, 536 490, 536 515, 490 515, 490 490))");

        DocumentCursor cursor1 = collection.find(where("location").intersects(search));
        assertEquals(cursor1.size(), 2);
        assertEquals(cursor1.toList(), Arrays.asList(doc1, doc2));
    }

    @Test(expected = IndexingException.class)
    public void testIndexExists() {
        collection.createIndex("location", IndexOptions.indexOptions(SpatialIndex));
        collection.createIndex("location", IndexOptions.indexOptions(SpatialIndex));
    }

    @Test
    public void testRemoveIndexEntry() throws ParseException {
        WKTReader reader = new WKTReader();
        Geometry search = reader.read("POLYGON ((490 490, 536 490, 536 515, 490 515, 490 490))");
        WriteResult result = repository.remove(where("geometry").within(search));
        assertEquals(result.getAffectedCount(), 1);
    }

    @Test
    public void testUpdateIndex() throws ParseException {
        WKTReader reader = new WKTReader();
        Geometry search = reader.read("POLYGON ((490 490, 536 490, 536 515, 490 515, 490 490))");
        SpatialData update = new SpatialData();
        update.setId(3L);
        update.setGeometry(search);

        WriteResult result = repository.update(update);
        assertEquals(result.getAffectedCount(), 1);
    }

    @Test(expected = FilterException.class)
    public void testDropIndex() throws ParseException {
        repository.dropIndex("geometry");
        WKTReader reader = new WKTReader();
        Geometry search = reader.read("POLYGON ((490 490, 536 490, 536 515, 490 515, 490 490))");
        Cursor<SpatialData> cursor = repository.find(where("geometry").within(search));
        assertEquals(cursor.size(), 1);
    }

    @Test
    public void testDropAllIndex() {
        repository.dropAllIndices();

        assertFalse(repository.hasIndex("geometry"));
    }

    @Test(expected = FilterException.class)
    public void testFindEqual() throws ParseException {
        WKTReader reader = new WKTReader();
        Geometry search = reader.read("POINT(500 505)");

        Cursor<SpatialData> cursor = repository.find(FluentFilter.where("geometry").eq(search));
        assertEquals(cursor.size(), 2);
        assertEquals(cursor.toList(), Collections.singletonList(object1));
    }

    @Test
    public void testParseGeometry() throws ParseException {
        Nitrite db = NitriteBuilder.get().loadModule(new SpatialModule()).openOrCreate();

        WKTReader reader = new WKTReader();
        Geometry point = reader.read("POINT(500 505)");
        Document document = createDocument("geom", point);

        NitriteCollection collection = db.getCollection("test");
        collection.insert(document);
        collection.createIndex("geom", IndexOptions.indexOptions(SpatialIndex));
        Document doc = collection.find().firstOrNull();

        Document update = doc.clone();
        update.put("geom", reader.read("POINT(0 0)"));
        collection.update(update);
    }

    private Document trimMeta(Document document) {
        document.remove(DOC_ID);
        document.remove(DOC_REVISION);
        document.remove(DOC_MODIFIED);
        document.remove(DOC_SOURCE);
        return document;
    }

    public static class Paint extends JPanel {

        public void paint(Graphics g) {
            try {
                Graphics2D g2d = (Graphics2D) g;
                RenderingHints rh = new RenderingHints(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

                rh.put(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);

                g2d.setRenderingHints(rh);


                WKTReader reader = new WKTReader();
                Geometry point = reader.read("POINT(500 505)");
                Geometry point2 = reader.read("POINT (490 490)");
                Geometry line = reader.read("LINESTRING(550 551, 525 512, 565 566)");
                Geometry polygon = reader.read("POLYGON ((550 521, 580 540, 570 564, 512 566, 550 521))");
                Geometry search = reader.read("POLYGON ((490 490, 536 490, 536 515, 490 515, 490 490))");


                ShapeWriter sw = new ShapeWriter();
                Shape pointShape = sw.toShape(point);
                Shape pointShape2 = sw.toShape(point2);
                Shape lineShape = sw.toShape(line);
                Shape polygonShape = sw.toShape(polygon);
                Shape searchShape = sw.toShape(search);

                g2d.setColor(Color.RED);
                g2d.draw(pointShape);
                g2d.draw(pointShape2);
                g2d.draw(lineShape);
                g2d.draw(polygonShape);

                g2d.setColor(Color.GREEN);
                g2d.draw(searchShape);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
