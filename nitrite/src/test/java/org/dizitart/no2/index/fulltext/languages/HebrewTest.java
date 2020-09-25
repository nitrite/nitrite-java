package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class HebrewTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Hebrew()).stopWords();
        assertEquals(194, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("בשעהש"));
        assertTrue(actualStopWordsResult.contains("או"));
        assertTrue(actualStopWordsResult.contains("אז"));
        assertTrue(actualStopWordsResult.contains("אי"));
        assertTrue(actualStopWordsResult.contains("אך"));
        assertTrue(actualStopWordsResult.contains("אל"));
        assertTrue(actualStopWordsResult.contains("עליהן"));
        assertTrue(actualStopWordsResult.contains("אם"));
        assertTrue(actualStopWordsResult.contains("עליהם"));
        assertTrue(actualStopWordsResult.contains("אס"));
        assertTrue(actualStopWordsResult.contains("אף"));
    }
}

