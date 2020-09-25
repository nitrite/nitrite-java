package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class BrazilianPortugueseTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new BrazilianPortuguese()).stopWords();
        assertEquals(126, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("tua"));
        assertTrue(actualStopWordsResult.contains("depois"));
        assertTrue(actualStopWordsResult.contains("aos"));
        assertTrue(actualStopWordsResult.contains("diversa"));
        assertTrue(actualStopWordsResult.contains("cuja"));
        assertTrue(actualStopWordsResult.contains("outras"));
        assertTrue(actualStopWordsResult.contains("aonde"));
        assertTrue(actualStopWordsResult.contains("pelo"));
        assertTrue(actualStopWordsResult.contains("toda"));
        assertTrue(actualStopWordsResult.contains("isto"));
        assertTrue(actualStopWordsResult.contains("menos"));
    }
}

