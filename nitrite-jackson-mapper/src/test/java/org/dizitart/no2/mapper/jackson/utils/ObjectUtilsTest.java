/*
 * Copyright (c) 2017-2022 Nitrite author or authors.
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

package org.dizitart.no2.mapper.jackson.utils;

import org.dizitart.no2.mapper.jackson.JacksonMapper;
import org.dizitart.no2.mapper.jackson.integration.repository.data.Book;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

import static org.dizitart.no2.common.util.ObjectUtils.newInstance;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ObjectUtilsTest {
    @Test
    public void testNewInstance() {
        JacksonMapper mapper = new JacksonMapper();

        assertNotNull(newInstance(Object.class, false, mapper));
        assertNotNull(newInstance(Book.class, false, mapper));

        assertNull(newInstance(byte[].class, false, mapper));
        assertNull(newInstance(Number.class, false, mapper));
        assertNull(newInstance(Byte.class, false, mapper));
        assertNull(newInstance(Short.class, false, mapper));
        assertNull(newInstance(Integer.class, false, mapper));
        assertNull(newInstance(Long.class, false, mapper));
        assertNull(newInstance(Float.class, false, mapper));
        assertNull(newInstance(Double.class, false, mapper));
        assertNull(newInstance(BigDecimal.class, false, mapper));
        assertNull(newInstance(BigInteger.class, false, mapper));
        assertNull(newInstance(Boolean.class, false, mapper));
        assertNull(newInstance(Character.class, false, mapper));
        assertNull(newInstance(String.class, false, mapper));
        assertNull(newInstance(Date.class, false, mapper));
        assertNull(newInstance(URL.class, false, mapper));
        assertNull(newInstance(URI.class, false, mapper));
        assertNull(newInstance(Currency.class, false, mapper));
        assertNull(newInstance(Calendar.class, false, mapper));
        assertNull(newInstance(StringBuffer.class, false, mapper));
        assertNull(newInstance(StringBuilder.class, false, mapper));
        assertNull(newInstance(Locale.class, false, mapper));
        assertNull(newInstance(Void.class, false, mapper));
        assertNull(newInstance(UUID.class, false, mapper));
        assertNull(newInstance(Pattern.class, false, mapper));

        assertNull(newInstance(GregorianCalendar.class, false, mapper));
    }
}
