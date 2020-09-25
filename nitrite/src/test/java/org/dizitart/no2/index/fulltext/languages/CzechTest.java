package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class CzechTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Czech()).stopWords();
        assertEquals(550, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("nä›kde"));
        assertTrue(actualStopWordsResult.contains("nove"));
        assertTrue(actualStopWordsResult.contains("této"));
        assertTrue(actualStopWordsResult.contains("budu"));
        assertTrue(actualStopWordsResult.contains("byly"));
        assertTrue(actualStopWordsResult.contains("den"));
        assertTrue(actualStopWordsResult.contains("jedno"));
        assertTrue(actualStopWordsResult.contains("její"));
        assertTrue(actualStopWordsResult.contains("jemu"));
        assertTrue(actualStopWordsResult.contains("každý"));
        assertTrue(actualStopWordsResult.contains("nemã¡te"));
    }
}

