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

package org.dizitart.no2.collection.operation;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.common.processors.ProcessorChain;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.index.NitriteIndexer;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.store.memory.InMemoryMap;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Verifies that skip is pushed down to the store layer for paged reads,
 * so that skipped entries are neither read nor deserialized.
 */
public class ReadOperationsPagingTest {
    private static final String COLLECTION = "test";
    private static final int SIZE = 10;

    private InMemoryMap<NitriteId, Document> nitriteMap;
    private IndexOperations indexOperations;

    @Before
    public void setUp() {
        nitriteMap = spy(new InMemoryMap<>(COLLECTION, mock(NitriteStore.class)));
        for (int i = 0; i < SIZE; i++) {
            nitriteMap.put(id(i), Document.createDocument("value", i));
        }
        clearInvocations(nitriteMap);

        indexOperations = mock(IndexOperations.class);
        when(indexOperations.listIndexes()).thenReturn(new ArrayList<>());
    }

    private static NitriteId id(long value) {
        return NitriteId.createId(value);
    }

    private ReadOperations readOperations(NitriteConfig config) {
        return new ReadOperations(COLLECTION, indexOperations, config, nitriteMap, new ProcessorChain());
    }

    private static List<Integer> values(Iterable<Document> documents) {
        List<Integer> values = new ArrayList<>();
        for (Document document : documents) {
            values.add(document.get("value", Integer.class));
        }
        return values;
    }

    @Test
    public void testPureScanWithSkipDelegatesSkipToMap() {
        ReadOperations operations = readOperations(new NitriteConfig());

        List<Integer> result = values(operations.find(Filter.ALL,
            new FindOptions().skip(6).limit(2)));

        assertEquals(List.of(6, 7), result);
        verify(nitriteMap).entries(6L);
    }

    @Test
    public void testPureScanWithSkipOnlyDelegatesSkipToMap() {
        ReadOperations operations = readOperations(new NitriteConfig());

        List<Integer> result = values(operations.find(Filter.ALL,
            new FindOptions().skip(4)));

        assertEquals(List.of(4, 5, 6, 7, 8, 9), result);
        verify(nitriteMap).entries(4L);
    }

    @Test
    public void testPureScanSkipBeyondSizeIsEmpty() {
        ReadOperations operations = readOperations(new NitriteConfig());

        List<Integer> result = values(operations.find(Filter.ALL,
            new FindOptions().skip(SIZE + 5).limit(2)));

        assertEquals(List.of(), result);
        verify(nitriteMap).entries((long) (SIZE + 5));
    }

    @Test
    public void testPureScanWithZeroSkipUsesPlainEntries() {
        ReadOperations operations = readOperations(new NitriteConfig());

        List<Integer> result = values(operations.find(Filter.ALL,
            new FindOptions().skip(0).limit(3)));

        assertEquals(List.of(0, 1, 2), result);
        verify(nitriteMap, never()).entries(anyLong());
    }

    @Test
    public void testNoPushDownWithBlockingSort() {
        ReadOperations operations = readOperations(new NitriteConfig());

        // sort must be applied before skip, so skip cannot be delegated to the map
        List<Integer> result = values(operations.find(Filter.ALL,
            FindOptions.orderBy("value", SortOrder.Descending).skip(6).limit(2)));

        assertEquals(List.of(3, 2), result);
        verify(nitriteMap, never()).entries(anyLong());
    }

    @Test
    public void testNoPushDownWithCollectionScanFilter() {
        ReadOperations operations = readOperations(new NitriteConfig());

        // the filter must see every entry, so skip cannot be delegated to the map
        List<Integer> result = values(operations.find(where("value").gte(2),
            new FindOptions().skip(3).limit(2)));

        assertEquals(List.of(5, 6), result);
        verify(nitriteMap, never()).entries(anyLong());
    }

    @Test
    public void testIndexedScanSkipsWithoutFetchingDocuments() {
        ReadOperations operations = readOperations(configWithIndexOnValueField());

        List<Integer> result = values(operations.find(where("value").eq(3),
            new FindOptions().skip(7).limit(2)));

        assertEquals(List.of(7, 8), result);
        // only the two returned documents may be fetched, none of the seven skipped ones
        verify(nitriteMap, times(2)).get(any());
        verify(nitriteMap).get(id(7));
        verify(nitriteMap).get(id(8));
    }

    @Test
    public void testIndexedScanWithoutSkipFetchesOnlyLimitedDocuments() {
        ReadOperations operations = readOperations(configWithIndexOnValueField());

        List<Integer> result = values(operations.find(where("value").eq(3),
            new FindOptions().limit(2)));

        assertEquals(List.of(0, 1), result);
        verify(nitriteMap, times(2)).get(any());
    }

    /**
     * Sets up an index descriptor on the "value" field backed by a stub indexer
     * that reports every document as matching, in natural id order.
     */
    private NitriteConfig configWithIndexOnValueField() {
        IndexDescriptor indexDescriptor =
            new IndexDescriptor(IndexType.UNIQUE, Fields.withNames("value"), COLLECTION);
        when(indexOperations.listIndexes()).thenReturn(List.of(indexDescriptor));

        LinkedHashSet<NitriteId> matchingIds = new LinkedHashSet<>();
        for (int i = 0; i < SIZE; i++) {
            matchingIds.add(id(i));
        }
        NitriteIndexer indexer = mock(NitriteIndexer.class);
        when(indexer.findByFilter(any(), any())).thenReturn(matchingIds);

        NitriteConfig nitriteConfig = mock(NitriteConfig.class);
        when(nitriteConfig.findIndexer(IndexType.UNIQUE)).thenReturn(indexer);
        return nitriteConfig;
    }
}
