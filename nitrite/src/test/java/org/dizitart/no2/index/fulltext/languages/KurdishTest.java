package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class KurdishTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Kurdish()).stopWords();
        assertEquals(62, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("لەبارەی"));
        assertTrue(actualStopWordsResult.contains("دە"));
        assertTrue(actualStopWordsResult.contains("بەلای"));
        assertTrue(actualStopWordsResult.contains("لێ"));
        assertTrue(actualStopWordsResult.contains("لەبەر"));
        assertTrue(actualStopWordsResult.contains("ئێوە"));
        assertTrue(actualStopWordsResult.contains("پێ"));
        assertTrue(actualStopWordsResult.contains("لە"));
        assertTrue(actualStopWordsResult.contains("لەرەوی"));
        assertTrue(actualStopWordsResult.contains("بەپێی"));
        assertTrue(actualStopWordsResult.contains("هەر"));
    }
}

