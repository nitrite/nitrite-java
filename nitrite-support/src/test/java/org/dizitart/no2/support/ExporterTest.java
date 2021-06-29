package org.dizitart.no2.support;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.SubtypeResolver;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Test;

import static org.junit.Assert.*;

public class ExporterTest {
    @Test
    public void testCreateObjectMapper() {
        ObjectMapper actualCreateObjectMapperResult = Exporter.createObjectMapper();
        DeserializationConfig deserializationConfig = actualCreateObjectMapperResult.getDeserializationConfig();
        assertTrue(actualCreateObjectMapperResult
                .getSerializerFactory() instanceof com.fasterxml.jackson.databind.ser.BeanSerializerFactory);
        PolymorphicTypeValidator polymorphicTypeValidator = actualCreateObjectMapperResult.getPolymorphicTypeValidator();
        assertTrue(polymorphicTypeValidator instanceof LaissezFaireSubTypeValidator);
        assertTrue(actualCreateObjectMapperResult
                .getDeserializationContext() instanceof com.fasterxml.jackson.databind.deser.DefaultDeserializationContext.Impl);
        assertSame(actualCreateObjectMapperResult.getFactory(), actualCreateObjectMapperResult.getJsonFactory());
        assertTrue(actualCreateObjectMapperResult
                .getSerializerProviderInstance() instanceof com.fasterxml.jackson.databind.ser.DefaultSerializerProvider.Impl);
        VisibilityChecker<?> visibilityChecker = actualCreateObjectMapperResult.getVisibilityChecker();
        assertTrue(visibilityChecker instanceof VisibilityChecker.Std);
        assertNull(actualCreateObjectMapperResult.getPropertyNamingStrategy());
        assertTrue(actualCreateObjectMapperResult
                .getSerializerProvider() instanceof com.fasterxml.jackson.databind.ser.DefaultSerializerProvider.Impl);
        SubtypeResolver subtypeResolver = actualCreateObjectMapperResult.getSubtypeResolver();
        assertTrue(subtypeResolver instanceof StdSubtypeResolver);
        assertNull(deserializationConfig.getProblemHandlers());
        assertSame(visibilityChecker, deserializationConfig.getDefaultVisibilityChecker());
        assertTrue(deserializationConfig
                .getClassIntrospector() instanceof com.fasterxml.jackson.databind.introspect.BasicClassIntrospector);
        assertTrue(deserializationConfig.isAnnotationProcessingEnabled());
        assertNull(deserializationConfig.getPropertyNamingStrategy());
        TypeFactory expectedTypeFactory = actualCreateObjectMapperResult.getTypeFactory();
        assertSame(expectedTypeFactory, deserializationConfig.getTypeFactory());
        assertTrue(deserializationConfig.getAttributes() instanceof ContextAttributes.Impl);
        JsonNodeFactory expectedNodeFactory = actualCreateObjectMapperResult.getNodeFactory();
        assertSame(expectedNodeFactory, deserializationConfig.getNodeFactory());
        assertNull(deserializationConfig.getDefaultMergeable());
        assertNull(deserializationConfig.getHandlerInstantiator());
        assertSame(actualCreateObjectMapperResult.getDateFormat(), deserializationConfig.getDateFormat());
        assertNull(deserializationConfig.getFullRootName());
        assertNull(deserializationConfig.getActiveView());
        assertEquals(237020288, deserializationConfig.getDeserializationFeatures());
        assertSame(subtypeResolver, deserializationConfig.getSubtypeResolver());
        assertTrue(deserializationConfig
                .getAnnotationIntrospector() instanceof com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector);
        assertSame(polymorphicTypeValidator, deserializationConfig.getPolymorphicTypeValidator());
    }
}

