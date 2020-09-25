package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class SwedishTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Swedish()).stopWords();
        assertEquals(418, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("övre"));
        assertTrue(actualStopWordsResult.contains("del"));
        assertTrue(actualStopWordsResult.contains("fin"));
        assertTrue(actualStopWordsResult.contains("än"));
        assertTrue(actualStopWordsResult.contains("den"));
        assertTrue(actualStopWordsResult.contains("dem"));
        assertTrue(actualStopWordsResult.contains("varje"));
        assertTrue(actualStopWordsResult.contains("är"));
        assertTrue(actualStopWordsResult.contains("vid"));
        assertTrue(actualStopWordsResult.contains("det"));
        assertTrue(actualStopWordsResult.contains("kanske"));
    }
}

