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

package org.dizitart.no2.mapper.jackson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.exceptions.ObjectMappingException;
import org.junit.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.BinaryNode;
import tools.jackson.databind.node.JsonNodeFactory;

import java.nio.charset.StandardCharsets;

public class JacksonMapperTest {
    @Test
    public void testGetObjectMapper() {
        ObjectMapper actualCreateObjectMapperResult = (new JacksonMapper()).getObjectMapper();
        assertNotNull(actualCreateObjectMapperResult);
    }

    @Test
    public void testConvert() {
        JacksonMapper jacksonMapper = new JacksonMapper();
        assertEquals("Source", jacksonMapper.<Object, Object>tryConvert("Source", Object.class));
    }

    @Test
    public void testConvert3() {
        JacksonMapper jacksonMapper = new JacksonMapper();
        assertNull(jacksonMapper.tryConvert(null, Object.class));
    }

    @Test
    public void testConvert4() {
        JacksonMapper jacksonMapper = new JacksonMapper();
        assertEquals(0, ((Integer) jacksonMapper.<Object, Object>tryConvert(0, Object.class)).intValue());
    }

    @Test
    public void testConvert5() {
        JacksonMapper jacksonMapper = new JacksonMapper();
        ArrayNode source = new ArrayNode(new JsonNodeFactory());
        assertThrows(ObjectMappingException.class, () -> jacksonMapper.<Object, Object>tryConvert(source, Object.class));
    }

    @Test
    public void testConvert6() {
        JacksonMapper jacksonMapper = new JacksonMapper();
        BinaryNode source = new BinaryNode("AAAAAAAAAAAAAAAAAAAAAAAA".getBytes(StandardCharsets.UTF_8));
        assertNull(jacksonMapper.<Object, Object>tryConvert(source, Object.class));
    }

    @Test
    public void testInitialize() {
        JacksonMapper jacksonMapper = new JacksonMapper();
        NitriteConfig nitriteConfig = new NitriteConfig();
        jacksonMapper.initialize(nitriteConfig);
        assertEquals(1, nitriteConfig.getSchemaVersion().intValue());
    }

    @Test
    public void testConvertFromDocument() {
        JacksonMapper jacksonMapper = new JacksonMapper();
        assertNull(jacksonMapper.convertFromDocument(null, Object.class));
    }

    @Test
    public void testConvertToDocument() {
        JacksonMapper jacksonMapper = new JacksonMapper();
        jacksonMapper.<Object>convertToDocument("Source");
    }
}

