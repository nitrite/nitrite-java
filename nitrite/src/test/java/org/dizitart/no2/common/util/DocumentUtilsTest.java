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

package org.dizitart.no2.common.util;

import org.dizitart.no2.NitriteBuilderTest;
import org.dizitart.no2.Retry;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.filters.IndexAwareFilter;
import org.dizitart.no2.mapper.Mappable;
import org.dizitart.no2.mapper.MappableMapper;
import org.dizitart.no2.mapper.NitriteMapper;
import org.junit.Rule;
import org.junit.Test;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.common.Constants.DOC_REVISION;
import static org.dizitart.no2.common.util.DocumentUtils.*;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DocumentUtilsTest {

    @Rule
    public Retry retry = new Retry(3);

    @Test
    public void testIsRecent() throws InterruptedException {
        Document first = createDocument("key1", "value1");
        Thread.sleep(500);
        Document second = createDocument("key1", "value2");
        assertTrue(isRecent(second, first));
        second = first.clone();
        second.put(DOC_REVISION, 1);

        first.put("key2", "value3");
        first.put(DOC_REVISION, 2);
        assertTrue(isRecent(first, second));
    }

    @Test
    public void testIsRecent2() {
        Document recent = Document.createDocument();
        assertTrue(DocumentUtils.isRecent(recent, Document.createDocument()));
    }

    @Test
    public void testCreateUniqueFilter() {
        Document doc = createDocument("score", 1034)
            .put("location", createDocument("state", "NY")
                .put("city", "New York")
                .put("address", createDocument("line1", "40")
                    .put("line2", "ABC Street")
                    .put("house", new String[]{"1", "2", "3"})))
            .put("category", new String[]{"food", "produce", "grocery"})
            .put("objArray", new Document[]{createDocument("value", 1), createDocument("value", 2)});
        doc.getId();
        Filter filter = createUniqueFilter(doc);
        assertNotNull(filter);
        assertTrue(filter instanceof IndexAwareFilter);
    }

    @Test
    public void testSkeletonDocument() {
        Class type = Object.class;
        assertNull(DocumentUtils.skeletonDocument(new NitriteBuilderTest.CustomNitriteMapper(), type));
    }

    @Test
    public void testSkeletonDocument2() {
        Class<?> forNameResult = Object.class;
        Class<?> forNameResult1 = Object.class;
        MappableMapper nitriteMapper = new MappableMapper(forNameResult, forNameResult1, Object.class);
        assertEquals(0, DocumentUtils.skeletonDocument(nitriteMapper, Object.class).size());
    }

    @Test
    public void testIsSimilar() {
        assertFalse(DocumentUtils.isSimilar(null, Document.createDocument(), "fields"));
        assertFalse(DocumentUtils.isSimilar(null, Document.createDocument(), (String) null));
        assertTrue(DocumentUtils.isSimilar(null, null, "fields"));
        assertFalse(DocumentUtils.isSimilar(Document.createDocument(), null, "fields"));
    }

    @Test
    public void testIsSimilar2() {
        Document document = Document.createDocument();
        assertTrue(DocumentUtils.isSimilar(document, Document.createDocument(), "fields"));
    }

    @Test
    public void testDummyDocument() {
        NitriteMapper nitriteMapper = new MappableMapper();
        Document document = skeletonDocument(nitriteMapper, DummyTest.class);
        assertTrue(document.containsKey("first"));
        assertTrue(document.containsKey("second"));
        assertNull(document.get("first"));
        assertNull(document.get("second"));
    }

    private static class DummyTest implements Mappable {
        private String first;
        private Double second;

        @Override
        public Document write(NitriteMapper mapper) {
            return createDocument("first", first)
                .put("second", second);
        }

        @Override
        public void read(NitriteMapper mapper, Document document) {
            first = document.get("first", String.class);
            second = document.get("second", Double.class);
        }
    }
}
