package org.dizitart.no2.index.fulltext.languages;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CatalanTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Catalan()).stopWords();
        assertEquals(278, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("n'he"));
        assertTrue(actualStopWordsResult.contains("meus"));
        assertTrue(actualStopWordsResult.contains("n'hi"));
        assertTrue(actualStopWordsResult.contains("pels"));
        assertTrue(actualStopWordsResult.contains("del"));
        assertTrue(actualStopWordsResult.contains("tots"));
        assertTrue(actualStopWordsResult.contains("altra"));
        assertTrue(actualStopWordsResult.contains("apa"));
        assertTrue(actualStopWordsResult.contains("des"));
        assertTrue(actualStopWordsResult.contains("altre"));
        assertTrue(actualStopWordsResult.contains("mateixos"));
    }
}

