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

import org.dizitart.no2.collection.IndexType;
import org.dizitart.no2.index.Index;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.dizitart.no2.common.Constants.INTERNAL_NAME_SEPARATOR;
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
