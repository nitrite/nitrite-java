package org.dizitart.no2.collection.operation;

import static org.junit.Assert.assertFalse;

import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.filters.Filter;
import org.junit.Test;

public class UnionStreamIteratorTest {
    @Test
    public void testHasNext() {
        FilteredRecordStream recordStream = new FilteredRecordStream(null, null);
        FilteredRecordStream recordStream1 = new FilteredRecordStream(recordStream, Filter.byId(NitriteId.newId()));
        FilteredRecordStream lhsStream = new FilteredRecordStream(recordStream1, Filter.byId(NitriteId.newId()));
        FilteredRecordStream recordStream2 = new FilteredRecordStream(null, null);
        FilteredRecordStream recordStream3 = new FilteredRecordStream(recordStream2, Filter.byId(NitriteId.newId()));
        assertFalse(
                (new UnionStreamIterator(lhsStream, new FilteredRecordStream(recordStream3, Filter.byId(NitriteId.newId()))))
                        .hasNext());
    }
}

