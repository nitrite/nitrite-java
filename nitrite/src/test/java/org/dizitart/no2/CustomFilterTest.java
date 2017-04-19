package org.dizitart.no2;

import org.dizitart.no2.filters.BaseFilter;
import org.dizitart.no2.store.NitriteMap;
import org.junit.Test;

import java.util.Set;

import static org.dizitart.no2.IndexOptions.indexOptions;
import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee.
 */
public class CustomFilterTest extends BaseCollectionTest {

    @Test
    public void testCustomFilter() {
        insert();
        collection.createIndex("firstName", indexOptions(IndexType.NonUnique));
        Cursor cursor = collection.find(new BaseFilter() {
            @Override
            public Set<NitriteId> apply(NitriteMap<NitriteId, Document> documentMap) {
                return nitriteService.findEqualWithIndex("firstName", "fn1");
            }
        });

        assertEquals(cursor.size(), 1);
        assertEquals(cursor.firstOrDefault().get("firstName"), "fn1");
    }
}
