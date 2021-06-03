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

package org.dizitart.no2.repository;

import org.dizitart.no2.NitriteBuilderTest;
import org.dizitart.no2.collection.NitriteCollection;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class AnnotationScannerTest {
    @Test
    public void testConstructor() {
        Class<?> type = Object.class;
        assertNull((new AnnotationScanner(type, null, new NitriteBuilderTest.CustomNitriteMapper())).getObjectIdField());
    }

    @Test
    public void testCreateIndices() {
        Class<?> type = Object.class;
        AnnotationScanner annotationScanner = new AnnotationScanner(type, null,
                new NitriteBuilderTest.CustomNitriteMapper());
        annotationScanner.createIndices();
        assertNull(annotationScanner.getObjectIdField());
    }

    @Test
    public void testCreateIdIndex() {
        Class<?> type = Object.class;
        AnnotationScanner annotationScanner = new AnnotationScanner(type, null,
                new NitriteBuilderTest.CustomNitriteMapper());
        annotationScanner.createIdIndex();
        assertNull(annotationScanner.getObjectIdField());
    }

    @Test
    public void testScanIndices() {
        Class<?> type = Object.class;
        AnnotationScanner annotationScanner = new AnnotationScanner(type, null,
                new NitriteBuilderTest.CustomNitriteMapper());
        annotationScanner.scanIndices();
        assertNull(annotationScanner.getObjectIdField());
    }

    @Test
    public void testScanIndices2() {
        Class<?> type = Field.class;
        AnnotationScanner annotationScanner = new AnnotationScanner(type, null,
                new NitriteBuilderTest.CustomNitriteMapper());
        annotationScanner.scanIndices();
        assertNull(annotationScanner.getObjectIdField());
    }

    @Test
    public void testScanIndices3() {
        Class<?> type = Object.class;
        NitriteCollection collection = mock(NitriteCollection.class);
        AnnotationScanner annotationScanner = new AnnotationScanner(type, collection,
                new NitriteBuilderTest.CustomNitriteMapper());
        annotationScanner.scanIndices();
        assertNull(annotationScanner.getObjectIdField());
    }
}

