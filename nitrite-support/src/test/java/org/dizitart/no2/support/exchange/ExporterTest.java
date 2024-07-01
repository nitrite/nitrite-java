package org.dizitart.no2.support.exchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class ExporterTest {
    @Test
    public void testCreateObjectMapper() {
        ObjectMapper actualCreateObjectMapperResult = Exporter.createObjectMapper();
        assertNotNull(actualCreateObjectMapperResult);
    }
}

