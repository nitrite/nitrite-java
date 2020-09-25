package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class NorwegianTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Norwegian()).stopWords();
        assertEquals(221, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("båe"));
        assertTrue(actualStopWordsResult.contains("deg"));
        assertTrue(actualStopWordsResult.contains("hvem"));
        assertTrue(actualStopWordsResult.contains("dei"));
        assertTrue(actualStopWordsResult.contains("hver"));
        assertTrue(actualStopWordsResult.contains("eitt"));
        assertTrue(actualStopWordsResult.contains("den"));
        assertTrue(actualStopWordsResult.contains("dem"));
        assertTrue(actualStopWordsResult.contains("bra"));
        assertTrue(actualStopWordsResult.contains("der"));
        assertTrue(actualStopWordsResult.contains("det"));
    }
}

