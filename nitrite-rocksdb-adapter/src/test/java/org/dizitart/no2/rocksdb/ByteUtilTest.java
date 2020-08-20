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

import org.dizitart.no2.rocksdb.formatter.KryoObjectFormatter;
import org.dizitart.no2.rocksdb.formatter.ObjectFormatter;
import org.dizitart.no2.rocksdb.utils.ByteUtil;
import org.junit.Test;

import java.util.Arrays;

import static org.dizitart.no2.rocksdb.utils.ByteUtil.concat;

/**
 * @author Anindya Chatterjee
 */
public class ByteUtilTest {
    private final ObjectFormatter objectFormatter = new KryoObjectFormatter();

    @Test
    public void testConcat() {
        byte[] b1 = new byte[] {1, 2};
        byte[] b2 = new byte[] {3, 4};
        byte[] b3 = new byte[] {5, 6};

        byte[] b4 = concat(b1, b2, b3);

        ByteUtil.split(b4, b2).forEach(item -> System.out.println(Arrays.toString(item)));

    }
}
