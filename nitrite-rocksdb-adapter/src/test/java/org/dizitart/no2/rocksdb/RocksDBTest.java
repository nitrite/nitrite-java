/*
 * Copyright (c) 2019-2020. Nitrite author or authors.
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

package org.dizitart.no2.rocksdb;

import org.dizitart.no2.store.NitriteMap;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.NavigableMap;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee
 */
public class RocksDBTest extends AbstractTest {

    @Test
    public void testRocksDBMap() {
        Calendar cal = Calendar.getInstance();
        NitriteMap<Date, Long> testLevelDBMap = db.getStore().openMap("testRocksDBMap");
        TreeMap<Date, Long> referenceMap = new TreeMap<>((k1, k2) -> {
            if (k1.getClass().equals(k2.getClass())) {
                return k1.compareTo(k2);
            }
            return 1;
        });

        cal.add(Calendar.DATE, -1);
        Date date1 = cal.getTime();

        cal.add(Calendar.DATE, -2);
        Date date2 = cal.getTime();

        cal.add(Calendar.DATE, 5);
        Date date3 = cal.getTime();

        cal = Calendar.getInstance();
        Date date4 = cal.getTime();

        testLevelDBMap.put(date1, 1L); // today - 1
        testLevelDBMap.put(date2, 2L); // today - 2
        testLevelDBMap.put(date3, 3L); // today + 5
        testLevelDBMap.put(date4, 4L); // today

        referenceMap.put(date1, 1L); // today - 1
        referenceMap.put(date2, 2L); // today - 2
        referenceMap.put(date3, 3L); // today + 5
        referenceMap.put(date4, 4L); // today

        testLevelDBMap.entries().forEach(System.out::println);
        System.out.println("*****************");
        referenceMap.entrySet().forEach(System.out::println);

        cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 6);
        Date date5 = cal.getTime();

        System.out.println("Floor Key " + referenceMap.floorKey(date5));
        assertEquals(testLevelDBMap.floorKey(date5), referenceMap.floorKey(date5));     // x <= date5 : date3

        System.out.println("Lower Key " + referenceMap.lowerKey(date5));
        assertEquals(testLevelDBMap.lowerKey(date5), referenceMap.lowerKey(date5));     // x < date5 : date4

        System.out.println("Higher Key " + referenceMap.higherKey(date5));
        assertEquals(testLevelDBMap.higherKey(date5), referenceMap.higherKey(date5));   // date5 >= x :

        System.out.println("Ceiling Key " + referenceMap.ceilingKey(date5));
        assertEquals(testLevelDBMap.ceilingKey(date5), referenceMap.ceilingKey(date5));
    }

    @Test
    public void testRocksDBMapInteger() {
        NitriteMap<Integer, Integer> testLevelDBMap = db.getStore().openMap("testRocksDBMapInteger");
        NavigableMap<Integer, Integer> referenceMap = new TreeMap<>();

        testLevelDBMap.put(1, 1); // today - 1
        testLevelDBMap.put(4, 4); // today
        testLevelDBMap.put(3, 3); // today + 5
        testLevelDBMap.put(2, 2); // today - 2

        referenceMap.put(1, 1); // today - 1
        referenceMap.put(2, 2); // today - 2
        referenceMap.put(3, 3); // today + 5
        referenceMap.put(4, 4); // today

        testLevelDBMap.entries().forEach(System.out::println);

        System.out.println("Floor Key " + referenceMap.floorKey(3));
        assertEquals(testLevelDBMap.floorKey(3), referenceMap.floorKey(3));     // x <= date5 : date3

        System.out.println("Lower Key " + referenceMap.lowerKey(3));
        assertEquals(testLevelDBMap.lowerKey(3), referenceMap.lowerKey(3));     // x < date5 : date4

        System.out.println("Higher Key " + referenceMap.higherKey(3));
        assertEquals(testLevelDBMap.higherKey(3), referenceMap.higherKey(3));   // date5 >= x :

        System.out.println("Ceiling Key " + referenceMap.ceilingKey(3));
        assertEquals(testLevelDBMap.ceilingKey(3), referenceMap.ceilingKey(3));
    }

    @Test
    public void testNaturalSort() {
        NitriteMap<Comparable<?>, Integer> map = db.getStore().openMap("testNaturalSort");
        TreeMap<Comparable<?>, Integer> treeMap = new TreeMap<>();

        map.put("z", 10);
        map.put("Z", 14);
        map.put("w", 13);
        map.put("A", 12);
        map.put("1", 11);

        map.entries().forEach(System.out::println);

        treeMap.put("z", 10);
        treeMap.put("Z", 14);
        treeMap.put("w", 13);
        treeMap.put("A", 12);
        treeMap.put("1", 11);

        System.out.println(map.floorKey("w"));
        System.out.println(map.higherKey("w"));
        System.out.println(map.ceilingKey("w"));
        System.out.println(map.lowerKey("w"));

        System.out.println("***************");

        System.out.println(treeMap.floorKey("w"));
        System.out.println(treeMap.higherKey("w"));
        System.out.println(treeMap.ceilingKey("w"));
        System.out.println(treeMap.lowerKey("w"));
    }
}
