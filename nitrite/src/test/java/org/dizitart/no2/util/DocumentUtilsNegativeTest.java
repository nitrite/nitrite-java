/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.util;

import org.dizitart.no2.Document;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.mapper.JacksonFacade;
import org.dizitart.no2.mapper.JacksonMapper;
import org.dizitart.no2.mapper.MapperFacade;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.AbstractCollection;

import static org.dizitart.no2.util.DocumentUtils.emptyDocument;
import static org.dizitart.no2.util.DocumentUtils.getFieldValue;
import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee.
 */
public class DocumentUtilsNegativeTest {
    private Document doc;

    @Before
    public void setUp() throws IOException {
    	MapperFacade nitriteMapper = new JacksonFacade();
        doc = nitriteMapper.parse("{" +
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

    @Test(expected = ValidationException.class)
    public void testGetValueFailure() {
        assertEquals(getFieldValue(doc, "score.test"), 1034);
    }

    @Test(expected = ValidationException.class)
    public void testGetValueInvalidIndex() {
        assertEquals(getFieldValue(doc, "category.3"), "grocery");
    }

    @Test(expected = ValidationException.class)
    public void testGetValueObjectArray() {
        assertEquals(getFieldValue(doc, "objArray.0.value"), 1);
    }

    @Test(expected = ValidationException.class)
    public void testGetValueInvalidKey() {
        assertEquals(getFieldValue(doc, "."), 1);
    }

    @Test(expected = ValidationException.class)
    public void testEmptyDocumentForInterface() {
        emptyDocument(new JacksonMapper(), Comparable.class);
    }

    @Test(expected = ValidationException.class)
    public void testEmptyDocumentForPrimitive() {
        emptyDocument(new JacksonMapper(), int.class);
    }

    @Test(expected = ValidationException.class)
    public void testEmptyDocumentForArray() {
        emptyDocument(new JacksonMapper(), String[].class);
    }

    @Test(expected = ValidationException.class)
    public void testEmptyDocumentForAbstractClass() {
        emptyDocument(new JacksonMapper(), AbstractCollection.class);
    }
}
