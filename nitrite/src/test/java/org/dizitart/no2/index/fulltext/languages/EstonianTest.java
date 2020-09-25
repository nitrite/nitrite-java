package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class EstonianTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Estonian()).stopWords();
        assertEquals(35, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("oma"));
        assertTrue(actualStopWordsResult.contains("mul"));
        assertTrue(actualStopWordsResult.contains("oled"));
        assertTrue(actualStopWordsResult.contains("mulle"));
        assertTrue(actualStopWordsResult.contains("pole"));
        assertTrue(actualStopWordsResult.contains("midagi"));
        assertTrue(actualStopWordsResult.contains("sa"));
        assertTrue(actualStopWordsResult.contains("siin"));
        assertTrue(actualStopWordsResult.contains("nii"));
        assertTrue(actualStopWordsResult.contains("jah"));
        assertTrue(actualStopWordsResult.contains("minu"));
    }
}

