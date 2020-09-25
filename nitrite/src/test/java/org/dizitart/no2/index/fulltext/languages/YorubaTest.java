package org.dizitart.no2.index.fulltext.languages;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class YorubaTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Yoruba()).stopWords();
        assertEquals(60, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("ọ̀pọ̀lọpọ̀"));
        assertTrue(actualStopWordsResult.contains("ṣùgbọ́n"));
        assertTrue(actualStopWordsResult.contains("mọ̀"));
        assertTrue(actualStopWordsResult.contains("òun"));
        assertTrue(actualStopWordsResult.contains("pẹ̀lú"));
        assertTrue(actualStopWordsResult.contains("náà"));
        assertTrue(actualStopWordsResult.contains("jẹ"));
        assertTrue(actualStopWordsResult.contains("ṣ"));
        assertTrue(actualStopWordsResult.contains("sínú"));
        assertTrue(actualStopWordsResult.contains("padà"));
        assertTrue(actualStopWordsResult.contains("fún"));
    }
}

