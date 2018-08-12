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

package org.dizitart.no2.exceptions;

import org.junit.Test;

import static org.dizitart.no2.exceptions.ErrorMessage.PREFIX;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee.
 */
public class ErrorMessageTest {

    @Test
    public void testErrorMessage() {
        ErrorMessage message = errorMessage("test", 100);
        assertEquals(message.getMessage(), "test");
        assertEquals(message.getErrorCode(), PREFIX + 100);
    }

    @Test
    public void testIsEqual() {
        ErrorMessage message = errorMessage("test", 100);
        assertTrue(message.isEqual(100));
    }
}
