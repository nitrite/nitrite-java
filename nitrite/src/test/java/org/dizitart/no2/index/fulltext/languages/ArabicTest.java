package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class ArabicTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Arabic()).stopWords();
        assertEquals(480, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("ثمّ"));
        assertTrue(actualStopWordsResult.contains("إذاً"));
        assertTrue(actualStopWordsResult.contains("ايضا"));
        assertTrue(actualStopWordsResult.contains("آمينَ"));
        assertTrue(actualStopWordsResult.contains("إلّا"));
        assertTrue(actualStopWordsResult.contains("وُشْكَانََ"));
        assertTrue(actualStopWordsResult.contains("حيث"));
        assertTrue(actualStopWordsResult.contains("تانِ"));
        assertTrue(actualStopWordsResult.contains("فو"));
        assertTrue(actualStopWordsResult.contains("أمامك"));
        assertTrue(actualStopWordsResult.contains("فى"));
    }
}

