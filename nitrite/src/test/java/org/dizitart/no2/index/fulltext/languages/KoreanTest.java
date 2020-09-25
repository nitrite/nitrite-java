package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class KoreanTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Korean()).stopWords();
        assertEquals(679, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("가"));
        assertTrue(actualStopWordsResult.contains("저"));
        assertTrue(actualStopWordsResult.contains("할망정"));
        assertTrue(actualStopWordsResult.contains("、"));
        assertTrue(actualStopWordsResult.contains("각"));
        assertTrue(actualStopWordsResult.contains("。"));
        assertTrue(actualStopWordsResult.contains("이봐"));
        assertTrue(actualStopWordsResult.contains("오직"));
        assertTrue(actualStopWordsResult.contains("양자"));
        assertTrue(actualStopWordsResult.contains("〈"));
        assertTrue(actualStopWordsResult.contains("〉"));
    }
}

