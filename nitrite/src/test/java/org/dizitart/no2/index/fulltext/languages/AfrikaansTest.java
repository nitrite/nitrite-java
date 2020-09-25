package org.dizitart.no2.index.fulltext.languages;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AfrikaansTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Afrikaans()).stopWords();
        assertEquals(51, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("die"));
        assertTrue(actualStopWordsResult.contains("gesê"));
        assertTrue(actualStopWordsResult.contains("kom"));
        assertTrue(actualStopWordsResult.contains("dag"));
        assertTrue(actualStopWordsResult.contains("nie"));
        assertTrue(actualStopWordsResult.contains("dit"));
        assertTrue(actualStopWordsResult.contains("hy"));
        assertTrue(actualStopWordsResult.contains("ma"));
        assertTrue(actualStopWordsResult.contains("dat"));
        assertTrue(actualStopWordsResult.contains("het"));
        assertTrue(actualStopWordsResult.contains("vir"));
    }
}

