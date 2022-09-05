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

package org.dizitart.no2.common.mapper;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BinaryNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.common.mapper.modules.NitriteIdModule;
import org.dizitart.no2.exceptions.ObjectMappingException;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;

import static org.junit.Assert.*;

public class JacksonMapperTest {
    @Test
    public void testConstructor() {
        ObjectMapper objectMapper = (new JacksonMapper()).getObjectMapper();
        PolymorphicTypeValidator polymorphicTypeValidator = objectMapper.getPolymorphicTypeValidator();
        assertTrue(polymorphicTypeValidator instanceof LaissezFaireSubTypeValidator);
        VisibilityChecker<?> visibilityChecker = objectMapper.getVisibilityChecker();
        assertTrue(visibilityChecker instanceof VisibilityChecker.Std);
        assertNull(objectMapper.getPropertyNamingStrategy());
        assertTrue(objectMapper
            .getDeserializationContext() instanceof com.fasterxml.jackson.databind.deser.DefaultDeserializationContext.Impl);
        assertSame(objectMapper.getFactory(), objectMapper.getJsonFactory());
        assertTrue(objectMapper.getSerializerFactory() instanceof com.fasterxml.jackson.databind.ser.BeanSerializerFactory);
        assertTrue(objectMapper
            .getSerializerProvider() instanceof com.fasterxml.jackson.databind.ser.DefaultSerializerProvider.Impl);
        assertTrue(objectMapper
            .getSerializerProviderInstance() instanceof com.fasterxml.jackson.databind.ser.DefaultSerializerProvider.Impl);
        assertTrue(
            objectMapper.getSubtypeResolver() instanceof com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver);
        DeserializationConfig deserializationConfig = objectMapper.getDeserializationConfig();
        assertTrue(deserializationConfig
            .getAnnotationIntrospector() instanceof com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector);
        assertNull(deserializationConfig.getActiveView());
        assertNull(deserializationConfig.getHandlerInstantiator());
        assertSame(visibilityChecker, deserializationConfig.getDefaultVisibilityChecker());
        assertTrue(deserializationConfig
            .getClassIntrospector() instanceof com.fasterxml.jackson.databind.introspect.BasicClassIntrospector);
        DateFormat expectedDateFormat = objectMapper.getDateFormat();
        assertSame(expectedDateFormat, deserializationConfig.getDateFormat());
        assertNull(deserializationConfig.getFullRootName());
        JsonNodeFactory expectedNodeFactory = objectMapper.getNodeFactory();
        assertSame(expectedNodeFactory, deserializationConfig.getNodeFactory());
        assertSame(polymorphicTypeValidator, deserializationConfig.getPolymorphicTypeValidator());
        assertNull(deserializationConfig.getDefaultMergeable());
        assertEquals(237020288, deserializationConfig.getDeserializationFeatures());
        assertTrue(deserializationConfig.getAttributes() instanceof ContextAttributes.Impl);
    }

    @Test
    public void testConstructor2() {
        NitriteIdModule nitriteIdModule = new NitriteIdModule();
        NitriteIdModule nitriteIdModule1 = new NitriteIdModule();
        JacksonMapper jacksonMapper = new JacksonMapper();
        jacksonMapper.registerJacksonModule(nitriteIdModule);
        jacksonMapper.registerJacksonModule(nitriteIdModule1);
        jacksonMapper.registerJacksonModule(new NitriteIdModule());
        ObjectMapper objectMapper = jacksonMapper.getObjectMapper();
        PolymorphicTypeValidator polymorphicTypeValidator = objectMapper.getPolymorphicTypeValidator();
        assertTrue(polymorphicTypeValidator instanceof LaissezFaireSubTypeValidator);
        VisibilityChecker<?> visibilityChecker = objectMapper.getVisibilityChecker();
        assertTrue(visibilityChecker instanceof VisibilityChecker.Std);
        assertNull(objectMapper.getPropertyNamingStrategy());
        assertTrue(objectMapper
            .getDeserializationContext() instanceof com.fasterxml.jackson.databind.deser.DefaultDeserializationContext.Impl);
        assertSame(objectMapper.getFactory(), objectMapper.getJsonFactory());
        assertTrue(objectMapper.getSerializerFactory() instanceof com.fasterxml.jackson.databind.ser.BeanSerializerFactory);
        assertTrue(objectMapper
            .getSerializerProvider() instanceof com.fasterxml.jackson.databind.ser.DefaultSerializerProvider.Impl);
        assertTrue(objectMapper
            .getSerializerProviderInstance() instanceof com.fasterxml.jackson.databind.ser.DefaultSerializerProvider.Impl);
        assertTrue(
            objectMapper.getSubtypeResolver() instanceof com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver);
        DeserializationConfig deserializationConfig = objectMapper.getDeserializationConfig();
        assertTrue(deserializationConfig
            .getAnnotationIntrospector() instanceof com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector);
        assertNull(deserializationConfig.getActiveView());
        assertNull(deserializationConfig.getHandlerInstantiator());
        assertSame(visibilityChecker, deserializationConfig.getDefaultVisibilityChecker());
        assertTrue(deserializationConfig
            .getClassIntrospector() instanceof com.fasterxml.jackson.databind.introspect.BasicClassIntrospector);
        DateFormat expectedDateFormat = objectMapper.getDateFormat();
        assertSame(expectedDateFormat, deserializationConfig.getDateFormat());
        assertNull(deserializationConfig.getFullRootName());
        JsonNodeFactory expectedNodeFactory = objectMapper.getNodeFactory();
        assertSame(expectedNodeFactory, deserializationConfig.getNodeFactory());
        assertSame(polymorphicTypeValidator, deserializationConfig.getPolymorphicTypeValidator());
        assertNull(deserializationConfig.getDefaultMergeable());
        assertEquals(237020288, deserializationConfig.getDeserializationFeatures());
        assertTrue(deserializationConfig.getAttributes() instanceof ContextAttributes.Impl);
    }

    @Test
    public void testConstructor3() {
        JacksonMapper jacksonMapper = new JacksonMapper();
        jacksonMapper.registerJacksonModule(new NitriteIdModule());
        ObjectMapper objectMapper = jacksonMapper.getObjectMapper();
        PolymorphicTypeValidator polymorphicTypeValidator = objectMapper.getPolymorphicTypeValidator();
        assertTrue(polymorphicTypeValidator instanceof LaissezFaireSubTypeValidator);
        VisibilityChecker<?> visibilityChecker = objectMapper.getVisibilityChecker();
        assertTrue(visibilityChecker instanceof VisibilityChecker.Std);
        assertNull(objectMapper.getPropertyNamingStrategy());
        assertTrue(objectMapper
            .getDeserializationContext() instanceof com.fasterxml.jackson.databind.deser.DefaultDeserializationContext.Impl);
        assertSame(objectMapper.getFactory(), objectMapper.getJsonFactory());
        assertTrue(objectMapper.getSerializerFactory() instanceof com.fasterxml.jackson.databind.ser.BeanSerializerFactory);
        assertTrue(objectMapper
            .getSerializerProvider() instanceof com.fasterxml.jackson.databind.ser.DefaultSerializerProvider.Impl);
        assertTrue(objectMapper
            .getSerializerProviderInstance() instanceof com.fasterxml.jackson.databind.ser.DefaultSerializerProvider.Impl);
        assertTrue(
            objectMapper.getSubtypeResolver() instanceof com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver);
        DeserializationConfig deserializationConfig = objectMapper.getDeserializationConfig();
        assertTrue(deserializationConfig
            .getAnnotationIntrospector() instanceof com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector);
        assertNull(deserializationConfig.getActiveView());
        assertNull(deserializationConfig.getHandlerInstantiator());
        assertSame(visibilityChecker, deserializationConfig.getDefaultVisibilityChecker());
        assertTrue(deserializationConfig
            .getClassIntrospector() instanceof com.fasterxml.jackson.databind.introspect.BasicClassIntrospector);
        DateFormat expectedDateFormat = objectMapper.getDateFormat();
        assertSame(expectedDateFormat, deserializationConfig.getDateFormat());
        assertNull(deserializationConfig.getFullRootName());
        JsonNodeFactory expectedNodeFactory = objectMapper.getNodeFactory();
        assertSame(expectedNodeFactory, deserializationConfig.getNodeFactory());
        assertSame(polymorphicTypeValidator, deserializationConfig.getPolymorphicTypeValidator());
        assertNull(deserializationConfig.getDefaultMergeable());
        assertEquals(237020288, deserializationConfig.getDeserializationFeatures());
        assertTrue(deserializationConfig.getAttributes() instanceof ContextAttributes.Impl);
    }

    @Test
    public void testGetObjectMapper() {
        ObjectMapper actualCreateObjectMapperResult = (new JacksonMapper()).getObjectMapper();
        PolymorphicTypeValidator polymorphicTypeValidator = actualCreateObjectMapperResult.getPolymorphicTypeValidator();
        assertTrue(polymorphicTypeValidator instanceof LaissezFaireSubTypeValidator);
        VisibilityChecker<?> visibilityChecker = actualCreateObjectMapperResult.getVisibilityChecker();
        assertTrue(visibilityChecker instanceof VisibilityChecker.Std);
        assertNull(actualCreateObjectMapperResult.getPropertyNamingStrategy());
        assertTrue(actualCreateObjectMapperResult
            .getDeserializationContext() instanceof com.fasterxml.jackson.databind.deser.DefaultDeserializationContext.Impl);
        assertSame(actualCreateObjectMapperResult.getFactory(), actualCreateObjectMapperResult.getJsonFactory());
        assertTrue(actualCreateObjectMapperResult
            .getSerializerFactory() instanceof com.fasterxml.jackson.databind.ser.BeanSerializerFactory);
        assertTrue(actualCreateObjectMapperResult
            .getSerializerProvider() instanceof com.fasterxml.jackson.databind.ser.DefaultSerializerProvider.Impl);
        assertTrue(actualCreateObjectMapperResult
            .getSerializerProviderInstance() instanceof com.fasterxml.jackson.databind.ser.DefaultSerializerProvider.Impl);
        assertTrue(actualCreateObjectMapperResult
            .getSubtypeResolver() instanceof com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver);
        DeserializationConfig deserializationConfig = actualCreateObjectMapperResult.getDeserializationConfig();
        assertTrue(deserializationConfig.getAttributes() instanceof ContextAttributes.Impl);
        assertTrue(deserializationConfig
            .getAnnotationIntrospector() instanceof com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector);
        assertNull(deserializationConfig.getActiveView());
        assertEquals(237020288, deserializationConfig.getDeserializationFeatures());
        DateFormat expectedDateFormat = actualCreateObjectMapperResult.getDateFormat();
        assertSame(expectedDateFormat, deserializationConfig.getDateFormat());
        assertNull(deserializationConfig.getDefaultMergeable());
        assertSame(visibilityChecker, deserializationConfig.getDefaultVisibilityChecker());
        assertNull(deserializationConfig.getHandlerInstantiator());
        JsonNodeFactory expectedNodeFactory = actualCreateObjectMapperResult.getNodeFactory();
        assertSame(expectedNodeFactory, deserializationConfig.getNodeFactory());
        assertSame(polymorphicTypeValidator, deserializationConfig.getPolymorphicTypeValidator());
        assertTrue(deserializationConfig
            .getClassIntrospector() instanceof com.fasterxml.jackson.databind.introspect.BasicClassIntrospector);
        assertNull(deserializationConfig.getFullRootName());
        assertNull(deserializationConfig.getProblemHandlers());
    }

    @Test
    public void testConvert() {
        JacksonMapper jacksonMapper = new JacksonMapper();
        assertEquals("Source", jacksonMapper.<Object, Object>convert("Source", Object.class));
        assertTrue(jacksonMapper.getObjectMapper()
            .getSerializerProviderInstance() instanceof com.fasterxml.jackson.databind.ser.DefaultSerializerProvider.Impl);
    }

    @Test
    public void testConvert3() {
        JacksonMapper jacksonMapper = new JacksonMapper();
        assertNull(jacksonMapper.convert(null, Object.class));
    }

    @Test
    public void testConvert4() {
        JacksonMapper jacksonMapper = new JacksonMapper();
        assertEquals(0, ((Integer) jacksonMapper.<Object, Object>convert(0, Object.class)).intValue());
        assertTrue(jacksonMapper.getObjectMapper()
            .getSerializerProviderInstance() instanceof com.fasterxml.jackson.databind.ser.DefaultSerializerProvider.Impl);
    }

    @Test
    public void testConvert5() {
        JacksonMapper jacksonMapper = new JacksonMapper();
        ArrayNode source = new ArrayNode(new JsonNodeFactory(true));
        assertThrows(ObjectMappingException.class, () -> jacksonMapper.<Object, Object>convert(source, Object.class));
    }

    @Test
    public void testConvert6() throws UnsupportedEncodingException {
        JacksonMapper jacksonMapper = new JacksonMapper();
        BinaryNode source = new BinaryNode("AAAAAAAAAAAAAAAAAAAAAAAA".getBytes(StandardCharsets.UTF_8));
        assertNull(jacksonMapper.<Object, Object>convert(source, Object.class));
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
        assertTrue(jacksonMapper.getObjectMapper()
            .getSerializerProviderInstance() instanceof com.fasterxml.jackson.databind.ser.DefaultSerializerProvider.Impl);
    }
}

