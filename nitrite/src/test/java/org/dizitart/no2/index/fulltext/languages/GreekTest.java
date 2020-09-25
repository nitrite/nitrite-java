package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class GreekTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Greek()).stopWords();
        assertEquals(265, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("παρα"));
        assertTrue(actualStopWordsResult.contains("ὅτε"));
        assertTrue(actualStopWordsResult.contains("για"));
        assertTrue(actualStopWordsResult.contains("ὅτι"));
        assertTrue(actualStopWordsResult.contains("αυτο"));
        assertTrue(actualStopWordsResult.contains("σὺ"));
        assertTrue(actualStopWordsResult.contains("τὸν"));
        assertTrue(actualStopWordsResult.contains("αυτη"));
        assertTrue(actualStopWordsResult.contains("εκεινουσ"));
        assertTrue(actualStopWordsResult.contains("εἰ"));
        assertTrue(actualStopWordsResult.contains("αντι"));
    }
}

