package org.dizitart.no2.util;

import org.dizitart.no2.Document;
import org.dizitart.no2.internals.JacksonMapper;
import org.dizitart.no2.internals.NitriteMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;

import static org.dizitart.no2.util.DocumentUtils.getFields;
import static org.dizitart.no2.util.DocumentUtils.getFieldValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class DocumentUtilsTest {
    private Document doc;

    @Before
    public void setUp() throws IOException {
        NitriteMapper nitriteMapper = new JacksonMapper();
        doc = nitriteMapper.parse("{" +
                "  score: 1034," +
                "  location: {  " +
                "       state: 'NY', " +
                "       city: 'New York', " +
                "       address: {" +
                "            line1: '40', " +
                "            line2: 'ABC Street', " +
                "            house: ['1', '2', '3'] " +
                "       }" +
                "  }," +
                "  category: ['food', 'produce', 'grocery'], " +
                "  objArray: [{ value: 1}, {value: 2}]" +
                "}");
    }

    @Test
    public void testGetValue() throws IOException {
        NitriteMapper nitriteMapper = new JacksonMapper();
        assertEquals(getFieldValue(doc, ""), null);
        assertEquals(getFieldValue(doc, "score"), 1034);
        assertEquals(getFieldValue(doc, "location.state"), "NY");
        assertEquals(getFieldValue(doc, "location.address"), nitriteMapper.parse("{" +
                "            line1: '40', " +
                "            line2: 'ABC Street', " +
                "            house: ['1', '2', '3'] " +
                "       },"));
        assertEquals(getFieldValue(doc, "location.address.line1"), "40");
        assertEquals(getFieldValue(doc, "location.category"), null);

        assertEquals(getFieldValue(doc, "category"), doc.get("category"));
        assertEquals(getFieldValue(doc, "category.2"), "grocery");
        assertEquals(getFieldValue(doc, "location.address.house.2"), "3");

        assertNotEquals(getFieldValue(doc, "location.address.test"), nitriteMapper.parse("{" +
                "            line1: '40', " +
                "            line2: 'ABC Street'" +
                "       },"));
        assertNotEquals(getFieldValue(doc, "location.address.test"), "a");
    }

    @Test
    public void testIndexableFields() {
        Set<String> result = getFields(doc);
        for (String string : result) {
            System.out.println(string);
        }
    }
}
