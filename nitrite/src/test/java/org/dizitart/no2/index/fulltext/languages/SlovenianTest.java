package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class SlovenianTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Slovenian()).stopWords();
        assertEquals(446, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("skozi"));
        assertTrue(actualStopWordsResult.contains("dobro"));
        assertTrue(actualStopWordsResult.contains("prvi"));
        assertTrue(actualStopWordsResult.contains("morajo"));
        assertTrue(actualStopWordsResult.contains("halo"));
        assertTrue(actualStopWordsResult.contains("kamorkoli"));
        assertTrue(actualStopWordsResult.contains("toda"));
        assertTrue(actualStopWordsResult.contains("prvo"));
        assertTrue(actualStopWordsResult.contains("slab"));
        assertTrue(actualStopWordsResult.contains("dobra"));
        assertTrue(actualStopWordsResult.contains("vaš"));
    }
}

