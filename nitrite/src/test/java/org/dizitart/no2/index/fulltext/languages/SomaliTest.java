package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class SomaliTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Somali()).stopWords();
        assertEquals(30, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("waxa"));
        assertTrue(actualStopWordsResult.contains("aad"));
        assertTrue(actualStopWordsResult.contains("kuu"));
        assertTrue(actualStopWordsResult.contains("jiray"));
        assertTrue(actualStopWordsResult.contains("uga"));
        assertTrue(actualStopWordsResult.contains("soo"));
        assertTrue(actualStopWordsResult.contains("hadana"));
        assertTrue(actualStopWordsResult.contains("atabo"));
        assertTrue(actualStopWordsResult.contains("ka"));
        assertTrue(actualStopWordsResult.contains("si"));
        assertTrue(actualStopWordsResult.contains("waa"));
    }
}

