package org.dizitart.no2.index.fulltext;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LanguagesTest {
    @Test
    public void testValueOf() {
        assertEquals(Languages.ALL, Languages.valueOf("ALL"));
    }

    @Test
    public void testValues() {
        Languages[] actualValuesResult = Languages.values();
        assertEquals(59, actualValuesResult.length);
        assertEquals(Languages.ALL, actualValuesResult[0]);
        assertEquals(Languages.Afrikaans, actualValuesResult[1]);
        assertEquals(Languages.Arabic, actualValuesResult[2]);
        assertEquals(Languages.Armenian, actualValuesResult[3]);
        assertEquals(Languages.Basque, actualValuesResult[4]);
        assertEquals(Languages.Bengali, actualValuesResult[5]);
        assertEquals(Languages.Turkish, actualValuesResult[53]);
        assertEquals(Languages.Ukrainian, actualValuesResult[54]);
        assertEquals(Languages.Urdu, actualValuesResult[55]);
        assertEquals(Languages.Vietnamese, actualValuesResult[56]);
        assertEquals(Languages.Yoruba, actualValuesResult[57]);
        assertEquals(Languages.Zulu, actualValuesResult[58]);
    }
}

