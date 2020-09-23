package org.dizitart.no2.collection.operation;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.util.ArrayIterator;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.store.memory.InMemoryMap;
import org.junit.Test;

public class IndexedStreamTest {
    @Test
    public void testIndexedStreamIteratorHasNext() {
        NitriteId newIdResult = NitriteId.newId();
        NitriteId newIdResult1 = NitriteId.newId();
        ArrayIterator<NitriteId> iterator = new ArrayIterator<NitriteId>(
            new NitriteId[]{newIdResult, newIdResult1, NitriteId.newId()});
        assertTrue(
            (new IndexedStream.IndexedStreamIterator(iterator, new InMemoryMap<NitriteId, Document>("mapName", null)))
                .hasNext());
    }

    @Test
    public void testIndexedStreamIteratorNext() {
        NitriteId newIdResult = NitriteId.newId();
        NitriteId newIdResult1 = NitriteId.newId();
        ArrayIterator<NitriteId> iterator = new ArrayIterator<NitriteId>(
            new NitriteId[]{newIdResult, newIdResult1, NitriteId.newId()});
        IndexedStream.IndexedStreamIterator indexedStreamIterator = new IndexedStream.IndexedStreamIterator(iterator,
            new InMemoryMap<NitriteId, Document>("mapName", null));
        assertNull(indexedStreamIterator.next().getSecond());
        assertTrue(indexedStreamIterator.hasNext());
    }
}

