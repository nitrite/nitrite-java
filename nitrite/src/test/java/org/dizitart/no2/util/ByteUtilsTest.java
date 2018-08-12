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

import org.junit.Test;

import static org.dizitart.no2.util.ByteUtils.bytesToLong;
import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee.
 */
public class ByteUtilsTest {

    @Test
    public void testBytesToLong() {
        long value = bytesToLong(new byte[]{2, 5, 9, 16, 5, 7, 5, 9});
        assertEquals(value, 145532527367881993L);

        value = bytesToLong(new byte[]{0, 0, 0, 0, 0, 0, 0, 0});
        assertEquals(value, 0);
    }
}
