package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class ArmenianTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Armenian()).stopWords();
        assertEquals(45, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("նրա"));
        assertTrue(actualStopWordsResult.contains("հետո"));
        assertTrue(actualStopWordsResult.contains("համար"));
        assertTrue(actualStopWordsResult.contains("էին"));
        assertTrue(actualStopWordsResult.contains("և"));
        assertTrue(actualStopWordsResult.contains("էիր"));
        assertTrue(actualStopWordsResult.contains("որ"));
        assertTrue(actualStopWordsResult.contains("ու"));
        assertTrue(actualStopWordsResult.contains("վրա"));
        assertTrue(actualStopWordsResult.contains("որպես"));
        assertTrue(actualStopWordsResult.contains("այն"));
    }
}

