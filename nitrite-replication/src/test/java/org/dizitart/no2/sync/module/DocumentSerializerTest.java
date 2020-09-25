package org.dizitart.no2.sync.module;

import com.fasterxml.jackson.core.util.JsonGeneratorDelegate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import org.dizitart.no2.collection.Document;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DocumentSerializerTest {
    @Test
    public void testSerialize() throws IOException {
        JsonGeneratorDelegate jsonGeneratorDelegate = new JsonGeneratorDelegate(
            new JsonGeneratorDelegate(new JsonGeneratorDelegate(new TokenBuffer(new ObjectMapper(), true))));
        DefaultSerializerProvider.Impl provider = new DefaultSerializerProvider.Impl();
        DocumentSerializer documentSerializer = new DocumentSerializer();
        documentSerializer.serialize(Document.createDocument(), jsonGeneratorDelegate, provider);
        Object actualCurrentValue = jsonGeneratorDelegate.getDelegate().getCurrentValue();
        assertEquals(1, jsonGeneratorDelegate.getOutputContext().getEntryCount());
        assertNull(actualCurrentValue);
    }
}

