package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class ChineseTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Chinese()).stopWords();
        assertEquals(788, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("、"));
        assertTrue(actualStopWordsResult.contains("有时"));
        assertTrue(actualStopWordsResult.contains("。"));
        assertTrue(actualStopWordsResult.contains("要不然"));
        assertTrue(actualStopWordsResult.contains("哪边"));
        assertTrue(actualStopWordsResult.contains("者"));
        assertTrue(actualStopWordsResult.contains("〈"));
        assertTrue(actualStopWordsResult.contains("等到"));
        assertTrue(actualStopWordsResult.contains("〉"));
        assertTrue(actualStopWordsResult.contains("反过来说"));
        assertTrue(actualStopWordsResult.contains("《"));
    }
}

