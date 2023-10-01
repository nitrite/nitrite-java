package org.dizitart.no2.support.exchange;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.dizitart.no2.support.exchange.ExportOptions;
import org.junit.Test;

public class ExportOptionsTest {

    @Test
    public void testSetExportData() {
        ExportOptions exportOptions = new ExportOptions();
        exportOptions.setExportData(true);
        assertTrue(exportOptions.isExportData());
    }

    @Test
    public void testSetExportIndices() {
        ExportOptions exportOptions = new ExportOptions();
        exportOptions.setExportIndices(true);
        assertTrue(exportOptions.isExportIndices());
    }
}

