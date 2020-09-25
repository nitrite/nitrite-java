package org.dizitart.no2.index.fulltext.languages;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BulgarianTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Bulgarian()).stopWords();
        assertEquals(518, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("тук"));
        assertTrue(actualStopWordsResult.contains("ð´ð¾ðºð°ñ‚ð¾"));
        assertTrue(actualStopWordsResult.contains("ð´ð¾ð±ñ€ð°"));
        assertTrue(actualStopWordsResult.contains("нищо"));
        assertTrue(actualStopWordsResult.contains("като"));
        assertTrue(actualStopWordsResult.contains("са"));
        assertTrue(actualStopWordsResult.contains("ð´ð¾ð±ñšñ€"));
        assertTrue(actualStopWordsResult.contains("ð´ð¾ð±ñ€ðµ"));
        assertTrue(actualStopWordsResult.contains("ðºð¾ð¹ñ‚ð¾"));
        assertTrue(actualStopWordsResult.contains("се"));
        assertTrue(actualStopWordsResult.contains("ð²ð·ðµð¼ð°"));
    }
}

