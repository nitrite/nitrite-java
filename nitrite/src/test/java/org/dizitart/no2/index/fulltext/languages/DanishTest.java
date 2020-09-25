package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class DanishTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Danish()).stopWords();
        assertEquals(170, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("hun"));
        assertTrue(actualStopWordsResult.contains("mod"));
        assertTrue(actualStopWordsResult.contains("hvem"));
        assertTrue(actualStopWordsResult.contains("nyt"));
        assertTrue(actualStopWordsResult.contains("fik"));
        assertTrue(actualStopWordsResult.contains("hver"));
        assertTrue(actualStopWordsResult.contains("den"));
        assertTrue(actualStopWordsResult.contains("hej"));
        assertTrue(actualStopWordsResult.contains("dem"));
        assertTrue(actualStopWordsResult.contains("seks"));
        assertTrue(actualStopWordsResult.contains("der"));
    }
}

