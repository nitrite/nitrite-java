package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class PortugueseTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Portuguese()).stopWords();
        assertEquals(560, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("sejam"));
        assertTrue(actualStopWordsResult.contains("catorze"));
        assertTrue(actualStopWordsResult.contains("bastante"));
        assertTrue(actualStopWordsResult.contains("fazemos"));
        assertTrue(actualStopWordsResult.contains("nove"));
        assertTrue(actualStopWordsResult.contains("estavam"));
        assertTrue(actualStopWordsResult.contains("diversa"));
        assertTrue(actualStopWordsResult.contains("poderá"));
        assertTrue(actualStopWordsResult.contains("estiveste"));
        assertTrue(actualStopWordsResult.contains("novo"));
        assertTrue(actualStopWordsResult.contains("fim"));
    }
}

