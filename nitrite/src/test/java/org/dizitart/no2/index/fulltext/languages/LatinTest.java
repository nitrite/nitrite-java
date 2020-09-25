package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class LatinTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Latin()).stopWords();
        assertEquals(49, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("de"));
        assertTrue(actualStopWordsResult.contains("etiam"));
        assertTrue(actualStopWordsResult.contains("hic"));
        assertTrue(actualStopWordsResult.contains("est"));
        assertTrue(actualStopWordsResult.contains("nec"));
        assertTrue(actualStopWordsResult.contains("erat"));
        assertTrue(actualStopWordsResult.contains("ita"));
        assertTrue(actualStopWordsResult.contains("neque"));
        assertTrue(actualStopWordsResult.contains("me"));
        assertTrue(actualStopWordsResult.contains("rebus"));
        assertTrue(actualStopWordsResult.contains("rem"));
    }
}

