/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.transaction;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TransactionStateTest {
    @Test
    public void testValueOf2() {
        assertEquals(TransactionState.Aborted, TransactionState.valueOf("Aborted"));
    }

    @Test
    public void testValues() {
        TransactionState[] actualValuesResult = TransactionState.values();
        assertEquals(6, actualValuesResult.length);
        assertEquals(TransactionState.Active, actualValuesResult[0]);
        assertEquals(TransactionState.PartiallyCommitted, actualValuesResult[1]);
        assertEquals(TransactionState.Committed, actualValuesResult[2]);
        assertEquals(TransactionState.Closed, actualValuesResult[3]);
        assertEquals(TransactionState.Failed, actualValuesResult[4]);
        assertEquals(TransactionState.Aborted, actualValuesResult[5]);
    }
}

