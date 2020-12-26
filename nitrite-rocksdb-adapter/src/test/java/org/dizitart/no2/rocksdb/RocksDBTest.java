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

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.ByteBufferOutput;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.github.javafaker.Faker;
import lombok.Data;
import org.dizitart.no2.store.NitriteMap;
import org.junit.Test;
import org.rocksdb.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee
 */
public class RocksDBTest extends AbstractTest {

    @Test
    public void testRocksDBMap() {
        Calendar cal = Calendar.getInstance();
        NitriteMap<Date, Long> testLevelDBMap = db.getStore().openMap("testRocksDBMap", Date.class, Long.class);
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
        NitriteMap<Integer, Integer> testLevelDBMap = db.getStore().openMap("testRocksDBMapInteger", Integer.class, Integer.class);
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
        NitriteMap<String, Integer> map = db.getStore().openMap("testNaturalSort", String.class, Integer.class);
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

    @Test
    public void test() throws IOException {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        Faker faker = new Faker();

        First first = new First();
        first.setFirstName(faker.name().firstName());
        first.setAddress(faker.address().fullAddress());

        Second second = new Second();
        second.setSecondName(faker.name().firstName());
        second.setAddress(faker.address().fullAddress());

        byte[] data;
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            try (ByteBufferOutput output = new ByteBufferOutput(bos)) {
                kryo.writeClassAndObject(output, first);
                kryo.writeClassAndObject(output, second);
            }
            data = bos.toByteArray();
        }

        try(ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
            try (Input input = new Input(inputStream)) {
                First f1 = (First) kryo.readClassAndObject(input);
                Second s1 = (Second) kryo.readClassAndObject(input);

                assertEquals(first, f1);
                assertEquals(second, s1);
            }
        }
    }

    @Data
    public static class First {
        private String firstName;
        private String address;
    }

    @Data
    public static class Second {
        private String secondName;
        private String address;
    }

    @Test
    public void testPrev() {
        // a static method that loads the RocksDB C++ library.
        RocksDB.loadLibrary();

        try (final ColumnFamilyOptions cfOpts = new ColumnFamilyOptions().optimizeUniversalStyleCompaction()) {

            // list of column family descriptors, first entry must always be default column family
            final List<ColumnFamilyDescriptor> cfDescriptors = Arrays.asList(
                new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, cfOpts),
                new ColumnFamilyDescriptor("testPrev".getBytes(), cfOpts)
            );

            // a list which will hold the handles for the column families once the db is opened
            final List<ColumnFamilyHandle> columnFamilyHandleList =
                new ArrayList<>();

            try (final DBOptions options = new DBOptions()
                .setCreateIfMissing(true)
                .setCreateMissingColumnFamilies(true);
                 final RocksDB db = RocksDB.open(options,
                     System.getProperty("java.io.tmpdir") + File.separator
                         + "nitrite" + File.separator
                         + "data" + File.separator
                         + "test", cfDescriptors,
                     columnFamilyHandleList)) {
                try {
                    ColumnFamilyHandle handle = columnFamilyHandleList.get(1);
                    db.put(handle, "a1".getBytes(), "z1".getBytes());
                    db.put(handle, "a2".getBytes(), "x1".getBytes());
                    db.put(handle, "a3".getBytes(), "y1".getBytes());
                    db.put(handle, "b1".getBytes(), "w1".getBytes());
                    db.put(handle, "b2".getBytes(), "v1".getBytes());

                    try(RocksIterator iterator = db.newIterator(handle)) {
                        iterator.seekForPrev("b1".getBytes());

                        while (iterator.isValid()) {
                            byte[] key = iterator.key();
                            byte[] value = iterator.value();

                            System.out.println("Key: Value = " + new String(key) + ": " + new String(value));
                            iterator.prev();
                        }
                    }
                    // do something

                } finally {

                    // NOTE frees the column family handles before freeing the db
                    for (final ColumnFamilyHandle columnFamilyHandle :
                        columnFamilyHandleList) {
                        columnFamilyHandle.close();
                    }
                } // frees the db and the db options
            } catch (RocksDBException e) {
                e.printStackTrace();
            }
        } // frees the column family options
    }

    @Test
    public void testPrevNitriteMap() {
        NitriteMap<Long, String> testLevelDBMap = db.getStore().openMap("testPrevNitriteMap", Long.class, String.class);
        testLevelDBMap.put(1L, "z1");
        testLevelDBMap.put(2L, "x2");
        testLevelDBMap.put(3L, "y1");
        testLevelDBMap.put(4L, "w1");
        testLevelDBMap.put(10L, "v1");

        Long floorKey = testLevelDBMap.floorKey(10L);
        while (floorKey != null) {
            System.out.println("Key: Value = " + floorKey + ": " + testLevelDBMap.get(floorKey));
            floorKey = testLevelDBMap.lowerKey(floorKey);
        }
    }
}