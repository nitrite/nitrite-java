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

package org.dizitart.no2.collection;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.exceptions.InvalidIdException;
import org.dizitart.no2.exceptions.ValidationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.common.Constants.DOC_ID;
import static org.dizitart.no2.common.util.Iterables.listOf;
import static org.dizitart.no2.integration.TestUtil.parse;
import static org.junit.Assert.*;

@Slf4j
public class DocumentTest {
    private Document doc;

    @Test
    public void testCreateDocument() {
        assertTrue(((NitriteDocument) Document.createDocument()).isEmpty());
        assertEquals(1, Document.createDocument("Key", "Value").size());
        assertTrue(((NitriteDocument) Document.createDocument(new HashMap<>(1))).isEmpty());
    }

    @Before
    public void setUp() {
        (new NitriteConfig()).fieldSeparator(".");

        doc = parse("{" +
            "  score: 1034," +
            "  location: {  " +
            "       state: 'NY', " +
            "       city: 'New York', " +
            "       address: {" +
            "            line1: '40', " +
            "            line2: 'ABC Street', " +
            "            house: ['1', '2', '3'] " +
            "       }" +
            "  }," +
            "  category: ['food', 'produce', 'grocery'], " +
            "  objArray: [{ value: 1}, {value: 2}]" +
            "}");
    }

    @After
    public void tearDown() {
        (new NitriteConfig()).fieldSeparator(".");
    }

    @Test
    public void testGetValue() {
        assertNull(doc.get(""));
        assertEquals(doc.get("score"), 1034);
        assertEquals(doc.get("location.state"), "NY");
        assertEquals(doc.get("location.address"), parse("{" +
            "            line1: '40', " +
            "            line2: 'ABC Street', " +
            "            house: ['1', '2', '3'] " +
            "       },"));
        assertEquals(doc.get("location.address.line1"), "40");
        assertNull(doc.get("location.category"));

        assertEquals(doc.get("category"), doc.get("category"));
        assertEquals(doc.get("category.2"), "grocery");
        assertEquals(doc.get("location.address.house.2"), "3");

        assertNotEquals(doc.get("location.address.test"), parse("{" +
            "            line1: '40', " +
            "            line2: 'ABC Street'" +
            "       },"));
        assertNotEquals(doc.get("location.address.test"), "a");
        assertNull(doc.get("."));
        assertNull(doc.get("score.test"));
    }

    @Test
    public void testGetValueWithCustomFieldSeparator() {
        NitriteConfig config = new NitriteConfig();
        config.fieldSeparator(":");
        assertNull(doc.get(""));
        assertEquals(doc.get("score"), 1034);
        assertEquals(doc.get("location:state"), "NY");
        assertEquals(doc.get("location:address"), parse("{" +
            "            line1: '40', " +
            "            line2: 'ABC Street', " +
            "            house: ['1', '2', '3'] " +
            "       },"));
        assertEquals(doc.get("location:address:line1"), "40");
        assertNull(doc.get("location:category"));

        assertEquals(doc.get("category"), doc.get("category"));
        assertEquals(doc.get("category:2"), "grocery");
        assertEquals(doc.get("location:address:house:2"), "3");

        assertNotEquals(doc.get("location:address:test"), parse("{" +
            "            line1: '40', " +
            "            line2: 'ABC Street'" +
            "       },"));
        assertNotEquals(doc.get("location:address:test"), "a");
        assertNull(doc.get(":"));
        assertNull(doc.get("score:test"));

        assertNull(doc.get("location.state"));
        assertNull(doc.get("location.address"));
        assertNull(doc.get("location.address.line1"));
        assertNull(doc.get("location.category"));

        // revert the global field separator value.
        config.fieldSeparator(".");
    }

    @Test
    public void testGetValueObjectArray() {
        assertEquals(doc.get("objArray.0.value"), 1);
    }

    @Test(expected = ValidationException.class)
    public void testGetValueInvalidIndex() {
        assertEquals(doc.get("category.3"), "grocery");
    }

    @Test
    public void testPutNull() {
        assertNotNull(doc.put("test", null));
        assertNull(doc.get("test"));
    }

    @Test(expected = InvalidIdException.class)
    public void testPut() {
        doc.put(DOC_ID, "id");
    }

    @Test(expected = InvalidIdException.class)
    public void testGetId() {
        Map<String, Object> map = new HashMap<>();
        map.put(DOC_ID, "id");

        Document document = createDocument(map);
        document.getId();
    }

    @Test(expected = ValidationException.class)
    public void testGet() {
        String key = "first.array.-1";
        Document document = createDocument()
            .put("first", createDocument().put("array", new int[]{0}));
        document.get(key);
    }

    @Test
    public void testRemove() {
        assertEquals(doc.size(), 4);
        doc.remove("score");
        assertEquals(doc.size(), 3);
    }

    @Test
    public void testRemoveWithCustomFieldSeparator() {
        NitriteConfig config = new NitriteConfig();
        config.fieldSeparator(":");
        assertEquals(((Document)doc.get("location:address")).size(), 3);
        doc.remove("location:address:line1");
        assertEquals(((Document)doc.get("location:address")).size(), 2);
        config.fieldSeparator(".");
    }
    
    @Test
    public void testRemoveFromArray() {
        NitriteConfig config = new NitriteConfig();
        config.fieldSeparator(":");

        assertEquals(((List<?>)doc.get("location:address:house")).size(), 3);
        doc.remove("location:address:house:0");
        assertEquals(((List<?>)doc.get("location:address:house")).size(), 2);

        assertEquals(((List<?>) doc.get("objArray")).size(), 2);
        doc.remove("objArray:0:value");
        assertEquals(((List<?>) doc.get("objArray")).size(), 1);
        assertEquals(((Document) doc.get("objArray:0")).size(), 1);
    }

    @Test
    public void getFields() {
        Set<String> fields = doc.getFields();
        assertEquals(fields.size(), 8);
        assertTrue(fields.contains("location.address.line1"));
        assertTrue(fields.contains("location.address.line2"));
        assertTrue(fields.contains("location.address.house"));
        assertTrue(fields.contains("location.city"));
        assertTrue(fields.contains("location.state"));
        assertTrue(fields.contains("score"));
        assertTrue(fields.contains("objArray"));
        assertTrue(fields.contains("category"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetEmbeddedArrayFields() {
        Document document = createDocument("first", "value")
            .put("seconds", new String[]{"1", "2"})
            .put("third", null)
            .put("fourth", createDocument("first", "value")
                .put("seconds", new String[]{"1", "2"})
                .put("third", createDocument("first", new Integer[]{1, 2})
                    .put("second", "other")
                )
            )
            .put("fifth", listOf(
                createDocument("first", "value")
                    .put("second", new Integer[]{1, 2, 3})
                    .put("third", createDocument("first", "value")
                        .put("second", new Integer[]{1, 2}))
                    .put("fourth", new Document[]{
                        createDocument("first", "value")
                            .put("second", new Integer[]{1, 2}),
                        createDocument("first", "value")
                            .put("second", new Integer[]{1, 2})
                    }),
                createDocument("first", "value")
                    .put("second", new Integer[]{3, 4, 5})
                    .put("third", createDocument("first", "value")
                        .put("second", new Integer[]{1, 2}))
                    .put("fourth", new Document[]{
                        createDocument("first", "value")
                            .put("second", new Integer[]{1, 2}),
                        createDocument("first", "value")
                            .put("second", new Integer[]{1, 2})
                    }),
                createDocument("first", "value")
                    .put("second", new Integer[]{5, 6, 7})
                    .put("third", createDocument("first", "value")
                        .put("second", new Integer[]{1, 2}))
                    .put("fourth", new Document[]{
                        createDocument("first", "value")
                            .put("second", new Integer[]{1, 2}),
                        createDocument("first", "value")
                            .put("second", new Integer[]{3, 4})
                    })
            ));

        List<Integer> intArray = document.get("fifth.second", List.class);
        assertEquals(intArray.size(), 7);

        intArray = document.get("fifth.fourth.second", List.class);
        assertEquals(intArray.size(), 4);

        String value = document.get("fourth.third.second", String.class);
        assertEquals(value, "other");

        int number = document.get("fifth.0.second.0", Integer.class);
        assertEquals(number, 1);

        number = document.get("fifth.1.fourth.0.second.1", Integer.class);
        assertEquals(number, 2);
    }

    @Test
    public void testSerializability() throws IOException, ClassNotFoundException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(doc);
                byte[] data = bos.toByteArray();

                try (ByteArrayInputStream bis = new ByteArrayInputStream(data)) {
                    try (ObjectInputStream ois = new ObjectInputStream(bis)) {
                        Document otherDoc = (Document) ois.readObject();

                        assertEquals(doc, otherDoc);
                    }
                }
            }
        }
    }

    @Test
    public void testDeepPut() {
        doc.put("location.address.pin", 700037);
        assertEquals(doc.get("location.address.pin"), 700037);

        doc.put("location.address.business.pin", 700037);
        assertEquals(doc.get("location.address.business.pin"), 700037);
    }

    @Test
    public void testDeepRemove() {
        doc.remove("location.address.line1");
        doc.remove("location.address.line2");
        doc.remove("location.address.house");
        assertNull(doc.get("location.address"));
    }

    @Test
    public void testContainsField() {
        assertTrue(doc.containsField("location.address.line1"));
        assertFalse(doc.containsField("location.address.line3"));

        assertTrue(doc.containsField("objArray"));
        System.out.println(doc.getId());
    }
}

