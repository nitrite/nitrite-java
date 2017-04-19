package org.dizitart.no2.objects;

import org.dizitart.no2.IndexOptions;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.SortOrder;
import org.dizitart.no2.objects.data.ClassA;
import org.dizitart.no2.objects.data.ClassC;
import org.junit.Test;

import static org.dizitart.no2.FindOptions.sort;
import static org.dizitart.no2.objects.filters.ObjectFilters.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Anindya Chatterjee.
 */
public class UnAnnotatedObjectTest extends BaseObjectRepositoryTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testFind() {
        Cursor cursor = aObjectRepository.find();
        assertEquals(cursor.size(), 10);
        assertFalse(cursor.hasMore());

        IndexOptions indexOptions = new IndexOptions();
        indexOptions.setIndexType(IndexType.Unique);
        aObjectRepository.createIndex("b.number", indexOptions);

        cursor = aObjectRepository.find(not(eq("b.number", 160)),
                sort("b.number", SortOrder.Ascending).thenLimit(0, 10));

        System.out.println("Available - " + cursor.hasMore());
        System.out.println("Total Size - " + cursor.totalCount());

        Iterable<ClassA> findRecord = cursor.project(ClassA.class);
        for(ClassA classA : findRecord) {
            System.out.println(classA);
        }

        cursor = aObjectRepository.find(not(eq("b.number", 160)),
                sort("b.number", SortOrder.Descending).thenLimit(2, 7));

        System.out.println("Available - " + cursor.hasMore());
        System.out.println("Total Size - " + cursor.totalCount());

        findRecord = cursor.project(ClassA.class);
        for(ClassA classA : findRecord) {
            System.out.println(classA);
        }

        cursor = cObjectRepository.find(gt("id", 900),
                sort("id", SortOrder.Descending).thenLimit(2, 7));
        System.out.println("Available - " + cursor.hasMore());
        System.out.println("Total Size - " + cursor.totalCount());

        Iterable<ClassC> findRecordC = cursor.project(ClassC.class);
        for(ClassC classC : findRecordC) {
            System.out.println(classC);
        }
    }
}
