package org.dizitart.no2.support.exchange;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import tools.jackson.databind.json.JsonMapper;

public class ExporterTest {
    @Test
    public void testCreateObjectMapper() {
        JsonMapper actualCreateJsonMapperResult = Exporter.createJsonMapper();
        assertNotNull(actualCreateJsonMapperResult);
    }
}

