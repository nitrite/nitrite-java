package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class RomanianTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Romanian()).stopWords();
        assertEquals(434, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("lui"));
        assertTrue(actualStopWordsResult.contains("fie"));
        assertTrue(actualStopWordsResult.contains("fara"));
        assertTrue(actualStopWordsResult.contains("fii"));
        assertTrue(actualStopWordsResult.contains("anume"));
        assertTrue(actualStopWordsResult.contains("voastre"));
        assertTrue(actualStopWordsResult.contains("fim"));
        assertTrue(actualStopWordsResult.contains("patrulea"));
        assertTrue(actualStopWordsResult.contains("bine"));
        assertTrue(actualStopWordsResult.contains("acea"));
        assertTrue(actualStopWordsResult.contains("fiu"));
    }
}

