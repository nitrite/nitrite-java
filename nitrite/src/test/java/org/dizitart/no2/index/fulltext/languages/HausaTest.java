package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class HausaTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Hausa()).stopWords();
        assertEquals(39, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("wata"));
        assertTrue(actualStopWordsResult.contains("don"));
        assertTrue(actualStopWordsResult.contains("yana"));
        assertTrue(actualStopWordsResult.contains("tafi"));
        assertTrue(actualStopWordsResult.contains("ya"));
        assertTrue(actualStopWordsResult.contains("kuma"));
        assertTrue(actualStopWordsResult.contains("sun"));
        assertTrue(actualStopWordsResult.contains("ban"));
        assertTrue(actualStopWordsResult.contains("cikin"));
        assertTrue(actualStopWordsResult.contains("suka"));
        assertTrue(actualStopWordsResult.contains("yi"));
    }
}

