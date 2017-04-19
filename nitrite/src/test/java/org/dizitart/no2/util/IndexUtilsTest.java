package org.dizitart.no2.util;

import org.dizitart.no2.Index;
import org.dizitart.no2.IndexType;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.dizitart.no2.Constants.INTERNAL_NAME_SEPARATOR;
import static org.dizitart.no2.util.IndexUtils.internalName;
import static org.dizitart.no2.util.IndexUtils.sortByScore;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee.
 */
public class IndexUtilsTest {

    @Test
    public void testInternalName() {
        Index idx = new Index(IndexType.NonUnique, "abc", "coll");
        String name = internalName(idx);

        assertEquals(name, "$nitrite_index" + INTERNAL_NAME_SEPARATOR
                + "coll" + INTERNAL_NAME_SEPARATOR + "abc"
                + INTERNAL_NAME_SEPARATOR + "NonUnique");

        idx = new Index(IndexType.Fulltext, "abc", "coll");
        name = internalName(idx);

        assertEquals(name, "$nitrite_index" + INTERNAL_NAME_SEPARATOR
                + "coll" + INTERNAL_NAME_SEPARATOR + "abc"
                + INTERNAL_NAME_SEPARATOR + "Fulltext");
    }

    @Test
    public void testSortByScore() {
        Map<String, Integer> unsortedMap = new HashMap<String, Integer>() {{
            put("abc", 2);
            put("xyz", 5);
            put("cdf", 9);
            put("lmn", 1);
        }};

        List<String> sortedValues = new LinkedList<String>() {{
            add("cdf");
            add("xyz");
            add("abc");
            add("lmn");
        }};

        Map<String, Integer> sortedMapByScore = sortByScore(unsortedMap);
        assertArrayEquals(sortedMapByScore.keySet().toArray(new String[0]),
                sortedValues.toArray(new String[0]));
    }
}
