package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class UkrainianTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Ukrainian()).stopWords();
        assertEquals(28, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("чого"));
        assertTrue(actualStopWordsResult.contains("ви"));
        assertTrue(actualStopWordsResult.contains("він"));
        assertTrue(actualStopWordsResult.contains("як"));
        assertTrue(actualStopWordsResult.contains("чи"));
        assertTrue(actualStopWordsResult.contains("воно"));
        assertTrue(actualStopWordsResult.contains("що"));
        assertTrue(actualStopWordsResult.contains("нам"));
        assertTrue(actualStopWordsResult.contains("інших"));
        assertTrue(actualStopWordsResult.contains("╙"));
        assertTrue(actualStopWordsResult.contains("але"));
    }
}

