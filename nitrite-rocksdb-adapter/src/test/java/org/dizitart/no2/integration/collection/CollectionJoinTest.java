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

package org.dizitart.no2.integration.collection;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.Lookup;
import org.dizitart.no2.common.RecordStream;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.filters.Filter.ALL;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class CollectionJoinTest extends BaseCollectionTest {
    private NitriteCollection foreignCollection;

    @Before
    @Override
    public void setUp() {
        try {
            super.setUp();
            foreignCollection = db.getCollection("foreign");
            foreignCollection.remove(ALL);

            Document fdoc1 = createDocument("fName", "fn1")
                    .put("address", "ABCD Street")
                    .put("telephone", "123456789");

            Document fdoc2 = createDocument("fName", "fn2")
                    .put("address", "XYZ Street")
                    .put("telephone", "000000000");

            Document fdoc3 = createDocument("fName", "fn2")
                    .put("address", "Some other Street")
                    .put("telephone", "7893141321");

            foreignCollection.insert(fdoc1, fdoc2, fdoc3);
        } catch (Throwable t) {
            log.error("Error while initializing test database", t);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testJoinAll() {
        insert();

        Lookup lookup = new Lookup();
        lookup.setLocalField("firstName");
        lookup.setForeignField("fName");
        lookup.setTargetField("personalDetails");

        RecordStream<Document> result = collection.find().join(foreignCollection.find(), lookup);
        assertEquals(result.size(), 3);

        for (Document document : result) {
            if (document.get("firstName") == "fn1") {
                Collection<Document> personalDetails = (Collection<Document>) document.get("personalDetails");
                assertNotNull(personalDetails);
                assertEquals(personalDetails.size(), 1);
                Object[] details = personalDetails.toArray();
                assertEquals(((Document) details[0]).get("telephone"), "123456789");
            } else if (document.get("firstName") == "fn2") {
                Collection<Document> personalDetails = (Collection<Document>) document.get("personalDetails");
                assertNotNull(personalDetails);
                assertEquals(personalDetails.size(), 2);
                Object[] details = personalDetails.toArray();
                for (Object o : details) {
                    Document d = (Document) o;
                    if (d.get("address").equals("XYZ Street")) {
                        assertEquals(d.get("telephone"), "000000000");
                    } else {
                        assertEquals(d.get("telephone"), "7893141321");
                    }
                }
            } else if (document.get("firstName") == "fn3") {
                assertNull(document.get("personalDetails"));
            }
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testJoinUserOrders() {
        Document user1 = createDocument("firstName", "John")
                .put("lastName", "Doe")
                .put("userId", 1);

        Document user2 = createDocument("firstName", "Jane")
                .put("lastName", "Doe")
                .put("userId", 2);

        NitriteCollection userCollection = db.getCollection("user");
        userCollection.insert(user1, user2);

        Document order1 = createDocument("userId", 1)
                .put("orderId", 1)
                .put("item", "book");

        Document order2 = createDocument("userId", 1)
                .put("orderId", 2)
                .put("item", "pen");

        Document order3 = createDocument("userId", 2)
                .put("orderId", 3)
                .put("item", "pencil");

        Document order4 = createDocument("userId", 2)
                .put("orderId", 4)
                .put("item", "eraser");

        NitriteCollection orderCollection = db.getCollection("order");
        orderCollection.insert(order1, order2, order3, order4);

        Lookup lookup = new Lookup();
        lookup.setLocalField("userId");
        lookup.setForeignField("userId");
        lookup.setTargetField("orders");

        RecordStream<Document> result = userCollection.find().join(orderCollection.find(), lookup);
        assertEquals(result.size(), 2);

        for (Document document : result) {
            if (document.get("userId", Integer.class) == 1) {
                assertEquals(document.get("firstName"), "John");
                assertEquals(document.get("lastName"), "Doe");
                Collection<Document> orders = (Collection<Document>) document.get("orders");
                assertNotNull(orders);
                assertEquals(orders.size(), 2);
                Object[] details = orders.toArray();
                for (Object o : details) {
                    Document d = (Document) o;
                    if (d.get("orderId").equals(1)) {
                        assertEquals(d.get("item"), "book");
                    } else {
                        assertEquals(d.get("item"), "pen");
                    }
                }
            } else if (document.get("userId", Integer.class) == 2) {
                assertEquals(document.get("firstName"), "Jane");
                assertEquals(document.get("lastName"), "Doe");
                Collection<Document> orders = (Collection<Document>) document.get("orders");
                assertNotNull(orders);
                assertEquals(orders.size(), 2);
                Object[] details = orders.toArray();
                for (Object o : details) {
                    Document d = (Document) o;
                    if (d.get("orderId").equals(3)) {
                        assertEquals(d.get("item"), "pencil");
                    } else {
                        assertEquals(d.get("item"), "eraser");
                    }
                }
            }
        }

    }
}
