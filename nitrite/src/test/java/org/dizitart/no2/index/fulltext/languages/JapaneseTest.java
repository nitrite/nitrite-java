package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class JapaneseTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Japanese()).stopWords();
        assertEquals(134, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("ため"));
        assertTrue(actualStopWordsResult.contains("より"));
        assertTrue(actualStopWordsResult.contains("だっ"));
        assertTrue(actualStopWordsResult.contains("について"));
        assertTrue(actualStopWordsResult.contains("どこ"));
        assertTrue(actualStopWordsResult.contains("たり"));
        assertTrue(actualStopWordsResult.contains("および"));
        assertTrue(actualStopWordsResult.contains("に関する"));
        assertTrue(actualStopWordsResult.contains("による"));
        assertTrue(actualStopWordsResult.contains("により"));
        assertTrue(actualStopWordsResult.contains("我々"));
    }
}

