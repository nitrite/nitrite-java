package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class DutchTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Dutch()).stopWords();
        assertEquals(413, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("weinig"));
        assertTrue(actualStopWordsResult.contains("negen"));
        assertTrue(actualStopWordsResult.contains("eerst"));
        assertTrue(actualStopWordsResult.contains("via"));
        assertTrue(actualStopWordsResult.contains("der"));
        assertTrue(actualStopWordsResult.contains("beetje"));
        assertTrue(actualStopWordsResult.contains("inzake"));
        assertTrue(actualStopWordsResult.contains("toch"));
        assertTrue(actualStopWordsResult.contains("terwijl"));
        assertTrue(actualStopWordsResult.contains("vaak"));
        assertTrue(actualStopWordsResult.contains("mochten"));
    }
}

